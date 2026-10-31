package org.thelazybattley.macrotrack.core.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.encodeToPng(): ByteArray =
    Image.makeFromBitmap(bitmap = asSkiaBitmap()).encodeToData(format = EncodedImageFormat.PNG)?.bytes ?: ByteArray(size = 0)
