package org.thelazybattley.macrotrack.features.scanbarcode

/** Food packaging uses EAN-8, UPC-A (12), EAN-13 and GTIN-14 barcodes; anything else isn't worth looking up. */
fun normalizeBarcode(raw: String): String? = raw.filter(Char::isDigit).takeIf { it.length in 8..14 }

/** Groups an EAN-13 the way it's printed under the bars ("4 800016 642507"); other lengths stay as they are. */
fun formatBarcode(digits: String): String = if (digits.length == 13) {
    "${digits.first()} ${digits.substring(startIndex = 1, endIndex = 7)} ${digits.substring(startIndex = 7)}"
} else {
    digits
}
