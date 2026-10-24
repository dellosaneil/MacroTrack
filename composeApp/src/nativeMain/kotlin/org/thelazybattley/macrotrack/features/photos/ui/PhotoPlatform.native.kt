@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cValue
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRect
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.QuartzCore.CATransaction
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_time
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify
import platform.posix.memcpy

@Composable
actual fun rememberPhotoCamera(): PhotoCamera {
    val camera = remember { IosPhotoCamera() }
    DisposableEffect(key1 = camera) {
        camera.start()
        onDispose { camera.stop() }
    }
    return camera
}

@Composable
actual fun PhotoCameraPreview(camera: PhotoCamera, modifier: Modifier) {
    UIKitView(
        factory = { (camera as IosPhotoCamera).previewView },
        modifier = modifier,
        properties = UIKitInteropProperties(isInteractive = false, isNativeAccessibilityEnabled = false)
    )
}

private class IosPhotoCamera : PhotoCamera {
    private val session = AVCaptureSession().apply { sessionPreset = AVCaptureSessionPresetPhoto }
    private val output = AVCapturePhotoOutput()
    private var input: AVCaptureInput? = null
    // The photo output only holds its delegate weakly, so the one for the photo being taken is kept here.
    private var delegate: CaptureDelegate? = null
    private var frontFacing by mutableStateOf(value = false)
    val previewView = CameraPreviewView(session = session)

    init {
        if (session.canAddOutput(output = output)) session.addOutput(output = output)
        useCamera(isFront = false)
    }

    override var isFrontFacing: Boolean
        get() = frontFacing
        set(value) {
            if (value == frontFacing) return
            frontFacing = value
            dispatch_async(queue = backgroundQueue()) { useCamera(isFront = value) }
        }

    override fun capture(onResult: (ByteArray?) -> Unit) {
        val settings = AVCapturePhotoSettings.photoSettingsWithFormat(format = mapOf<Any?, Any>(AVVideoCodecKey to (AVVideoCodecTypeJPEG ?: "jpeg")))
        output.connectionWithMediaType(mediaType = AVMediaTypeVideo)?.let { connection ->
            if (connection.supportsVideoOrientation) connection.videoOrientation = AVCaptureVideoOrientationPortrait
        }
        delegate = CaptureDelegate { bytes ->
            dispatch_async(queue = dispatch_get_main_queue()) {
                delegate = null
                onResult(bytes)
            }
        }.also { output.capturePhotoWithSettings(settings = settings, delegate = it) }
    }

    // Starting and stopping block for a moment, so they run off the main thread.
    fun start() {
        dispatch_async(queue = backgroundQueue()) { if (!session.running) session.startRunning() }
    }

    fun stop() {
        dispatch_async(queue = backgroundQueue()) { if (session.running) session.stopRunning() }
    }

    private fun useCamera(isFront: Boolean) {
        val device = AVCaptureDevice.defaultDeviceWithDeviceType(
            deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
            mediaType = AVMediaTypeVideo,
            position = if (isFront) AVCaptureDevicePositionFront else AVCaptureDevicePositionBack
        ) ?: return
        val newInput = AVCaptureDeviceInput.deviceInputWithDevice(device = device, error = null) ?: return
        session.beginConfiguration()
        input?.let { session.removeInput(input = it) }
        input = if (session.canAddInput(input = newInput)) newInput.also { session.addInput(input = it) } else null
        // Front-camera photos come out the way the mirrored preview looked.
        output.connectionWithMediaType(mediaType = AVMediaTypeVideo)?.let { connection ->
            if (connection.supportsVideoMirroring) {
                connection.automaticallyAdjustsVideoMirroring = false
                connection.videoMirrored = isFront
            }
        }
        session.commitConfiguration()
    }

    private fun backgroundQueue() = dispatch_get_global_queue(identifier = DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), flags = 0u)
}

private class CaptureDelegate(
    private val onPhoto: (ByteArray?) -> Unit
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {

    @ObjCSignatureOverride
    override fun captureOutput(output: AVCapturePhotoOutput, didFinishProcessingPhoto: AVCapturePhoto, error: NSError?) {
        onPhoto(if (error == null) didFinishProcessingPhoto.fileDataRepresentation()?.toByteArray() else null)
    }
}

/** A view whose preview layer always fills it, so the camera follows Compose's layout. */
private class CameraPreviewView(session: AVCaptureSession) : UIView(frame = cValue<CGRect>()) {
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        layer.addSublayer(layer = previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        // Without this the layer animates to its new size.
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}

@Composable
actual fun rememberPhotoPicker(): PhotoPicker = remember { IosPhotoPicker() }

private class IosPhotoPicker : PhotoPicker {
    // The picker only holds its delegate weakly, so the current one is kept here.
    private var delegate: PickerDelegate? = null

    override fun pick(max: Int, onPicked: (List<ByteArray>) -> Unit) {
        val configuration = PHPickerConfiguration().apply {
            selectionLimit = max.toLong()
            filter = PHPickerFilter.imagesFilter
        }
        val picker = PHPickerViewController(configuration = configuration)
        delegate = PickerDelegate { images ->
            delegate = null
            onPicked(images)
        }.also { picker.delegate = it }
        present(controller = picker, triesLeft = PRESENT_TRIES)
    }
}

private const val PRESENT_TRIES = 20
private const val PRESENT_RETRY_NANOS = 100_000_000L

/**
 * Presents [controller] once nothing is mid-dismissal. The Add photo sheet is still closing when the picker is asked
 * for, and UIKit quietly drops a presentation made during another's dismissal.
 */
private fun present(controller: UIViewController, triesLeft: Int) {
    val top = topViewController()
    if (top != null && !top.isBeingDismissed() && top.presentedViewController == null) {
        top.presentViewController(controller, animated = true, completion = null)
        return
    }
    if (triesLeft == 0) return
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, PRESENT_RETRY_NANOS), dispatch_get_main_queue()) {
        present(controller = controller, triesLeft = triesLeft - 1)
    }
}

private class PickerDelegate(
    private val onPicked: (List<ByteArray>) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(flag = true, completion = null)
        val providers = didFinishPicking.filterIsInstance<PHPickerResult>().map { it.itemProvider }
        val images = arrayOfNulls<ByteArray>(size = providers.size)
        val group = dispatch_group_create()
        providers.forEachIndexed { index, provider ->
            dispatch_group_enter(group)
            // JPEG keeps the date taken readable; photos that can't be had as JPEG come as they are.
            provider.loadImageData { bytes ->
                images[index] = bytes
                dispatch_group_leave(group)
            }
        }
        dispatch_group_notify(group, dispatch_get_main_queue()) { onPicked(images.filterNotNull()) }
    }
}

private fun NSItemProvider.loadImageData(onLoaded: (ByteArray?) -> Unit) {
    val type = if (hasRepresentationConformingToTypeIdentifier(typeIdentifier = "public.jpeg", fileOptions = 0)) "public.jpeg" else "public.image"
    loadDataRepresentationForTypeIdentifier(typeIdentifier = type) { data, _ -> onLoaded(data?.toByteArray()) }
}

/**
 * The screen on top, so the picker opens above any sheet; a screen that's on its way out doesn't count. The window
 * comes from the active scene, since a SwiftUI app may have no key window yet.
 */
private fun topViewController(): UIViewController? {
    val scene = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
    val window = scene?.windows?.filterIsInstance<UIWindow>()?.let { windows -> windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull() }
        ?: UIApplication.sharedApplication.keyWindow
    var controller = window?.rootViewController
    while (true) {
        val presented = controller?.presentedViewController ?: break
        if (presented.isBeingDismissed()) break
        controller = presented
    }
    return controller
}

private fun NSData.toByteArray(): ByteArray {
    val length = length.toInt()
    if (length == 0) return ByteArray(size = 0)
    return ByteArray(size = length).apply { usePinned { memcpy(it.addressOf(index = 0), bytes, this@toByteArray.length) } }
}
