package org.thelazybattley.macrotrack.core.export

import kotlin.math.roundToInt

/** An RGB colour for PDF drawing, each part 0–1. */
data class PdfColor(val red: Double, val green: Double, val blue: Double) {
    internal fun operands(): String = "${red.pdf()} ${green.pdf()} ${blue.pdf()}"

    companion object {
        val Ink = PdfColor(red = 0.08, green = 0.09, blue = 0.10)
        val Muted = PdfColor(red = 0.37, green = 0.38, blue = 0.41)
        val Line = PdfColor(red = 0.89, green = 0.89, blue = 0.85)
        val Accent = PdfColor(red = 0.05, green = 0.43, blue = 0.32)
        val Hero = PdfColor(red = 0.89, green = 0.95, blue = 0.92)
    }
}

/**
 * A minimal PDF writer: A4 pages with text in the built-in Helvetica fonts, lines and filled rectangles. Positions
 * are in points from the page's top-left corner. Text is limited to what Windows-1252 can show; other characters
 * become "?".
 */
class PdfWriter {
    private val pages = mutableListOf<StringBuilder>()
    private val current: StringBuilder get() = pages.last()

    val pageCount: Int get() = pages.size

    fun newPage() {
        pages.add(StringBuilder())
    }

    fun text(x: Double, y: Double, text: String, size: Double = 10.0, bold: Boolean = false, color: PdfColor = PdfColor.Ink) {
        val font = if (bold) "F2" else "F1"
        current.append("BT /$font ${size.pdf()} Tf ${color.operands()} rg ${x.pdf()} ${flip(y = y).pdf()} Td (")
            .append(escape(text = text)).append(") Tj ET\n")
    }

    /** Text ending at [right], for numbers in table columns. */
    fun textRight(right: Double, y: Double, text: String, size: Double = 10.0, bold: Boolean = false, color: PdfColor = PdfColor.Ink) {
        text(x = right - textWidth(text = text, size = size, bold = bold), y = y, text = text, size = size, bold = bold, color = color)
    }

    fun line(x1: Double, y1: Double, x2: Double, y2: Double, width: Double = 0.75, color: PdfColor = PdfColor.Line) {
        current.append("${width.pdf()} w ${color.operands()} RG ${x1.pdf()} ${flip(y = y1).pdf()} m ${x2.pdf()} ${flip(y = y2).pdf()} l S\n")
    }

    fun polyline(points: List<Pair<Double, Double>>, width: Double = 1.5, color: PdfColor = PdfColor.Accent) {
        if (points.size < 2) return
        current.append("${width.pdf()} w 1 J 1 j ${color.operands()} RG ")
        points.forEachIndexed { index, (x, y) ->
            current.append("${x.pdf()} ${flip(y = y).pdf()} ").append(if (index == 0) "m " else "l ")
        }
        current.append("S\n")
    }

    fun fillRect(x: Double, y: Double, width: Double, height: Double, color: PdfColor) {
        current.append("${color.operands()} rg ${x.pdf()} ${flip(y = y + height).pdf()} ${width.pdf()} ${height.pdf()} re f\n")
    }

    fun build(): ByteArray {
        if (pages.isEmpty()) newPage()
        val out = ByteSink()
        val offsets = mutableListOf<Int>()
        fun obj(body: String) {
            offsets.add(out.size)
            out.bytes("${offsets.size} 0 obj\n$body\nendobj\n".encodeLatin1())
        }
        out.bytes("%PDF-1.4\n".encodeLatin1())
        // 1 catalog, 2 page tree, 3–4 fonts, then a page and its content stream for each page.
        val pageIds = pages.indices.map { 5 + it * 2 }
        obj(body = "<< /Type /Catalog /Pages 2 0 R >>")
        obj(body = "<< /Type /Pages /Kids [${pageIds.joinToString(separator = " ") { "$it 0 R" }}] /Count ${pages.size} >>")
        obj(body = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>")
        obj(body = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>")
        pages.forEachIndexed { index, content ->
            obj(
                body = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $PAGE_WIDTH $PAGE_HEIGHT] " +
                    "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents ${pageIds[index] + 1} 0 R >>"
            )
            val stream = content.toString().encodeLatin1()
            offsets.add(out.size)
            out.bytes("${offsets.size} 0 obj\n<< /Length ${stream.size} >>\nstream\n".encodeLatin1())
            out.bytes(stream)
            out.bytes("\nendstream\nendobj\n".encodeLatin1())
        }
        val xref = out.size
        out.bytes("xref\n0 ${offsets.size + 1}\n0000000000 65535 f \n".encodeLatin1())
        offsets.forEach { out.bytes("${it.toString().padStart(length = 10, padChar = '0')} 00000 n \n".encodeLatin1()) }
        out.bytes("trailer\n<< /Size ${offsets.size + 1} /Root 1 0 R >>\nstartxref\n$xref\n%%EOF\n".encodeLatin1())
        return out.toByteArray()
    }

    private fun flip(y: Double): Double = PAGE_HEIGHT - y

    companion object {
        const val PAGE_WIDTH = 595.0
        const val PAGE_HEIGHT = 842.0

        /** Width of [text] in Helvetica, close enough to right-align and fit columns. */
        fun textWidth(text: String, size: Double, bold: Boolean = false): Double =
            text.sumOf { charWidth(char = it, bold = bold) } * size / 1000

        /** Shortens [text] with "..." so it fits in [width]. */
        fun fit(text: String, width: Double, size: Double, bold: Boolean = false): String {
            if (textWidth(text = text, size = size, bold = bold) <= width) return text
            var end = text.length
            while (end > 0 && textWidth(text = text.take(n = end) + "...", size = size, bold = bold) > width) end--
            return text.take(n = end).trimEnd() + "..."
        }

        private fun charWidth(char: Char, bold: Boolean): Int = when {
            char.isDigit() -> 556
            char == ' ' || char == '.' || char == ',' || char == ':' || char == '·' -> 278
            char == 'i' || char == 'l' || char == 'j' || char == '|' || char == '\'' -> 222 + if (bold) 56 else 0
            char == 'm' || char == 'w' -> 833
            char == 'M' || char == 'W' -> 860
            char.isUpperCase() -> 667 + if (bold) 55 else 0
            else -> 520 + if (bold) 40 else 0
        }
    }
}

private fun Double.pdf(): String {
    val hundredths = (this * 100).roundToInt()
    val sign = if (hundredths < 0) "-" else ""
    val abs = kotlin.math.abs(hundredths)
    return "$sign${abs / 100}.${(abs % 100).toString().padStart(length = 2, padChar = '0')}"
}

/** Characters outside Latin-1 that Windows-1252 still has. */
private val winAnsiExtras = mapOf(
    '€' to 0x80, '‚' to 0x82, '„' to 0x84, '…' to 0x85, '‘' to 0x91, '’' to 0x92, '“' to 0x93, '”' to 0x94,
    '•' to 0x95, '–' to 0x96, '—' to 0x97, '™' to 0x99, '−' to '-'.code, '↑' to '^'.code, '↓' to 'v'.code
)

private fun escape(text: String): String = buildString {
    text.forEach { char ->
        when (char) {
            '(', ')', '\\' -> append('\\').append(char)
            else -> append(char)
        }
    }
}

private fun String.encodeLatin1(): ByteArray = ByteArray(size = length) { index ->
    val char = this[index]
    when {
        char.code < 0x80 -> char.code.toByte()
        char.code in 0xA0..0xFF -> char.code.toByte()
        else -> (winAnsiExtras[char] ?: '?'.code).toByte()
    }
}
