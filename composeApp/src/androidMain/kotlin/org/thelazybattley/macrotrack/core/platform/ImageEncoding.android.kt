package org.thelazybattley.macrotrack.core.platform

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.encodeToPng(): ByteArray = ByteArrayOutputStream().use { stream ->
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
    stream.toByteArray()
}

/** Ignored for PNG, which is lossless. */
private const val PNG_QUALITY = 100
