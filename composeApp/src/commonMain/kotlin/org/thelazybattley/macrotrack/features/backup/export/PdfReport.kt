package org.thelazybattley.macrotrack.features.backup.export

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.export.PdfColor
import org.thelazybattley.macrotrack.core.export.PdfWriter
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/** The words in a PDF report, looked up in the app's strings so the report reads in the user's language. */
data class PdfLabels(
    val title: String = "MacroTrack report",
    /** The range, dates and units under the title. */
    val subtitle: String = "",
    val summary: String = "Summary",
    val summaryEntry: String = "Summary & weekly averages",
    val daysLogged: String = "Days logged",
    val averageCalories: String = "Average calories",
    val averageMacros: String = "Average protein · carbs · fat",
    val weightChange: String = "Weight change",
    val weight: String = "Weight",
    val weeklyAverages: String = "Weekly averages",
    val weekOf: String = "Week of",
    val days: String = "Days",
    val kcal: String = "kcal",
    val protein: String = "Protein",
    val carbs: String = "Carbs",
    val fat: String = "Fat",
    val averageWeight: String = "Avg weight",
    val foodLog: String = "Food log",
    val date: String = "Date",
    val meal: String = "Meal",
    val food: String = "Food",
    val amount: String = "Amount",
    val weighIns: String = "Weigh-ins",
    val measurements: String = "Body measurements",
    val measurement: String = "Measurement",
    val value: String = "Value",
    val note: String = "Note",
    val goalHistory: String = "Nutrition goal history",
    val from: String = "From",
    val goal: String = "Goal",
    val recipe: String = "Recipe",
    val quickAdd: String = "Quick add",
    val noData: String = "Nothing logged in this range.",
    val meals: Map<MealType, String> = MealType.entries.associateWith { it.name.lowercase().replaceFirstChar(Char::uppercase) },
    val measurementTypes: Map<MeasurementType, String> = MeasurementType.entries.associateWith { it.name.lowercase() },
    val goals: Map<Goal, String> = Goal.entries.associateWith { it.name.lowercase() },
    val formatDate: (LocalDate) -> String = { it.toString() }
)

class PdfReport(val bytes: ByteArray, val entries: List<ExportEntry>)

private const val MARGIN = 40.0
private const val CONTENT_WIDTH = PdfWriter.PAGE_WIDTH - MARGIN * 2
private const val BOTTOM = PdfWriter.PAGE_HEIGHT - 56.0
private const val ROW_HEIGHT = 16.0

/** A table column: where it ends (right edge, as a share of the content width) and whether numbers align right. */
private class Column(val title: String, val end: Double, val alignRight: Boolean = false)

/** Lays out the report top to bottom, starting a new page whenever the next block doesn't fit. */
private class ReportLayout(private val labels: PdfLabels) {
    val pdf = PdfWriter()
    var y = 0.0

    fun startPage() {
        pdf.newPage()
        y = MARGIN
        pdf.text(x = MARGIN, y = PdfWriter.PAGE_HEIGHT - 28, text = "${labels.title} · ${pdf.pageCount}", size = 8.0, color = PdfColor.Muted)
    }

    fun ensure(height: Double) {
        if (y + height > BOTTOM) startPage()
    }

    fun heading(text: String) {
        ensure(height = 40.0)
        y += 14
        pdf.text(x = MARGIN, y = y + 12, text = text, size = 14.0, bold = true)
        y += 22
    }

    fun table(columns: List<Column>, rows: List<List<String>>) {
        fun header() {
            columns.forEachIndexed { index, column -> cell(columns = columns, index = index, text = column.title, bold = true, color = PdfColor.Muted) }
            y += ROW_HEIGHT
            pdf.line(x1 = MARGIN, y1 = y - 4, x2 = MARGIN + CONTENT_WIDTH, y2 = y - 4)
        }
        ensure(height = ROW_HEIGHT * 3)
        header()
        rows.forEach { row ->
            if (y + ROW_HEIGHT > BOTTOM) {
                startPage()
                header()
            }
            row.forEachIndexed { index, text -> cell(columns = columns, index = index, text = text) }
            y += ROW_HEIGHT
        }
    }

    fun paragraph(text: String) {
        ensure(height = ROW_HEIGHT)
        pdf.text(x = MARGIN, y = y + 10, text = text, size = 10.0, color = PdfColor.Muted)
        y += ROW_HEIGHT
    }

    private fun cell(columns: List<Column>, index: Int, text: String, bold: Boolean = false, color: PdfColor = PdfColor.Ink) {
        val column = columns[index]
        val start = MARGIN + (columns.getOrNull(index = index - 1)?.end ?: 0.0) * CONTENT_WIDTH
        val end = MARGIN + column.end * CONTENT_WIDTH - 6
        val fitted = PdfWriter.fit(text = text, width = end - start, size = 9.0, bold = bold)
        if (column.alignRight) {
            pdf.textRight(right = end, y = y + 10, text = fitted, size = 9.0, bold = bold, color = color)
        } else {
            pdf.text(x = start, y = y + 10, text = fitted, size = 9.0, bold = bold, color = color)
        }
    }
}

internal fun buildPdfReport(
    items: List<ExportItem>,
    data: ExportData,
    unitSystem: UnitSystem,
    labels: PdfLabels
): PdfReport {
    val layout = ReportLayout(labels = labels)
    val entries = mutableListOf<ExportEntry>()
    val weightUnit = unitSystem.weightLabel()
    layout.startPage()

    // Title and summary
    entries.add(ExportEntry(name = labels.summaryEntry, kind = ExportEntryKind.PAGE, value = 1))
    layout.pdf.text(x = MARGIN, y = layout.y + 20, text = labels.title, size = 22.0, bold = true)
    layout.y += 30
    layout.pdf.text(x = MARGIN, y = layout.y + 12, text = labels.subtitle, size = 10.0, color = PdfColor.Muted)
    layout.y += 28
    val days = data.logs.groupBy { it.date }
    val dayCount = days.size
    val stats = buildList {
        add(labels.daysLogged to dayCount.toString())
        if (dayCount > 0) {
            add(labels.averageCalories to "${(data.logs.sumOf { it.calories }.toDouble() / dayCount).roundToInt()} ${labels.kcal}")
            add(
                labels.averageMacros to listOf(data.logs.sumOf { it.protein }, data.logs.sumOf { it.carbs }, data.logs.sumOf { it.fat })
                    .joinToString(separator = " · ") { "${(it / dayCount).roundToInt()} g" }
            )
        }
        val sortedWeights = data.weights.sortedBy { it.date }
        if (sortedWeights.size >= 2) {
            val change = unitSystem.weightFromKg(kg = sortedWeights.last().weight - sortedWeights.first().weight)
            add(labels.weightChange to "${if (change > 0) "+" else ""}${change.twoDecimals()} $weightUnit")
        }
    }
    val boxHeight = 24.0 + stats.size * 18
    layout.pdf.fillRect(x = MARGIN, y = layout.y, width = CONTENT_WIDTH, height = boxHeight, color = PdfColor.Hero)
    var statY = layout.y + 22
    stats.forEach { (label, value) ->
        layout.pdf.text(x = MARGIN + 14, y = statY, text = label, size = 10.0, color = PdfColor.Muted)
        layout.pdf.textRight(right = MARGIN + CONTENT_WIDTH - 14, y = statY, text = value, size = 11.0, bold = true)
        statY += 18
    }
    layout.y += boxHeight + 8
    if (dayCount == 0 && data.weights.isEmpty()) layout.paragraph(text = labels.noData)

    // Weight chart
    val chartWeights = data.weights.sortedBy { it.date }
    if (chartWeights.size >= 2) {
        layout.heading(text = labels.weight)
        val height = 150.0
        layout.ensure(height = height + 24)
        val top = layout.y
        val values = chartWeights.map { unitSystem.weightFromKg(kg = it.weight) }
        // Rounded out to half units with a little room, like the app's weight chart.
        val low = floor((values.min() - 0.2) * 2) / 2
        val high = ceil((values.max() + 0.2) * 2) / 2
        val firstDay = chartWeights.first().date.toEpochDays().toDouble()
        val span = (chartWeights.last().date.toEpochDays() - firstDay).coerceAtLeast(minimumValue = 1.0)
        val left = MARGIN + 40
        val width = CONTENT_WIDTH - 40
        listOf(high, (high + low) / 2, low).forEachIndexed { index, value ->
            val lineY = top + height * index / 2
            layout.pdf.line(x1 = left, y1 = lineY, x2 = left + width, y2 = lineY)
            layout.pdf.textRight(right = left - 6, y = lineY + 3, text = value.oneDecimal(), size = 8.0, color = PdfColor.Muted)
        }
        layout.pdf.polyline(
            points = chartWeights.mapIndexed { index, weight ->
                (left + width * (weight.date.toEpochDays() - firstDay) / span) to (top + height * (high - values[index]) / (high - low))
            }
        )
        layout.pdf.text(x = left, y = top + height + 14, text = labels.formatDate(chartWeights.first().date), size = 8.0, color = PdfColor.Muted)
        layout.pdf.textRight(right = left + width, y = top + height + 14, text = labels.formatDate(chartWeights.last().date), size = 8.0, color = PdfColor.Muted)
        layout.y = top + height + 24
    }

    // Weekly averages
    if (dayCount > 0) {
        layout.heading(text = labels.weeklyAverages)
        val weightsByWeek = data.weights.groupBy { it.date.weekStart() }
        val rows = days.entries.groupBy { it.key.weekStart() }.entries.sortedByDescending { it.key }.map { (week, weekDays) ->
            val logs = weekDays.flatMap { it.value }
            val count = weekDays.size
            fun avg(value: Double) = (value / count).roundToInt().toString()
            listOf(
                labels.formatDate(week),
                count.toString(),
                avg(value = logs.sumOf { it.calories }.toDouble()),
                avg(value = logs.sumOf { it.protein }) + " g",
                avg(value = logs.sumOf { it.carbs }) + " g",
                avg(value = logs.sumOf { it.fat }) + " g",
                weightsByWeek[week]?.map { unitSystem.weightFromKg(kg = it.weight) }?.average()?.let { "${it.oneDecimal()} $weightUnit" } ?: "–"
            )
        }
        layout.table(
            columns = listOf(
                Column(title = labels.weekOf, end = 0.24),
                Column(title = labels.days, end = 0.34, alignRight = true),
                Column(title = labels.kcal, end = 0.48, alignRight = true),
                Column(title = labels.protein, end = 0.61, alignRight = true),
                Column(title = labels.carbs, end = 0.74, alignRight = true),
                Column(title = labels.fat, end = 0.86, alignRight = true),
                Column(title = labels.averageWeight, end = 1.0, alignRight = true)
            ),
            rows = rows
        )
    }

    // Sections. Saved foods and recipes aren't part of the report; they're in the CSV and JSON exports.
    items.filter { it != ExportItem.FOODS }.forEach { item ->
        layout.startPage()
        when (item) {
            ExportItem.FOOD_LOG -> {
                entries.add(ExportEntry(name = labels.foodLog, kind = ExportEntryKind.PAGE, value = layout.pdf.pageCount))
                layout.heading(text = labels.foodLog)
                layout.table(
                    columns = listOf(
                        Column(title = labels.date, end = 0.17),
                        Column(title = labels.meal, end = 0.31),
                        Column(title = labels.food, end = 0.62),
                        Column(title = labels.amount, end = 0.74, alignRight = true),
                        Column(title = labels.kcal, end = 0.83, alignRight = true),
                        Column(title = "P · C · F", end = 1.0, alignRight = true)
                    ),
                    rows = data.logs.sortedWith(compareByDescending<FoodLog> { it.date }.thenBy { it.mealType.ordinal }.thenBy { it.id }).map { log ->
                        listOf(
                            labels.formatDate(log.date),
                            labels.meals[log.mealType].orEmpty(),
                            log.foodName,
                            when {
                                log.isQuickAdd -> labels.quickAdd
                                log.isRecipe -> labels.recipe
                                else -> log.portion(data = data, unitSystem = unitSystem)?.let { "${it.first} ${it.second}" }.orEmpty()
                            },
                            log.calories.toString(),
                            listOf(log.protein, log.carbs, log.fat).joinToString(separator = " · ") { it.roundToInt().toString() }
                        )
                    }
                )
            }
            ExportItem.WEIGH_INS -> {
                entries.add(ExportEntry(name = labels.weighIns, kind = ExportEntryKind.PAGE, value = layout.pdf.pageCount))
                layout.heading(text = labels.weighIns)
                layout.table(
                    columns = listOf(Column(title = labels.date, end = 0.5), Column(title = labels.weight, end = 1.0, alignRight = true)),
                    rows = data.weights.sortedByDescending { it.date }.map {
                        listOf(labels.formatDate(it.date), "${unitSystem.weightFromKg(kg = it.weight).twoDecimals()} $weightUnit")
                    }
                )
            }
            ExportItem.MEASUREMENTS -> {
                entries.add(ExportEntry(name = labels.measurements, kind = ExportEntryKind.PAGE, value = layout.pdf.pageCount))
                layout.heading(text = labels.measurements)
                layout.table(
                    columns = listOf(
                        Column(title = labels.date, end = 0.2),
                        Column(title = labels.measurement, end = 0.45),
                        Column(title = labels.value, end = 0.6, alignRight = true),
                        Column(title = labels.note, end = 1.0)
                    ),
                    rows = data.measurements.sortedWith(compareByDescending<Measurement> { it.date }.thenBy { it.type.ordinal }).map {
                        listOf(
                            labels.formatDate(it.date),
                            labels.measurementTypes[it.type].orEmpty(),
                            "${unitSystem.lengthFromCm(cm = it.valueCm).twoDecimals()} ${unitSystem.lengthLabel()}",
                            it.note.orEmpty()
                        )
                    }
                )
            }
            ExportItem.GOALS -> {
                entries.add(ExportEntry(name = labels.goalHistory, kind = ExportEntryKind.PAGE, value = layout.pdf.pageCount))
                layout.heading(text = labels.goalHistory)
                layout.table(
                    columns = listOf(
                        Column(title = labels.from, end = 0.22),
                        Column(title = labels.goal, end = 0.5),
                        Column(title = labels.kcal, end = 0.64, alignRight = true),
                        Column(title = labels.protein, end = 0.76, alignRight = true),
                        Column(title = labels.carbs, end = 0.88, alignRight = true),
                        Column(title = labels.fat, end = 1.0, alignRight = true)
                    ),
                    rows = data.goalChanges().reversed().map { change ->
                        val goals = change.goal.macroGoals
                        listOf(
                            labels.formatDate(change.goal.date),
                            labels.goals[change.goal.goal].orEmpty(),
                            goals.calories.toString(),
                            "${goals.protein} g",
                            "${goals.carbs} g",
                            "${goals.fat} g"
                        )
                    }
                )
            }
            ExportItem.FOODS -> Unit
        }
    }
    return PdfReport(bytes = layout.pdf.build(), entries = entries)
}
