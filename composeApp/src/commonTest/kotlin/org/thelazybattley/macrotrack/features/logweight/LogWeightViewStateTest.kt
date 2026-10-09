package org.thelazybattley.macrotrack.features.logweight

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.Weight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LogWeightViewStateTest {

    private val weights = listOf(
        Weight(date = LocalDate(2026, 9, 28), weight = 75.0),
        Weight(date = LocalDate(2026, 10, 1), weight = 74.8),
        Weight(date = LocalDate(2026, 10, 5), weight = 74.5),
        Weight(date = LocalDate(2026, 10, 8), weight = 74.2)
    )

    @Test
    fun newDateComparesWithMostRecentEarlierEntry() {
        val state = LogWeightViewState(date = LocalDate(2026, 10, 9), input = "74", weights = weights)
        assertEquals(74.2, state.previous?.weight)
        assertNull(state.existing)
        assertEquals(LocalDate(2026, 10, 1), state.monthBaseline?.date)
        assertEquals(74.0, state.weight)
    }

    @Test
    fun backfilledDateUsesEntriesBeforeIt() {
        val state = LogWeightViewState(date = LocalDate(2026, 10, 5), input = "74.6", weights = weights)
        assertEquals(74.8, state.previous?.weight)
        assertEquals(74.5, state.existing?.weight)
        assertEquals(LocalDate(2026, 10, 1), state.monthBaseline?.date)
    }

    @Test
    fun invalidInputHasNoWeight() {
        assertNull(LogWeightViewState(input = "").weight)
        assertNull(LogWeightViewState(input = "0").weight)
    }
}
