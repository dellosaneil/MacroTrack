package org.thelazybattley.macrotrack.features.scanbarcode.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

// The iOS camera scanner isn't built yet; scanning there means typing the number.
actual val isCameraScanSupported: Boolean = false

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState =
    remember { CameraPermissionState(isGranted = false, isPermanentlyDenied = false, request = {}) }

@Composable
actual fun BarcodeCameraPreview(
    modifier: Modifier,
    isTorchOn: Boolean,
    onBarcode: (String) -> Unit
) {
    Box(modifier = modifier)
}
