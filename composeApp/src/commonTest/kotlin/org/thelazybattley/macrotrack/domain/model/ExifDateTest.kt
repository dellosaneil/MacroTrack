package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExifDateTest {

    /** A JPEG holding only an EXIF block: IFD0 points at an EXIF directory with DateTimeOriginal, and has [dateTime]. */
    private fun jpeg(isLittleEndian: Boolean, original: String?, dateTime: String? = null): ByteArray {
        val tiff = mutableListOf<Byte>()
        fun u16(value: Int) {
            val bytes = listOf(value and 0xFF, (value shr 8) and 0xFF)
            (if (isLittleEndian) bytes else bytes.reversed()).forEach { tiff += it.toByte() }
        }
        fun u32(value: Int) {
            val bytes = listOf(value and 0xFF, (value shr 8) and 0xFF, (value shr 16) and 0xFF, (value shr 24) and 0xFF)
            (if (isLittleEndian) bytes else bytes.reversed()).forEach { tiff += it.toByte() }
        }
        val ifd0Entries = listOfNotNull(0x8769 to "exif", dateTime?.let { 0x0132 to "dateTime" })
        val ifd0 = 8
        val exifIfd = ifd0 + 2 + ifd0Entries.size * 12 + 4
        val exifEntries = listOfNotNull(original?.let { 0x9003 })
        val textStart = exifIfd + 2 + exifEntries.size * 12 + 4
        val originalText = original?.plus("\u0000")?.encodeToByteArray()
        val dateTimeText = dateTime?.plus("\u0000")?.encodeToByteArray()
        val dateTimeAt = textStart + (originalText?.size ?: 0)
        tiff += (if (isLittleEndian) "II" else "MM").encodeToByteArray().toList()
        u16(value = 42)
        u32(value = ifd0)
        u16(value = ifd0Entries.size)
        ifd0Entries.forEach { (tag, kind) ->
            u16(value = tag)
            if (kind == "exif") {
                u16(value = 4)
                u32(value = 1)
                u32(value = exifIfd)
            } else {
                u16(value = 2)
                u32(value = dateTimeText!!.size)
                u32(value = dateTimeAt)
            }
        }
        u32(value = 0)
        u16(value = exifEntries.size)
        exifEntries.forEach { tag ->
            u16(value = tag)
            u16(value = 2)
            u32(value = originalText!!.size)
            u32(value = textStart)
        }
        u32(value = 0)
        originalText?.let { tiff += it.toList() }
        dateTimeText?.let { tiff += it.toList() }
        val app1 = "Exif".encodeToByteArray().toList() + listOf<Byte>(0, 0) + tiff
        val length = app1.size + 2
        return (listOf(0xFF, 0xD8, 0xFF, 0xE1, length shr 8, length and 0xFF).map { it.toByte() } + app1 +
            listOf(0xFF, 0xDA).map { it.toByte() }).toByteArray()
    }

    @Test
    fun readsTheDateTakenFromAnIntelOrderedPhoto() {
        assertEquals(LocalDate(2026, 10, 18), jpegDateTaken(bytes = jpeg(isLittleEndian = true, original = "2026:10:18 07:42:10")))
    }

    @Test
    fun readsTheDateTakenFromAMotorolaOrderedPhoto() {
        assertEquals(LocalDate(2025, 1, 3), jpegDateTaken(bytes = jpeg(isLittleEndian = false, original = "2025:01:03 21:00:00")))
    }

    @Test
    fun fallsBackToTheLastChangedDate() {
        assertEquals(
            LocalDate(2026, 9, 30),
            jpegDateTaken(bytes = jpeg(isLittleEndian = true, original = null, dateTime = "2026:09:30 12:00:00"))
        )
    }

    @Test
    fun anythingElseHasNoDate() {
        assertNull(jpegDateTaken(bytes = "not a photo".encodeToByteArray()))
        assertNull(jpegDateTaken(bytes = jpeg(isLittleEndian = true, original = "0000:00:00 00:00:00")))
        assertNull(jpegDateTaken(bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())))
    }
}
