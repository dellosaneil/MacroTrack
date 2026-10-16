@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberFileActions(): FileActions = remember { IosFileActions() }

private class IosFileActions : FileActions {
    // Document pickers only hold their delegate weakly, so the current one is kept here.
    private var delegate: PickerDelegate? = null

    override fun save(file: SharedFile, onResult: (Boolean) -> Unit) {
        val url = writeTemporary(file = file) ?: return onResult(false)
        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true)
        delegate = PickerDelegate(
            onPicked = { onResult(true) },
            onCancelled = { onResult(false) }
        ).also { picker.delegate = it }
        topViewController()?.presentViewController(picker, animated = true, completion = null)
    }

    override fun share(file: SharedFile) {
        val url = writeTemporary(file = file) ?: return
        val sheet = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
        val presenter = topViewController() ?: return
        // On iPad the share sheet is a popover and needs something to point at.
        sheet.popoverPresentationController()?.sourceView = presenter.view
        presenter.presentViewController(sheet, animated = true, completion = null)
    }

    override fun pick(onPicked: (ByteArray?) -> Unit) {
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeJSON, UTTypeData), asCopy = true)
        delegate = PickerDelegate(
            onPicked = { urls -> onPicked(urls.firstOrNull()?.let { NSData.dataWithContentsOfURL(it)?.toByteArray() }) },
            onCancelled = { onPicked(null) }
        ).also { picker.delegate = it }
        topViewController()?.presentViewController(picker, animated = true, completion = null)
    }

    private fun writeTemporary(file: SharedFile): NSURL? {
        val url = NSURL.fileURLWithPath(path = NSTemporaryDirectory() + file.name)
        return url.takeIf { file.bytes.toNSData().writeToURL(url = it, atomically = true) }
    }
}

private class PickerDelegate(
    private val onPicked: (List<NSURL>) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onPicked(didPickDocumentsAtURLs.filterIsInstance<NSURL>())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCancelled()
    }
}

/** The screen on top, so a picker or sheet opened from a sheet appears above it. */
private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) controller = controller.presentedViewController
    return controller
}

private fun ByteArray.toNSData(): NSData = if (isEmpty()) {
    NSData()
} else {
    usePinned { NSData.create(bytes = it.addressOf(index = 0), length = size.toULong()) }
}

private fun NSData.toByteArray(): ByteArray {
    val length = length.toInt()
    if (length == 0) return ByteArray(size = 0)
    return ByteArray(size = length).apply { usePinned { memcpy(it.addressOf(index = 0), bytes, this@toByteArray.length) } }
}
