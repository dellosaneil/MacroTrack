package org.thelazybattley.macrotrack.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.telemetry.NoOpTelemetry
import org.thelazybattley.macrotrack.domain.repository.ChecklistRepository
import org.thelazybattley.macrotrack.domain.usecase.checklist.ChecklistAction
import org.thelazybattley.macrotrack.domain.usecase.checklist.ReorderChecklistUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.UpdateChecklistDayUseCase
import org.thelazybattley.macrotrack.runSync
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChecklistTest {

    // A Thursday.
    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun day(daysAgo: Int) = today.minus(period = DatePeriod(days = daysAgo))

    private fun item(
        id: Long = 1,
        createdDaysAgo: Int = 30,
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        stock: Int? = null,
        amount: Double = 1.0,
        unit: SupplementUnit = SupplementUnit.CAPSULES,
        time: ChecklistTime = ChecklistTime.MORNING
    ) = ChecklistItem(
        id = id,
        kind = ChecklistKind.SUPPLEMENT,
        name = "Item $id",
        icon = ChecklistIcon.PILL,
        amount = amount,
        unit = unit,
        time = time,
        days = days,
        stock = stock,
        createdOn = day(daysAgo = createdDaysAgo)
    )

    private fun done(daysAgo: Int, id: Long = 1) = ChecklistEntry(itemId = id, date = day(daysAgo = daysAgo), status = ChecklistStatus.DONE)
    private fun skipped(daysAgo: Int, id: Long = 1) = ChecklistEntry(itemId = id, date = day(daysAgo = daysAgo), status = ChecklistStatus.SKIPPED)

    private fun checklist(items: List<ChecklistItem>, entries: List<ChecklistEntry>) =
        Checklist(today = today, items = items, entries = entries, settings = ChecklistSettings())

    @Test
    fun streakCountsDaysInARowAndTodayStillOpenDoesNotBreakIt() {
        val item = item()
        val list = checklist(items = listOf(item), entries = (1..5).map { done(daysAgo = it) })
        assertEquals(5, list.streakOf(item = item))
        assertEquals(DayMark.TO_DO, list.markOn(item = item, date = today))
        val withToday = checklist(items = listOf(item), entries = (0..5).map { done(daysAgo = it) })
        assertEquals(6, withToday.streakOf(item = item))
    }

    @Test
    fun skippedAndUnscheduledDaysKeepTheStreakButAMissBreaksIt() {
        val weekdays = item(days = ChecklistDays.WEEKDAYS.days!!)
        // Thu today; done Wed, Tue, skipped Mon, nothing due Sun and Sat, done Fri, missed Thu a week ago.
        val list = checklist(items = listOf(weekdays), entries = listOf(done(daysAgo = 1), done(daysAgo = 2), skipped(daysAgo = 3), done(daysAgo = 6)))
        assertEquals(3, list.streakOf(item = weekdays))
        assertEquals(DayMark.NOT_DUE, list.markOn(item = weekdays, date = day(daysAgo = 4)))
        assertEquals(DayMark.MISSED, list.markOn(item = weekdays, date = day(daysAgo = 7)))
    }

    @Test
    fun bestStreakIsTheLongestRun() {
        val item = item(createdDaysAgo = 12)
        val entries = (8..12).map { done(daysAgo = it) } + (1..3).map { done(daysAgo = it) }
        assertEquals(5, checklist(items = listOf(item), entries = entries).bestStreakOf(item = item))
    }

    @Test
    fun completionLeavesOutSkippedDaysAndSpotsWeekendMisses() {
        val item = item(createdDaysAgo = 13)
        // Saturdays and Sundays in the last two weeks: 4, 5, 11 and 12 days ago. Everything else done; one skipped.
        val weekend = setOf(4, 5, 11, 12)
        val entries = (1..13).filter { it !in weekend && it != 2 }.map { done(daysAgo = it) } + skipped(daysAgo = 2)
        val completion = checklist(items = listOf(item), entries = entries).completionOf(item = item, from = day(daysAgo = 13))
        assertEquals(8, completion.done)
        assertEquals(4, completion.missed)
        assertEquals(1, completion.skipped)
        assertTrue(completion.isMostlyWeekends)
        assertEquals(8f / 12, completion.share)
    }

    @Test
    fun summaryCountsDoneOutOfDueLessSkippedAndPointsToTheNextOpenOne() {
        val morning = item(id = 1)
        val evening = item(id = 2, time = ChecklistTime.BEDTIME)
        val skippedToday = item(id = 3, time = ChecklistTime.LUNCH)
        val list = checklist(items = listOf(evening, morning, skippedToday), entries = listOf(done(daysAgo = 0, id = 1), skipped(daysAgo = 0, id = 3)))
        val summary = list.summaryOn(date = today)
        assertEquals(1, summary.done)
        assertEquals(2, summary.total)
        assertEquals(2L, summary.next?.id)
        assertEquals(listOf(1L, 3L, 2L), summary.items.map { it.id })
    }

    @Test
    fun pausedItemsAreNotDueFromThePauseOn() {
        val paused = item().copy(pausedOn = day(daysAgo = 3))
        assertTrue(paused.isDueOn(date = day(daysAgo = 4)))
        assertFalse(paused.isDueOn(date = day(daysAgo = 3)))
        assertFalse(item(createdDaysAgo = 0).isDueOn(date = day(daysAgo = 1)))
    }

    @Test
    fun stockLastsByDoseAndSchedule() {
        val fishOil = item(stock = 6, amount = 2.0)
        assertEquals(2, fishOil.perDose)
        assertEquals(3, fishOil.daysLeft)
        assertTrue(fishOil.isLow)
        val magnesium = item(stock = 60, amount = 200.0, unit = SupplementUnit.MG, days = ChecklistDays.WEEKDAYS.days!!)
        assertEquals(1, magnesium.perDose)
        assertEquals(84, magnesium.daysLeft)
        assertFalse(magnesium.isLow)
    }

    @Test
    fun tickingTakesADoseFromStockAndUntickingPutsItBack() = runSync {
        val repository = FakeChecklistRepository(items = listOf(item(stock = 10, amount = 2.0, createdDaysAgo = 0)))
        val update = UpdateChecklistDayUseCase(repository = repository, telemetry = NoOpTelemetry)
        val date = getCurrentDate()
        update(itemId = 1, date = date, action = ChecklistAction.TAP)
        assertEquals(ChecklistStatus.DONE, repository.getEntry(itemId = 1, date = date)?.status)
        assertEquals(8, repository.getItem(id = 1)?.stock)
        update(itemId = 1, date = date, action = ChecklistAction.DONE)
        assertEquals(8, repository.getItem(id = 1)?.stock)
        update(itemId = 1, date = date, action = ChecklistAction.SKIP)
        assertEquals(ChecklistStatus.SKIPPED, repository.getEntry(itemId = 1, date = date)?.status)
        assertEquals(10, repository.getItem(id = 1)?.stock)
        update(itemId = 1, date = date, action = ChecklistAction.CLEAR)
        assertNull(repository.getEntry(itemId = 1, date = date))
    }

    @Test
    fun aTimesHabitIsDoneOnTheLastTap() = runSync {
        val habit = item(createdDaysAgo = 0).copy(kind = ChecklistKind.HABIT, goal = HabitGoal.TIMES, goalValue = 3)
        val repository = FakeChecklistRepository(items = listOf(habit))
        val update = UpdateChecklistDayUseCase(repository = repository, telemetry = NoOpTelemetry)
        val date = getCurrentDate()
        update(itemId = 1, date = date, action = ChecklistAction.TAP)
        update(itemId = 1, date = date, action = ChecklistAction.TAP)
        assertEquals(ChecklistEntry(itemId = 1, date = date, status = ChecklistStatus.PARTIAL, count = 2), repository.getEntry(itemId = 1, date = date))
        update(itemId = 1, date = date, action = ChecklistAction.TAP)
        assertEquals(ChecklistStatus.DONE, repository.getEntry(itemId = 1, date = date)?.status)
        update(itemId = 1, date = date, action = ChecklistAction.TAP)
        assertNull(repository.getEntry(itemId = 1, date = date))
    }

    @Test
    fun reorderPutsItemsInTheGivenOrder() = runSync {
        val repository = FakeChecklistRepository(items = listOf(item(id = 1), item(id = 2).copy(sortOrder = 1), item(id = 3).copy(sortOrder = 2)))
        ReorderChecklistUseCase(repository = repository)(orderedIds = listOf(3, 1))
        assertEquals(listOf(3L, 1L, 2L), repository.items.value.sortedBy { it.sortOrder }.map { it.id })
    }
}

private class FakeChecklistRepository(items: List<ChecklistItem>) : ChecklistRepository {
    val items = MutableStateFlow(value = items)
    private val entries = MutableStateFlow(value = emptyList<ChecklistEntry>())
    private val settings = MutableStateFlow(value = ChecklistSettings())

    override fun getItems(): Flow<List<ChecklistItem>> = items.map { list -> list.sortedBy { it.sortOrder } }
    override suspend fun getItem(id: Long): ChecklistItem? = items.value.firstOrNull { it.id == id }
    override suspend fun saveItem(item: ChecklistItem): Long {
        val id = if (item.id == 0L) (items.value.maxOfOrNull { it.id } ?: 0) + 1 else item.id
        items.value = items.value.filter { it.id != id } + item.copy(id = id)
        return id
    }
    override suspend fun saveItems(items: List<ChecklistItem>) {
        items.forEach { saveItem(item = it) }
    }
    override suspend fun deleteItem(id: Long) {
        items.value = items.value.filter { it.id != id }
    }
    override fun getEntries(): Flow<List<ChecklistEntry>> = entries
    override suspend fun getEntry(itemId: Long, date: LocalDate): ChecklistEntry? = entries.value.firstOrNull { it.itemId == itemId && it.date == date }
    override suspend fun saveEntry(entry: ChecklistEntry) {
        deleteEntry(itemId = entry.itemId, date = entry.date)
        entries.value += entry.copy(doneAtMillis = null)
    }
    override suspend fun deleteEntry(itemId: Long, date: LocalDate) {
        entries.value = entries.value.filterNot { it.itemId == itemId && it.date == date }
    }
    override fun getSettings(): Flow<ChecklistSettings> = settings
    override suspend fun saveSettings(settings: ChecklistSettings) {
        this.settings.value = settings
    }
}
