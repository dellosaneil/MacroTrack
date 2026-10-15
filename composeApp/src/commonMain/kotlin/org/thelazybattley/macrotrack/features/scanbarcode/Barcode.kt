package org.thelazybattley.macrotrack.features.scanbarcode

/**
 * Food packaging uses EAN-8, UPC-A (12), EAN-13 and GTIN-14 barcodes; anything else isn't worth looking up.
 * A UPC-A is kept at 12 digits: Android reads it that way, while iOS reads the same code as an EAN-13 with a
 * leading 0, and saved foods have to match whichever phone scanned them.
 */
fun normalizeBarcode(raw: String): String? {
    val digits = raw.filter(Char::isDigit).takeIf { it.length in 8..14 } ?: return null
    return if (digits.length == 13 && digits.first() == '0') digits.drop(n = 1) else digits
}

/** Groups an EAN-13 the way it's printed under the bars ("4 800016 642507"); other lengths stay as they are. */
fun formatBarcode(digits: String): String = if (digits.length == 13) {
    "${digits.first()} ${digits.substring(startIndex = 1, endIndex = 7)} ${digits.substring(startIndex = 7)}"
} else {
    digits
}
