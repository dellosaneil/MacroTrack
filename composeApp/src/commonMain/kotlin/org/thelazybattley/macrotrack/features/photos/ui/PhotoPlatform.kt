package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The camera behind the progress photo screen. */
interface PhotoCamera {
    /** Whether the front camera is in use; switching swaps the preview. */
    var isFrontFacing: Boolean

    /** Takes a photo; [onResult] gets its encoded image, or null if it failed. Front-camera photos match the mirrored preview. */
    fun capture(onResult: (ByteArray?) -> Unit)
}

@Composable
expect fun rememberPhotoCamera(): PhotoCamera

/** A full-bleed live preview of [camera]. */
@Composable
expect fun PhotoCameraPreview(camera: PhotoCamera, modifier: Modifier = Modifier)

/** The system photo picker; only the photos picked are shared with the app. */
interface PhotoPicker {
    /** Lets the user pick up to [max] photos; [onPicked] gets their encoded images, empty if they cancel. */
    fun pick(max: Int, onPicked: (List<ByteArray>) -> Unit)
}

@Composable
expect fun rememberPhotoPicker(): PhotoPicker
