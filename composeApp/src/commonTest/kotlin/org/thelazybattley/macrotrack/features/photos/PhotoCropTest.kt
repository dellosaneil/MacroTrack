package org.thelazybattley.macrotrack.features.photos

import org.thelazybattley.macrotrack.core.platform.CropRect
import kotlin.test.Test
import kotlin.test.assertEquals

class PhotoCropTest {

    private fun assertCrop(expected: CropRect, actual: CropRect) {
        assertEquals(expected.left, actual.left, absoluteTolerance = 0.001f)
        assertEquals(expected.top, actual.top, absoluteTolerance = 0.001f)
        assertEquals(expected.width, actual.width, absoluteTolerance = 0.001f)
        assertEquals(expected.height, actual.height, absoluteTolerance = 0.001f)
    }

    // A 1200 × 1200 square photo in a 300 × 400 frame shows 400 × 400, cut to the middle three quarters across.
    private val square = CropPlacement(imageWidth = 1200f, imageHeight = 1200f, frameWidth = 300f, frameHeight = 400f)

    @Test
    fun untouchedTheMiddleOfThePhotoIsKept() {
        assertCrop(CropRect(left = 0.125f, top = 0f, width = 0.75f, height = 1f), square.crop())
    }

    @Test
    fun aThreeByFourPhotoIsKeptWhole() {
        val photo = CropPlacement(imageWidth = 1200f, imageHeight = 1600f, frameWidth = 300f, frameHeight = 400f)
        assertCrop(CropRect(left = 0f, top = 0f, width = 1f, height = 1f), photo.crop())
    }

    @Test
    fun zoomingInKeepsLessOfThePhoto() {
        val zoomed = square.moved(factor = 2f)
        assertCrop(CropRect(left = 0.3125f, top = 0.25f, width = 0.375f, height = 0.5f), zoomed.crop())
    }

    @Test
    fun draggingStopsAtThePhotosEdge() {
        // Dragged right as far as it goes: the photo's left edge meets the frame's.
        val dragged = square.moved(panX = 500f, panY = 500f)
        assertEquals(50f, dragged.offsetX)
        assertEquals(0f, dragged.offsetY)
        assertCrop(CropRect(left = 0f, top = 0f, width = 0.75f, height = 1f), dragged.crop())
    }

    @Test
    fun zoomStaysBetweenWholeAndTheMost() {
        assertEquals(1f, square.moved(factor = 0.5f).zoom)
        assertEquals(CROP_MAX_ZOOM, square.moved(factor = 100f).zoom)
    }
}
