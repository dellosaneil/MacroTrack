package org.thelazybattley.macrotrack.features.scanbarcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Whether this platform scans with the camera; where it doesn't, the scanner opens on typing the number. */
expect val isCameraScanSupported: Boolean

/** Camera access for the scanner. [isPermanentlyDenied] means the system won't ask again, so [request] opens settings. */
class CameraPermissionState(
    val isGranted: Boolean,
    val isPermanentlyDenied: Boolean,
    val request: () -> Unit
)

@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState

/** A full-bleed camera preview that reports every food barcode it reads. */
@Composable
expect fun BarcodeCameraPreview(
    modifier: Modifier = Modifier,
    isTorchOn: Boolean,
    onBarcode: (String) -> Unit
)
