package org.thelazybattley.macrotrack.features.backup

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.features.backup.export.ExportData
import org.thelazybattley.macrotrack.features.backup.export.ExportEntryKind
import org.thelazybattley.macrotrack.features.backup.export.ExportFormat
import org.thelazybattley.macrotrack.features.backup.export.ExportItem
import org.thelazybattley.macrotrack.features.backup.export.ExportRange
import org.thelazybattley.macrotrack.features.backup.export.PdfLabels
import org.thelazybattley.macrotrack.features.backup.export.buildExport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportTest {

    private val today = LocalDate(2026, 10, 16)
    private val yogurt = Food(
        macros = FoodMacros(calories = 190, protein = 20.0, carbs = 8.0, fat = 10.0),
        name = "Greek yogurt",
        weight = 200.0,
        dominantMacro = MacroType.PROTEIN,
        unit = "g"
    )
    private val data = ExportData(
        logs = listOf(
            dummyFoodLog.copy(id = 1, foodName = "Greek yogurt", weight = 200.0, date = LocalDate(2026, 10, 15), mealType = MealType.BREAKFAST),
            dummyFoodLog.copy(id = 2, foodName = "Restaurant lunch", weight = 0.0, isQuickAdd = true, date = LocalDate(2026, 10, 15)),
            dummyFoodLog.copy(id = 3, foodName = "Greek yogurt", weight = 100.0, date = LocalDate(2025, 1, 2))
        ),
        weights = listOf(Weight(date = LocalDate(2026, 10, 1), weight = 75.0), Weight(date = LocalDate(2026, 10, 15), weight = 74.0)),
        foods = listOf(yogurt),
        goals = listOf(
            DailyGoal(date = LocalDate(2026, 9, 1), goal = Goal.MAINTAIN_WEIGHT, macroGoals = MacroGoals(calories = 2400, protein = 150, carbs = 280, fat = 70)),
            DailyGoal(date = LocalDate(2026, 9, 2), goal = Goal.MAINTAIN_WEIGHT, macroGoals = MacroGoals(calories = 2400, protein = 150, carbs = 280, fat = 70)),
            DailyGoal(date = LocalDate(2026, 10, 5), goal = Goal.LOSE_WEIGHT, macroGoals = MacroGoals(calories = 1900, protein = 150, carbs = 200, fat = 60))
        ),
        foodUnits = mapOf("Greek yogurt" to "g")
    )

    private fun export(format: ExportFormat, range: ExportRange = ExportRange.ALL_TIME, units: UnitSystem = UnitSystem.METRIC) = buildExport(
        data = data,
        items = ExportItem.entries.toSet(),
        range = range,
        format = format,
        unitSystem = units,
        today = today,
        pdfLabels = PdfLabels()
    )

    @Test
    fun csvZipsOneFilePerItemWithRowCounts() {
        val file = export(format = ExportFormat.CSV)
        assertEquals("macrotrack-all-time-2026-10-16.zip", file.fileName)
        assertEquals(
            listOf("food-log.csv" to 3, "weigh-ins.csv" to 2, "body-measurements.csv" to 0, "my-foods-recipes.csv" to 1, "goal-history.csv" to 2),
            file.entries.map { it.name to it.value }
        )
    }

    @Test
    fun rangeLeavesOutOlderEntries() {
        val file = export(format = ExportFormat.CSV, range = ExportRange.LAST_30_DAYS)
        assertEquals(2, file.entries.first { it.name == "food-log.csv" }.value)
        assertEquals(2, data.count(item = ExportItem.GOALS))
    }

    @Test
    fun jsonFollowsTheUnitSetting() {
        val text = export(format = ExportFormat.JSON, units = UnitSystem.IMPERIAL).bytes.decodeToString()
        assertTrue(text.contains("\"weight\": \"lb\""))
        // 74 kg is 163.14 lb; 200 g is 7.1 oz.
        assertTrue(text.contains("163.14"))
        assertTrue(text.contains("\"unit\": \"oz\""))
        assertTrue(text.contains("\"type\": \"quick_add\""))
    }

    @Test
    fun pdfListsItsSectionsByPage() {
        val file = export(format = ExportFormat.PDF)
        assertTrue(file.bytes.decodeToString().startsWith(prefix = "%PDF"))
        assertTrue(file.entries.all { it.kind == ExportEntryKind.PAGE })
        assertEquals(1, file.entries.first().value)
        // Foods and recipes stay out of the report.
        assertEquals(5, file.entries.size)
    }
}
