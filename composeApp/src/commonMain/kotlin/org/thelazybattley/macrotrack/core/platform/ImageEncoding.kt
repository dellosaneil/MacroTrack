package org.thelazybattley.macrotrack.core.platform

import androidx.compose.ui.graphics.ImageBitmap

/** The image as PNG bytes, for sharing or saving. */
expect fun ImageBitmap.encodeToPng(): ByteArray
