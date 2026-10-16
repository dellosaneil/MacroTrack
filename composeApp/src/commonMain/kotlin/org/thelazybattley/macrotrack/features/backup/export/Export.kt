package org.thelazybattley.macrotrack.features.backup.export

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.thelazybattley.macrotrack.core.export.CsvWriter
import org.thelazybattley.macrotrack.core.export.ZipEntry
import org.thelazybattley.macrotrack.core.export.zipOf
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.Weight
import kotlin.math.round

enum class ExportItem(val fileStem: String) {
    FOOD_LOG(fileStem = "food-log"),
    WEIGH_INS(fileStem = "weigh-ins"),
    MEASUREMENTS(fileStem = "body-measurements"),
    FOODS(fileStem = "my-foods-recipes"),
    GOALS(fileStem = "goal-history")
}

enum class ExportRange(val fileLabel: String) {
    LAST_30_DAYS(fileLabel = "last-30-days"),
    LAST_90_DAYS(fileLabel = "last-90-days"),
    THIS_YEAR(fileLabel = "this-year"),
    ALL_TIME(fileLabel = "all-time");

    /** The first day included, or null for everything. */
    fun startDate(today: LocalDate): LocalDate? = when (this) {
        LAST_30_DAYS -> today.minus(period = DatePeriod(days = 29))
        LAST_90_DAYS -> today.minus(period = DatePeriod(days = 89))
        THIS_YEAR -> LocalDate(year = today.year, month = 1, day = 1)
        ALL_TIME -> null
    }
}

enum class ExportFormat(val extension: String, val mimeType: String) {
    CSV(extension = "zip", mimeType = "application/zip"),
    PDF(extension = "pdf", mimeType = "application/pdf"),
    JSON(extension = "json", mimeType = "application/json")
}

/** Everything an export can draw from. [foodUnits] maps a logged name to its food's unit. */
data class ExportData(
    val logs: List<FoodLog> = emptyList(),
    val weights: List<Weight> = emptyList(),
    val measurements: List<Measurement> = emptyList(),
    val foods: List<Food> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val goals: List<DailyGoal> = emptyList(),
    val foodUnits: Map<String, String> = emptyMap()
) {
    /** Only what falls in [range]; saved foods and recipes have no date, so they're always included. */
    fun inRange(range: ExportRange, today: LocalDate): ExportData {
        val start = range.startDate(today = today) ?: return this
        return copy(
            logs = logs.filter { it.date >= start },
            weights = weights.filter { it.date >= start },
            measurements = measurements.filter { it.date >= start },
            goals = goalChanges().filter { change -> (change.end ?: today) >= start }.map { it.goal }
        )
    }

    /** Goal records with neighbours that kept the same goal and targets merged into one change. */
    fun goalChanges(): List<GoalChange> {
        val sorted = goals.sortedBy { it.date }
        val merged = sorted.filterIndexed { index, record ->
            index == 0 || sorted[index - 1].let { it.goal != record.goal || it.macroGoals != record.macroGoals }
        }
        return merged.mapIndexed { index, record ->
            GoalChange(goal = record, end = merged.getOrNull(index = index + 1)?.date?.minus(period = DatePeriod(days = 1)))
        }
    }

    fun count(item: ExportItem): Int = when (item) {
        ExportItem.FOOD_LOG -> logs.size
        ExportItem.WEIGH_INS -> weights.size
        ExportItem.MEASUREMENTS -> measurements.size
        ExportItem.FOODS -> foods.size + recipes.size
        ExportItem.GOALS -> goalChanges().size
    }
}

/** A goal and the last day it applied; null [end] means it still applies. */
data class GoalChange(val goal: DailyGoal, val end: LocalDate?)

/** How an exported file lists one of its parts. */
enum class ExportEntryKind { ROWS, PAGE, SECTIONS }

data class ExportEntry(val name: String, val kind: ExportEntryKind, val value: Int)

data class ExportFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val entries: List<ExportEntry>
)

/**
 * Builds an export of [items] from [data] for [range] in [format]. Weights, lengths and portions follow
 * [unitSystem]. [pdfLabels] gives the report its words; the CSV and JSON files use fixed English column names
 * so spreadsheets and other apps can read them.
 */
fun buildExport(
    data: ExportData,
    items: Set<ExportItem>,
    range: ExportRange,
    format: ExportFormat,
    unitSystem: UnitSystem,
    today: LocalDate,
    pdfLabels: PdfLabels
): ExportFile {
    val ranged = data.inRange(range = range, today = today)
    val fileName = "macrotrack-${range.fileLabel}-$today.${format.extension}"
    val ordered = ExportItem.entries.filter { it in items }
    return when (format) {
        ExportFormat.CSV -> {
            val files = ordered.map { item -> item to csvFor(item = item, data = ranged, unitSystem = unitSystem) }
            ExportFile(
                fileName = fileName,
                mimeType = format.mimeType,
                bytes = zipOf(entries = files.map { (item, csv) -> ZipEntry(name = "${item.fileStem}.csv", bytes = csv.toByteArray()) }),
                entries = files.map { (item, csv) -> ExportEntry(name = "${item.fileStem}.csv", kind = ExportEntryKind.ROWS, value = csv.rowCount) }
            )
        }
        ExportFormat.JSON -> ExportFile(
            fileName = fileName,
            mimeType = format.mimeType,
            bytes = json.encodeToString(
                serializer = JsonObject.serializer(),
                value = jsonFor(items = ordered, data = ranged, range = range, unitSystem = unitSystem, today = today)
            ).encodeToByteArray(),
            entries = listOf(ExportEntry(name = fileName, kind = ExportEntryKind.SECTIONS, value = ordered.size))
        )
        ExportFormat.PDF -> {
            val report = buildPdfReport(items = ordered, data = ranged, unitSystem = unitSystem, labels = pdfLabels)
            ExportFile(fileName = fileName, mimeType = format.mimeType, bytes = report.bytes, entries = report.entries)
        }
    }
}

private val json = Json { prettyPrint = true }

private fun csvFor(item: ExportItem, data: ExportData, unitSystem: UnitSystem): CsvWriter = when (item) {
    ExportItem.FOOD_LOG -> CsvWriter(
        header = listOf("date", "meal", "food", "type", "amount", "unit", "calories", "protein_g", "carbs_g", "fat_g")
    ).apply {
        data.logs.sortedWith(compareBy<FoodLog> { it.date }.thenBy { it.mealType.ordinal }.thenBy { it.id }).forEach { log ->
            val portion = log.portion(data = data, unitSystem = unitSystem)
            row(
                log.date, log.mealType.name.lowercase(), log.foodName, log.kind(),
                portion?.first, portion?.second, log.calories, log.protein.oneDecimal(), log.carbs.oneDecimal(), log.fat.oneDecimal()
            )
        }
    }
    ExportItem.WEIGH_INS -> CsvWriter(header = listOf("date", "weight_${unitSystem.weightLabel()}")).apply {
        data.weights.sortedBy { it.date }.forEach { row(it.date, unitSystem.weightFromKg(kg = it.weight).twoDecimals()) }
    }
    ExportItem.MEASUREMENTS -> CsvWriter(header = listOf("date", "measurement", "value_${unitSystem.lengthLabel()}", "note")).apply {
        data.measurements.sortedWith(compareBy<Measurement> { it.date }.thenBy { it.type.ordinal }).forEach {
            row(it.date, it.type.name.lowercase(), unitSystem.lengthFromCm(cm = it.valueCm).twoDecimals(), it.note)
        }
    }
    ExportItem.FOODS -> CsvWriter(
        header = listOf("type", "name", "serving_amount", "serving_unit", "calories", "protein_g", "carbs_g", "fat_g", "barcode", "ingredients")
    ).apply {
        data.foods.sortedBy { it.name.lowercase() }.forEach { food ->
            val unit = unitSystem.portionUnit(storedUnit = food.unit)
            row(
                "food", food.name, unit.format(amount = food.weight), unit.label, food.macros.calories,
                food.macros.protein.oneDecimal(), food.macros.carbs.oneDecimal(), food.macros.fat.oneDecimal(), food.barcode, null
            )
        }
        val foodsByName = data.foods.associateBy { it.name }
        data.recipes.sortedBy { it.name.lowercase() }.forEach { recipe ->
            val totals = recipe.totals(foodsByName = foodsByName)
            val ingredients = recipe.ingredients.joinToString(separator = "; ") { ingredient ->
                val unit = unitSystem.portionUnit(storedUnit = foodsByName[ingredient.name]?.unit.orEmpty())
                "${ingredient.name} ${unit.format(amount = ingredient.weight)} ${unit.label}".trim()
            }
            row(
                "recipe", recipe.name, 1, "serving", totals.calories, totals.protein.oneDecimal(), totals.carbs.oneDecimal(),
                totals.fat.oneDecimal(), null, ingredients
            )
        }
    }
    ExportItem.GOALS -> CsvWriter(header = listOf("from", "to", "goal", "calories", "protein_g", "carbs_g", "fat_g")).apply {
        data.goalChanges().forEach { change ->
            val goal = change.goal
            row(
                goal.date, change.end, goal.goal.name.lowercase(), goal.macroGoals.calories, goal.macroGoals.protein,
                goal.macroGoals.carbs, goal.macroGoals.fat
            )
        }
    }
}

private fun jsonFor(
    items: List<ExportItem>,
    data: ExportData,
    range: ExportRange,
    unitSystem: UnitSystem,
    today: LocalDate
): JsonObject = buildJsonObject {
    put("app", "MacroTrack")
    put("exportedOn", today.toString())
    put("range", range.fileLabel)
    putJsonObject("units") {
        put("weight", unitSystem.weightLabel())
        put("length", unitSystem.lengthLabel())
        put("macros", "g")
        put("energy", "kcal")
    }
    items.forEach { item ->
        when (item) {
            ExportItem.FOOD_LOG -> putJsonArray("foodLog") {
                data.logs.sortedWith(compareBy<FoodLog> { it.date }.thenBy { it.mealType.ordinal }.thenBy { it.id }).forEach { log ->
                    addJsonObject {
                        put("date", log.date.toString())
                        put("meal", log.mealType.name.lowercase())
                        put("food", log.foodName)
                        put("type", log.kind())
                        log.portion(data = data, unitSystem = unitSystem)?.let { (amount, unit) ->
                            put("amount", amount.toDouble())
                            put("unit", unit)
                        }
                        put("calories", log.calories)
                        put("proteinG", log.protein.oneDecimal().toDouble())
                        put("carbsG", log.carbs.oneDecimal().toDouble())
                        put("fatG", log.fat.oneDecimal().toDouble())
                    }
                }
            }
            ExportItem.WEIGH_INS -> putJsonArray("weighIns") {
                data.weights.sortedBy { it.date }.forEach {
                    addJsonObject {
                        put("date", it.date.toString())
                        put("weight", unitSystem.weightFromKg(kg = it.weight).twoDecimals().toDouble())
                    }
                }
            }
            ExportItem.MEASUREMENTS -> putJsonArray("bodyMeasurements") {
                data.measurements.sortedWith(compareBy<Measurement> { it.date }.thenBy { it.type.ordinal }).forEach {
                    addJsonObject {
                        put("date", it.date.toString())
                        put("measurement", it.type.name.lowercase())
                        put("value", unitSystem.lengthFromCm(cm = it.valueCm).twoDecimals().toDouble())
                        it.note?.let { note -> put("note", note) }
                    }
                }
            }
            ExportItem.FOODS -> {
                putJsonArray("foods") {
                    data.foods.sortedBy { it.name.lowercase() }.forEach { food -> addFood(food = food, unitSystem = unitSystem) }
                }
                val foodsByName = data.foods.associateBy { it.name }
                putJsonArray("recipes") {
                    data.recipes.sortedBy { it.name.lowercase() }.forEach { recipe ->
                        val totals = recipe.totals(foodsByName = foodsByName)
                        addJsonObject {
                            put("name", recipe.name)
                            put("calories", totals.calories)
                            put("proteinG", totals.protein.oneDecimal().toDouble())
                            put("carbsG", totals.carbs.oneDecimal().toDouble())
                            put("fatG", totals.fat.oneDecimal().toDouble())
                            putJsonArray("ingredients") {
                                recipe.ingredients.forEach { ingredient ->
                                    val unit = unitSystem.portionUnit(storedUnit = foodsByName[ingredient.name]?.unit.orEmpty())
                                    addJsonObject {
                                        put("food", ingredient.name)
                                        put("amount", unit.format(amount = ingredient.weight).toDouble())
                                        put("unit", unit.label)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ExportItem.GOALS -> putJsonArray("goalHistory") {
                data.goalChanges().forEach { change ->
                    addJsonObject {
                        put("from", change.goal.date.toString())
                        change.end?.let { put("to", it.toString()) }
                        put("goal", change.goal.goal.name.lowercase())
                        put("calories", change.goal.macroGoals.calories)
                        put("proteinG", change.goal.macroGoals.protein)
                        put("carbsG", change.goal.macroGoals.carbs)
                        put("fatG", change.goal.macroGoals.fat)
                    }
                }
            }
        }
    }
}

private fun JsonArrayBuilder.addFood(food: Food, unitSystem: UnitSystem) {
    val unit = unitSystem.portionUnit(storedUnit = food.unit)
    addJsonObject {
        put("name", food.name)
        put("servingAmount", unit.format(amount = food.weight).toDouble())
        put("servingUnit", unit.label)
        put("calories", food.macros.calories)
        put("proteinG", food.macros.protein.oneDecimal().toDouble())
        put("carbsG", food.macros.carbs.oneDecimal().toDouble())
        put("fatG", food.macros.fat.oneDecimal().toDouble())
        food.barcode?.let { put("barcode", it) }
    }
}

/** Totals of a recipe's ingredients, scaled from each saved food; missing foods count as nothing. */
internal data class Totals(val calories: Int, val protein: Double, val carbs: Double, val fat: Double)

internal fun Recipe.totals(foodsByName: Map<String, Food>): Totals {
    var calories = 0.0
    var protein = 0.0
    var carbs = 0.0
    var fat = 0.0
    ingredients.forEach { ingredient ->
        val food = foodsByName[ingredient.name] ?: return@forEach
        if (food.weight <= 0) return@forEach
        val scale = ingredient.weight / food.weight
        calories += food.macros.calories * scale
        protein += food.macros.protein * scale
        carbs += food.macros.carbs * scale
        fat += food.macros.fat * scale
    }
    return Totals(calories = round(calories).toInt(), protein = protein, carbs = carbs, fat = fat)
}

internal fun FoodLog.kind(): String = when {
    isQuickAdd -> "quick_add"
    isRecipe -> "recipe"
    else -> "food"
}

/** A food log's portion as shown in [unitSystem], or null for recipes and quick adds, which have none. */
internal fun FoodLog.portion(data: ExportData, unitSystem: UnitSystem): Pair<String, String>? {
    if (isRecipe || isQuickAdd) return null
    val unit = unitSystem.portionUnit(storedUnit = data.foodUnits[foodName].orEmpty())
    return unit.format(amount = weight) to unit.label
}

internal fun UnitSystem.weightLabel(): String = if (this == UnitSystem.IMPERIAL) "lb" else "kg"

internal fun UnitSystem.lengthLabel(): String = if (this == UnitSystem.IMPERIAL) "in" else "cm"

internal fun Double.oneDecimal(): String = (round(this * 10) / 10).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }

internal fun Double.twoDecimals(): String = (round(this * 100) / 100).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }

/** The Monday on or before this day, which starts its week in the report. */
internal fun LocalDate.weekStart(): LocalDate = minus(period = DatePeriod(days = dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal))
