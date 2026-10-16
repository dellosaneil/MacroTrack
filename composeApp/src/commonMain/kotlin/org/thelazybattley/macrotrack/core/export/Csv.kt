package org.thelazybattley.macrotrack.core.export

/** Builds a CSV file (RFC 4180): fields with commas, quotes or line breaks are quoted. */
class CsvWriter(header: List<String>) {
    private val builder = StringBuilder()
    var rowCount = 0
        private set

    init {
        appendLine(fields = header)
    }

    fun row(vararg fields: Any?) {
        appendLine(fields = fields.map { it?.toString().orEmpty() })
        rowCount++
    }

    /** UTF-8 with a byte order mark, so Excel reads accented food names correctly. */
    fun toByteArray(): ByteArray = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + builder.toString().encodeToByteArray()

    private fun appendLine(fields: List<String>) {
        fields.forEachIndexed { index, field ->
            if (index > 0) builder.append(',')
            if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
                builder.append('"').append(field.replace("\"", "\"\"")).append('"')
            } else {
                builder.append(field)
            }
        }
        builder.append("\r\n")
    }
}
