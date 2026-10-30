package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class UniqueFoodNameTest {

    @Test
    fun keepsTheNameWhenFree() {
        assertEquals(expected = "Skyr", actual = uniqueFoodName(name = "Skyr", brand = "North Fjord", barcode = "73110410", takenNames = listOf("Oats")))
    }

    @Test
    fun addsTheBrandThenTheBarcodeEnd() {
        assertEquals(
            expected = "Skyr (North Fjord)",
            actual = uniqueFoodName(name = "Skyr", brand = "North Fjord", barcode = "73110410", takenNames = listOf("skyr"))
        )
        assertEquals(
            expected = "Skyr (0410)",
            actual = uniqueFoodName(name = "Skyr", brand = "North Fjord", barcode = "73110410", takenNames = listOf("Skyr", "Skyr (North Fjord)"))
        )
    }
}
