package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate

private const val TAG_EXIF_IFD = 0x8769
private const val TAG_DATE_TIME_ORIGINAL = 0x9003
private const val TAG_DATE_TIME = 0x0132

/**
 * The day a JPEG says it was taken, read from its EXIF "date taken" (or, failing that, its last-changed date). Null
 * when it isn't a JPEG or carries no date, so the caller falls back to today.
 */
fun jpegDateTaken(bytes: ByteArray): LocalDate? = runCatching { readJpegDate(bytes = bytes) }.getOrNull()

private fun readJpegDate(bytes: ByteArray): LocalDate? {
    if (bytes.size < 4 || bytes.u8(0) != 0xFF || bytes.u8(1) != 0xD8) return null
    var offset = 2
    while (offset + 4 <= bytes.size) {
        if (bytes.u8(offset) != 0xFF) return null
        val marker = bytes.u8(offset + 1)
        // Start of scan: the image data follows, and EXIF always comes before it.
        if (marker == 0xDA || marker == 0xD9) return null
        val length = (bytes.u8(offset + 2) shl 8) or bytes.u8(offset + 3)
        val start = offset + 4
        if (marker == 0xE1 && start + 6 <= bytes.size && bytes.decodeToString(startIndex = start, endIndex = start + 4) == "Exif") {
            return readTiffDate(bytes = bytes, tiff = start + 6)
        }
        offset += 2 + length
    }
    return null
}

private fun readTiffDate(bytes: ByteArray, tiff: Int): LocalDate? {
    val isLittleEndian = when (bytes.decodeToString(startIndex = tiff, endIndex = tiff + 2)) {
        "II" -> true
        "MM" -> false
        else -> return null
    }
    val reader = TiffReader(bytes = bytes, tiff = tiff, isLittleEndian = isLittleEndian)
    val ifd0 = tiff + reader.u32(at = tiff + 4)
    val exifIfd = reader.entryValue(ifd = ifd0, tag = TAG_EXIF_IFD)?.let { tiff + it }
    val original = exifIfd?.let { reader.asciiEntry(ifd = it, tag = TAG_DATE_TIME_ORIGINAL) }
    return parseExifDate(text = original ?: reader.asciiEntry(ifd = ifd0, tag = TAG_DATE_TIME) ?: return null)
}

/** "2026:10:18 07:42:10" → Oct 18, 2026. */
private fun parseExifDate(text: String): LocalDate? {
    val parts = text.take(n = 10).split(":")
    if (parts.size != 3) return null
    val (year, month, day) = parts.map { it.toIntOrNull() ?: return null }
    return runCatching { LocalDate(year = year, month = month, day = day) }.getOrNull()
}

private class TiffReader(val bytes: ByteArray, val tiff: Int, val isLittleEndian: Boolean) {

    fun u16(at: Int): Int = if (isLittleEndian) {
        bytes.u8(at) or (bytes.u8(at + 1) shl 8)
    } else {
        (bytes.u8(at) shl 8) or bytes.u8(at + 1)
    }

    fun u32(at: Int): Int = if (isLittleEndian) {
        u16(at) or (u16(at + 2) shl 16)
    } else {
        (u16(at) shl 16) or u16(at + 2)
    }

    /** Where [tag]'s entry starts in the directory at [ifd], or null when it has none. */
    private fun entry(ifd: Int, tag: Int): Int? {
        val count = u16(at = ifd)
        for (index in 0 until count) {
            val entry = ifd + 2 + index * 12
            if (entry + 12 > bytes.size) return null
            if (u16(at = entry) == tag) return entry
        }
        return null
    }

    fun entryValue(ifd: Int, tag: Int): Int? = entry(ifd = ifd, tag = tag)?.let { u32(at = it + 8) }

    fun asciiEntry(ifd: Int, tag: Int): String? {
        val entry = entry(ifd = ifd, tag = tag) ?: return null
        val length = u32(at = entry + 4)
        // Four bytes or fewer sit in the entry itself; longer text is elsewhere.
        val start = if (length <= 4) entry + 8 else tiff + u32(at = entry + 8)
        if (start < 0 || start + length > bytes.size) return null
        return bytes.decodeToString(startIndex = start, endIndex = start + length).trimEnd('\u0000', ' ')
    }
}

private fun ByteArray.u8(index: Int): Int = this[index].toInt() and 0xFF
