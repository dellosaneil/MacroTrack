@file:OptIn(ExperimentalForeignApi::class)

package org.thelazybattley.macrotrack.features.scanbarcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.hasTorch
import platform.AVFoundation.requestAccessForMediaType
import platform.AVFoundation.torchMode
import platform.CoreGraphics.CGRect
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

/** The Simulator and some iPads have no camera; the scanner then opens on typing the number. */
actual val isCameraScanSupported: Boolean
    get() = AVCaptureDevice.defaultDeviceWithMediaType(mediaType = AVMediaTypeVideo) != null

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    var status by remember { mutableStateOf(value = cameraAuthorizationStatus()) }
    // Picks up access changed in Settings while the app was in the background.
    LifecycleResumeEffect(key1 = Unit) {
        status = cameraAuthorizationStatus()
        onPauseOrDispose {}
    }
    // iOS asks only once; after a denial the only way back is the app's page in Settings.
    val isPermanentlyDenied = status == AVAuthorizationStatusDenied || status == AVAuthorizationStatusRestricted
    return CameraPermissionState(
        isGranted = status == AVAuthorizationStatusAuthorized,
        isPermanentlyDenied = isPermanentlyDenied,
        request = {
            if (isPermanentlyDenied) {
                NSURL.URLWithString(URLString = UIApplicationOpenSettingsURLString)?.let { url ->
                    UIApplication.sharedApplication.openURL(url = url, options = emptyMap<Any?, Any>(), completionHandler = null)
                }
            } else {
                AVCaptureDevice.requestAccessForMediaType(mediaType = AVMediaTypeVideo) { _ ->
                    dispatch_async(queue = dispatch_get_main_queue()) { status = cameraAuthorizationStatus() }
                }
            }
        }
    )
}

@Composable
actual fun BarcodeCameraPreview(
    modifier: Modifier,
    isTorchOn: Boolean,
    onBarcode: (String) -> Unit
) {
    val latestOnBarcode by rememberUpdatedState(newValue = onBarcode)
    val scanner = remember { BarcodeScanner(onBarcode = { latestOnBarcode(it) }) }
    DisposableEffect(key1 = scanner) {
        scanner.start()
        onDispose { scanner.stop() }
    }
    LaunchedEffect(key1 = isTorchOn) {
        scanner.setTorch(isOn = isTorchOn)
    }
    UIKitView(
        factory = { scanner.previewView },
        modifier = modifier,
        properties = UIKitInteropProperties(isInteractive = false, isNativeAccessibilityEnabled = false)
    )
}

private fun cameraAuthorizationStatus() = AVCaptureDevice.authorizationStatusForMediaType(mediaType = AVMediaTypeVideo)

/** Runs the camera and reports every food barcode it reads, on the main thread. */
private class BarcodeScanner(onBarcode: (String) -> Unit) {
    private val session = AVCaptureSession()
    private val device = AVCaptureDevice.defaultDeviceWithMediaType(mediaType = AVMediaTypeVideo)
    // The metadata output only holds its delegate weakly, so the scanner keeps it alive.
    private val delegate = MetadataDelegate(onBarcode = onBarcode)
    val previewView = CameraPreviewView(session = session)

    init {
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(device = it, error = null) }
        if (input != null && session.canAddInput(input = input)) {
            session.addInput(input = input)
            val output = AVCaptureMetadataOutput()
            if (session.canAddOutput(output = output)) {
                session.addOutput(output = output)
                output.setMetadataObjectsDelegate(objectsDelegate = delegate, queue = dispatch_get_main_queue())
                // Only types the session supports may be set, and only once the output is attached.
                output.metadataObjectTypes = listOf(
                    AVMetadataObjectTypeEAN13Code,
                    AVMetadataObjectTypeEAN8Code,
                    AVMetadataObjectTypeUPCECode
                ).filter { it in output.availableMetadataObjectTypes }
            }
        }
    }

    // Starting and stopping block for a moment, so they run off the main thread.
    fun start() {
        dispatch_async(queue = backgroundQueue()) { if (!session.running) session.startRunning() }
    }

    fun stop() {
        setTorch(isOn = false)
        dispatch_async(queue = backgroundQueue()) { if (session.running) session.stopRunning() }
    }

    fun setTorch(isOn: Boolean) {
        val device = device?.takeIf { it.hasTorch } ?: return
        if (!device.lockForConfiguration(outError = null)) return
        device.torchMode = if (isOn) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        device.unlockForConfiguration()
    }

    private fun backgroundQueue() = dispatch_get_global_queue(identifier = DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), flags = 0u)
}

private class MetadataDelegate(
    private val onBarcode: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        didOutputMetadataObjects
            .firstNotNullOfOrNull { (it as? AVMetadataMachineReadableCodeObject)?.stringValue }
            ?.let(onBarcode)
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
