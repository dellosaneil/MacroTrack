package org.thelazybattley.macrotrack.features.scanbarcode.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

actual val isCameraScanSupported: Boolean = true

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var isGranted by remember { mutableStateOf(value = context.hasCameraPermission()) }
    var isPermanentlyDenied by remember { mutableStateOf(value = false) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
        isGranted = granted
        // Once someone denies twice Android stops showing the dialog, so the only way back is the app's settings.
        isPermanentlyDenied = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
    }
    // Picks up access granted in settings while the app was in the background.
    LifecycleResumeEffect(key1 = Unit) {
        isGranted = context.hasCameraPermission()
        onPauseOrDispose {}
    }
    return CameraPermissionState(
        isGranted = isGranted,
        isPermanentlyDenied = isPermanentlyDenied,
        request = {
            if (isPermanentlyDenied) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                launcher.launch(Manifest.permission.CAMERA)
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnBarcode by rememberUpdatedState(newValue = onBarcode)
    val controller = remember {
        LifecycleCameraController(context).apply { setEnabledUseCases(CameraController.IMAGE_ANALYSIS) }
    }
    DisposableEffect(key1 = lifecycleOwner) {
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
                .build()
        )
        val executor = ContextCompat.getMainExecutor(context)
        controller.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(listOf(scanner), ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL, executor) { result ->
                result.getValue(scanner)?.firstNotNullOfOrNull { it.rawValue }?.let { latestOnBarcode(it) }
            }
        )
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
            scanner.close()
        }
    }
    LaunchedEffect(key1 = isTorchOn) {
        controller.enableTorch(isTorchOn)
    }
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                this.controller = controller
            }
        }
    )
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
