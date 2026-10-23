package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeekBalanceTest {

    // Thursday, Oct 8, 2026; the week is Mon, Oct 5 – Sun, Oct 11.
    private val today = LocalDate(year = 2026, month = 10, day = 8)
    private val monday = LocalDate(year = 2026, month = 10, day = 5)

    private fun day(dayOfMonth: Int) = LocalDate(year = 2026, month = 10, day = dayOfMonth)

    @Test
    fun weeksStartOnMonday() {
        assertEquals(monday, today.weekStart())
        assertEquals(monday, monday.weekStart())
        assertEquals(monday, day(dayOfMonth = 11).weekStart())
    }

    @Test
    fun finishedLoggedDaysAddUpAndTodayWaitsUntilItEnds() {
        val balance = weekBalance(
            weekStart = monday,
            today = today,
            intake = mapOf(day(5) to 2350, day(6) to 2050, day(7) to 2090, day(8) to 1100),
            targetFor = { 2150 }
        )
        // +200, −100, −60; today's 1,100 doesn't count yet.
        assertEquals(40, balance.total)
        assertEquals(3, balance.countedDays)
        assertEquals(100, balance.runningTotal(index = 1))
        assertEquals(WeekDayStatus.TODAY, balance.days[3].status)
        assertEquals(WeekDayStatus.UPCOMING, balance.days[4].status)
        assertTrue(balance.isThisWeek)
        assertFalse(balance.hasNotLoggedDays)
        // Today and the three days after it share the 40 kcal back.
        assertEquals(4, balance.daysLeft)
        assertEquals(10, balance.evenPerDay)
    }

    @Test
    fun daysNotLoggedAreLeftOut() {
        val lastMonday = day(dayOfMonth = 5)
        val balance = weekBalance(
            weekStart = lastMonday,
            today = day(dayOfMonth = 14),
            intake = mapOf(day(5) to 2520, day(6) to 2380),
            targetFor = { 2450 }
        )
        assertEquals(0, balance.total)
        assertEquals(2, balance.countedDays)
        assertTrue(balance.hasNotLoggedDays)
        assertFalse(balance.isThisWeek)
        assertNull(balance.evenPerDay)
    }

    @Test
    fun eachDayUsesItsOwnTarget() {
        val balance = weekBalance(
            weekStart = monday,
            today = today,
            intake = mapOf(day(5) to 2450, day(6) to 2150),
            targetFor = { date -> if (date < day(6)) 2450 else 2150 }
        )
        assertEquals(0, balance.total)
    }
}
