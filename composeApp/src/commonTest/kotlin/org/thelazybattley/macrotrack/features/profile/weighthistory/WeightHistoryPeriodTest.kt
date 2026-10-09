package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.longLabel
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.shortLabel
import kotlin.test.Test
import kotlin.test.assertEquals

class WeightHistoryPeriodTest {

    // Thursday
    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun period(range: WeightHistoryTimeRangeEnum) =
        periodContaining(range = range, date = today, earliestEntry = LocalDate(2026, 7, 6))

    @Test
    fun weekRunsMondayToSunday() {
        val week = period(WeightHistoryTimeRangeEnum.WEEK)
        assertEquals(LocalDate(2026, 10, 5), week.start)
        assertEquals(LocalDate(2026, 10, 11), week.end)
        assertEquals("Oct 5 – 11, 2026", week.longLabel())

        val previous = week.shift(steps = -1)
        assertEquals(LocalDate(2026, 9, 28), previous.start)
        assertEquals("Sep 28 – Oct 4", previous.shortLabel())
    }

    @Test
    fun monthCoversTheCalendarMonth() {
        val month = period(WeightHistoryTimeRangeEnum.MONTH)
        assertEquals(LocalDate(2026, 10, 1), month.start)
        assertEquals(LocalDate(2026, 10, 31), month.end)
        assertEquals("October 2026", month.longLabel())

        val previous = month.shift(steps = -1)
        assertEquals(LocalDate(2026, 9, 30), previous.end)
        assertEquals("September", previous.shortLabel())
    }

    @Test
    fun threeMonthsEndsWithTheCurrentMonth() {
        val quarter = period(WeightHistoryTimeRangeEnum.THREE_MONTHS)
        assertEquals(LocalDate(2026, 8, 1), quarter.start)
        assertEquals(LocalDate(2026, 10, 31), quarter.end)
        assertEquals("Aug – Oct 2026", quarter.longLabel())

        val previous = quarter.shift(steps = -1)
        assertEquals(LocalDate(2026, 5, 1), previous.start)
        assertEquals(LocalDate(2026, 7, 31), previous.end)
        assertEquals("May – Jul", previous.shortLabel())
    }

    @Test
    fun threeMonthsAcrossYearsShowsBothYears() {
        val quarter = periodContaining(
            range = WeightHistoryTimeRangeEnum.THREE_MONTHS,
            date = LocalDate(2026, 1, 15),
            earliestEntry = null
        )
        assertEquals(LocalDate(2025, 11, 1), quarter.start)
        assertEquals("Nov 2025 – Jan 2026", quarter.longLabel())
    }

    @Test
    fun allStartsAtFirstEntryAndNeverShifts() {
        val all = period(WeightHistoryTimeRangeEnum.ALL)
        assertEquals(LocalDate(2026, 7, 6), all.start)
        assertEquals(today, all.end)
        assertEquals(all, all.shift(steps = -1))
    }

    @Test
    fun inPeriodFiltersAndSorts() {
        val weights = listOf(
            Weight(date = LocalDate(2026, 10, 8), weight = 74.2),
            Weight(date = LocalDate(2026, 9, 30), weight = 74.9),
            Weight(date = LocalDate(2026, 10, 1), weight = 74.8)
        )
        val inMonth = weights.inPeriod(period(WeightHistoryTimeRangeEnum.MONTH))
        assertEquals(listOf(74.8, 74.2), inMonth.map { it.weight })
    }
}
