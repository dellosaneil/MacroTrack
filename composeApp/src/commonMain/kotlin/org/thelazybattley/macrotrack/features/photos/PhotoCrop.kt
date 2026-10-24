package org.thelazybattley.macrotrack.features.photos

import org.thelazybattley.macrotrack.core.platform.CropRect
import kotlin.math.max

/** Photos are cropped to the 3:4 the albums show them in, so they line up with the ghost and with each other. */
const val CROP_ASPECT = 3f / 4f

/** How far in the crop can zoom. */
const val CROP_MAX_ZOOM = 5f

/**
 * An image laid over a crop frame: scaled just enough to cover the frame, then zoomed by [zoom] (1 or more) and
 * moved [offsetX], [offsetY] from centred, all in the frame's pixels.
 */
data class CropPlacement(
    val imageWidth: Float,
    val imageHeight: Float,
    val frameWidth: Float,
    val frameHeight: Float,
    val zoom: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) {
    private val coverScale: Float get() = max(frameWidth / imageWidth, frameHeight / imageHeight)

    val shownWidth: Float get() = imageWidth * coverScale * zoom

    val shownHeight: Float get() = imageHeight * coverScale * zoom

    /** Zoomed by [factor] and moved by [panX], [panY], kept so the image always fills the frame. */
    fun moved(factor: Float = 1f, panX: Float = 0f, panY: Float = 0f): CropPlacement {
        val next = copy(zoom = (zoom * factor).coerceIn(minimumValue = 1f, maximumValue = CROP_MAX_ZOOM))
        return next.copy(
            offsetX = (offsetX + panX).coerceIn(range = next.slackX()),
            offsetY = (offsetY + panY).coerceIn(range = next.slackY())
        )
    }

    /** The part of the image inside the frame. */
    fun crop(): CropRect {
        val left = (shownWidth - frameWidth) / 2 - offsetX
        val top = (shownHeight - frameHeight) / 2 - offsetY
        return CropRect(
            left = (left / shownWidth).coerceIn(minimumValue = 0f, maximumValue = 1f),
            top = (top / shownHeight).coerceIn(minimumValue = 0f, maximumValue = 1f),
            width = (frameWidth / shownWidth).coerceAtMost(maximumValue = 1f),
            height = (frameHeight / shownHeight).coerceAtMost(maximumValue = 1f)
        )
    }

    private fun slackX(): ClosedFloatingPointRange<Float> = ((shownWidth - frameWidth) / 2).let { -it..it }

    private fun slackY(): ClosedFloatingPointRange<Float> = ((shownHeight - frameHeight) / 2).let { -it..it }
}
