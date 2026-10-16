package org.thelazybattley.macrotrack.core.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportFormatsTest {

    @Test
    fun crcMatchesTheStandardCheckValue() {
        assertEquals(0xCBF43926.toInt(), crc32(bytes = "123456789".encodeToByteArray()))
    }

    @Test
    fun zipHasHeadersForEachFileAndAnEndRecord() {
        val zip = zipOf(entries = listOf(ZipEntry(name = "a.csv", bytes = "x".encodeToByteArray()), ZipEntry(name = "b.csv", bytes = ByteArray(0))))
        assertEquals(listOf(0x50, 0x4B, 0x03, 0x04), zip.take(n = 4).map { it.toInt() and 0xFF })
        // End of central directory: signature, then the entry counts.
        val end = zip.size - 22
        assertEquals(listOf(0x50, 0x4B, 0x05, 0x06), zip.slice(indices = end until end + 4).map { it.toInt() and 0xFF })
        assertEquals(2, zip[end + 10].toInt())
        assertTrue(zip.decodeToString().contains("b.csv"))
    }

    @Test
    fun csvQuotesFieldsThatNeedIt() {
        val csv = CsvWriter(header = listOf("name", "note")).apply {
            row("Chicken, roasted", "says \"hi\"")
            row("Rice", null)
        }
        val text = csv.toByteArray().drop(n = 3).toByteArray().decodeToString()
        assertEquals("name,note\r\n\"Chicken, roasted\",\"says \"\"hi\"\"\"\r\nRice,\r\n", text)
        assertEquals(2, csv.rowCount)
    }

    @Test
    fun pdfIsWellFormed() {
        val pdf = PdfWriter().apply {
            newPage()
            text(x = 40.0, y = 40.0, text = "Weight (kg) · 74.2")
            newPage()
            line(x1 = 0.0, y1 = 0.0, x2 = 10.0, y2 = 10.0)
        }.build().decodeToString()
        assertTrue(pdf.startsWith(prefix = "%PDF-1.4"))
        assertTrue(pdf.trimEnd().endsWith(suffix = "%%EOF"))
        assertTrue(pdf.contains("/Count 2"))
        // Parentheses in text are escaped so they don't end the string.
        assertTrue(pdf.contains("Weight \\(kg\\)"))
    }

    @Test
    fun longTextIsShortenedToFit() {
        val fitted = PdfWriter.fit(text = "A very long food name that will not fit", width = 60.0, size = 9.0)
        assertTrue(fitted.endsWith(suffix = "..."))
        assertTrue(PdfWriter.textWidth(text = fitted, size = 9.0) <= 60.0)
    }
}
