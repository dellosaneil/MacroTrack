package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

class LogRemindersTest {

    // Thursday, Oct 8, 2026.
    private val today = LocalDate(year = 2026, month = 10, day = 8)
    private val settings = LogReminderSettings(isOn = true, activeSince = today)

    private fun day(offset: Int) = today.plus(period = DatePeriod(days = offset))

    private fun log(date: LocalDate, mealType: MealType, loggedAtMillis: Long? = null) =
        dummyFoodLog.copy(date = date, mealType = mealType, loggedAtMillis = loggedAtMillis)

    private fun plan(
        settings: LogReminderSettings = this.settings,
        state: LogReminderState = LogReminderState(),
        from: LocalDateTime = today.at(minutes = 0),
        until: LocalDateTime = day(offset = 1).at(minutes = 0),
        foodLogs: List<FoodLog> = emptyList(),
        weights: List<Weight> = emptyList()
    ) = planLogReminders(settings = settings, state = state, from = from, until = until, foodLogs = foodLogs, weights = weights)

    @Test
    fun offSendsNothing() {
        assertEquals(emptyList(), plan(settings = settings.copy(isOn = false)))
    }

    @Test
    fun aDayWithNothingLoggedHasEveryReminderThatsOnInTimeOrder() {
        val reminders = plan()
        // Weight 9:00, breakfast 10:00, lunch 2:00 PM, dinner 8:30 PM; snacks are off by default.
        assertEquals(
            listOf(LogReminderKind.WEIGHT, LogReminderKind.BREAKFAST, LogReminderKind.LUNCH, LogReminderKind.DINNER),
            reminders.map { it.kind }
        )
        assertEquals(today.at(minutes = 9 * 60), reminders.first().at)
    }

    @Test
    fun aMealOrWeightAlreadyLoggedThatDayIsLeftOut() {
        val reminders = plan(
            foodLogs = listOf(log(date = today, mealType = MealType.BREAKFAST)),
            weights = listOf(Weight(date = today, weight = 74.4))
        )
        assertEquals(listOf(LogReminderKind.LUNCH, LogReminderKind.DINNER), reminders.map { it.kind })
    }

    @Test
    fun onlyRemindersAfterFromAndUpToUntilAreDue() {
        val reminders = plan(from = today.at(minutes = 10 * 60), until = today.at(minutes = 14 * 60))
        // Breakfast at exactly 10:00 was already handled; lunch at exactly 2:00 PM is due.
        assertEquals(listOf(LogReminderKind.LUNCH), reminders.map { it.kind })
    }

    @Test
    fun quietWeekendsSkipMealsButNotWeighIns() {
        val saturday = LocalDate(year = 2026, month = 10, day = 10)
        val reminders = plan(
            settings = settings.copy(isQuietOnWeekends = true),
            from = saturday.at(minutes = 0),
            until = saturday.plus(period = DatePeriod(days = 1)).at(minutes = 0)
        )
        assertEquals(listOf(LogReminderKind.WEIGHT), reminders.map { it.kind })
    }

    @Test
    fun weighInFollowsItsOwnDays() {
        // Thursday isn't picked.
        val reminders = plan(settings = settings.copy(weight = WeightReminder(days = setOf(1, 3, 5))))
        assertTrue(reminders.none { it.kind == LogReminderKind.WEIGHT })
    }

    @Test
    fun laterMovesTodaysWeighInOnly() {
        val laterAt = today.at(minutes = 10 * 60 + 5)
        val reminders = plan(state = LogReminderState(weightLaterAt = laterAt), until = day(offset = 2).at(minutes = 0))
        val weighIns = reminders.filter { it.kind == LogReminderKind.WEIGHT }.map { it.at }
        assertEquals(listOf(laterAt, day(offset = 1).at(minutes = 9 * 60)), weighIns)
    }

    @Test
    fun afterThreeDaysWithNothingLoggedRemindersPauseAndAskOnce() {
        // Last logged Monday: Tuesday to Thursday still remind, Friday asks, then nothing.
        val monday = LocalDate(year = 2026, month = 10, day = 5)
        val reminders = plan(
            settings = settings.copy(activeSince = monday.minus(period = DatePeriod(days = 10))),
            from = monday.at(minutes = 23 * 60),
            until = monday.plus(period = DatePeriod(days = 8)).at(minutes = 0),
            foodLogs = listOf(log(date = monday, mealType = MealType.LUNCH))
        )
        assertEquals(today, reminders.filter { it.kind != LogReminderKind.PAUSED }.maxOf { it.at.date })
        val paused = reminders.single { it.kind == LogReminderKind.PAUSED }
        // At the earliest reminder time: the 9:00 weigh-in.
        assertEquals(day(offset = 1).at(minutes = 9 * 60), paused.at)
        assertEquals(paused, reminders.last())
    }

    @Test
    fun withoutEasingOffRemindersKeepGoing() {
        val reminders = plan(
            settings = settings.copy(easesOff = false, activeSince = today.minus(period = DatePeriod(days = 30))),
            until = day(offset = 5).at(minutes = 0)
        )
        assertTrue(reminders.none { it.kind == LogReminderKind.PAUSED })
        assertEquals(day(offset = 4), reminders.maxOf { it.at.date })
    }

    @OptIn(ExperimentalTime::class)
    private fun loggedAt(date: LocalDate, minutes: Int) = date.at(minutes = minutes).toInstant(timeZone = TimeZone.UTC).toEpochMilliseconds()

    @Test
    fun usualTimeIsTheMiddleOfTheFirstLogEachDay() {
        val logs = listOf(
            log(date = day(offset = -1), mealType = MealType.LUNCH, loggedAtMillis = loggedAt(date = day(offset = -1), minutes = 12 * 60 + 40)),
            // A second lunch item later the same day doesn't count.
            log(date = day(offset = -1), mealType = MealType.LUNCH, loggedAtMillis = loggedAt(date = day(offset = -1), minutes = 15 * 60)),
            log(date = day(offset = -2), mealType = MealType.LUNCH, loggedAtMillis = loggedAt(date = day(offset = -2), minutes = 12 * 60 + 10)),
            log(date = day(offset = -3), mealType = MealType.LUNCH, loggedAtMillis = loggedAt(date = day(offset = -3), minutes = 13 * 60 + 2))
        )
        assertEquals(12 * 60 + 40, usualLogMinutes(foodLogs = logs, mealType = MealType.LUNCH, today = today, timeZone = TimeZone.UTC))
    }

    @Test
    fun usualTimeNeedsThreeDaysLoggedOnTheDay() {
        val logs = listOf(
            log(date = day(offset = -1), mealType = MealType.LUNCH, loggedAtMillis = loggedAt(date = day(offset = -1), minutes = 12 * 60)),
            log(date = day(offset = -2), mealType = MealType.LUNCH, loggedAtMillis = loggedAt(date = day(offset = -2), minutes = 12 * 60)),
            // Logged for a past day: when it was added says nothing about lunch.
            log(date = day(offset = -4), mealType = MealType.LUNCH, loggedAtMillis = loggedAt(date = day(offset = -3), minutes = 9 * 60)),
            log(date = day(offset = -5), mealType = MealType.LUNCH)
        )
        assertNull(usualLogMinutes(foodLogs = logs, mealType = MealType.LUNCH, today = today, timeZone = TimeZone.UTC))
    }

    @Test
    fun suggestionsCentreOnAnHourAfterTheUsualTime() {
        // Usually 12:40 PM: 1:40 PM rounds to 1:45 PM.
        assertEquals(listOf(795, 825, 855), suggestedReminderMinutes(kind = LogReminderKind.LUNCH, usualMinutes = 760))
        assertEquals(listOf(780, 840, 900), suggestedReminderMinutes(kind = LogReminderKind.LUNCH, usualMinutes = null))
    }
}
