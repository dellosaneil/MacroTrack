package org.thelazybattley.macrotrack.core.export

/** One file inside a zip. */
class ZipEntry(val name: String, val bytes: ByteArray)

/**
 * Writes a zip of [entries] without compression ("stored"), which every unzip tool and phone opens. Exports are
 * small text files, so compressing them isn't worth the extra code.
 */
fun zipOf(entries: List<ZipEntry>): ByteArray {
    val out = ByteSink()
    val central = ByteSink()
    entries.forEach { entry ->
        val name = entry.name.encodeToByteArray()
        val crc = crc32(bytes = entry.bytes)
        val offset = out.size
        // Local file header
        out.int(0x04034b50); out.short(20); out.short(0x0800); out.short(0); out.short(0); out.short(0x21)
        out.int(crc); out.int(entry.bytes.size); out.int(entry.bytes.size); out.short(name.size); out.short(0)
        out.bytes(name); out.bytes(entry.bytes)
        // Central directory record
        central.int(0x02014b50); central.short(20); central.short(20); central.short(0x0800); central.short(0)
        central.short(0); central.short(0x21); central.int(crc); central.int(entry.bytes.size); central.int(entry.bytes.size)
        central.short(name.size); central.short(0); central.short(0); central.short(0); central.short(0); central.int(0)
        central.int(offset); central.bytes(name)
    }
    val centralOffset = out.size
    val centralBytes = central.toByteArray()
    out.bytes(centralBytes)
    // End of central directory
    out.int(0x06054b50); out.short(0); out.short(0); out.short(entries.size); out.short(entries.size)
    out.int(centralBytes.size); out.int(centralOffset); out.short(0)
    return out.toByteArray()
}

private val crcTable = IntArray(size = 256) { n ->
    var c = n
    repeat(times = 8) { c = if (c and 1 != 0) (0xEDB88320.toInt() xor (c ushr 1)) else c ushr 1 }
    c
}

fun crc32(bytes: ByteArray): Int {
    var crc = -1
    bytes.forEach { crc = crcTable[(crc xor it.toInt()) and 0xFF] xor (crc ushr 8) }
    return crc.inv()
}

/** A growable little-endian byte buffer. */
internal class ByteSink {
    private var buffer = ByteArray(size = 1024)
    var size = 0
        private set

    fun byte(value: Int) {
        if (size == buffer.size) buffer = buffer.copyOf(newSize = buffer.size * 2)
        buffer[size++] = value.toByte()
    }

    fun short(value: Int) {
        byte(value and 0xFF); byte((value shr 8) and 0xFF)
    }

    fun int(value: Int) {
        short(value and 0xFFFF); short((value ushr 16) and 0xFFFF)
    }

    fun bytes(value: ByteArray) {
        if (size + value.size > buffer.size) buffer = buffer.copyOf(newSize = maxOf(buffer.size * 2, size + value.size))
        value.copyInto(destination = buffer, destinationOffset = size)
        size += value.size
    }

    fun toByteArray(): ByteArray = buffer.copyOf(newSize = size)
}
