package org.thelazybattley.macrotrack.features.foodhistory.ui

import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import macrotrack.composeapp.generated.resources.next_day_link_food_sub
import org.thelazybattley.macrotrack.features.nextday.ui.NextDayWeightLink
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.food_history_ago
import macrotrack.composeapp.generated.resources.food_history_all
import macrotrack.composeapp.generated.resources.food_history_amount_each_time
import macrotrack.composeapp.generated.resources.food_history_average
import macrotrack.composeapp.generated.resources.food_history_chart_aria
import macrotrack.composeapp.generated.resources.food_history_days
import macrotrack.composeapp.generated.resources.food_history_days_count
import macrotrack.composeapp.generated.resources.food_history_edit
import macrotrack.composeapp.generated.resources.food_history_empty
import macrotrack.composeapp.generated.resources.food_history_entry_aria
import macrotrack.composeapp.generated.resources.food_history_footer
import macrotrack.composeapp.generated.resources.food_history_kcal_protein
import macrotrack.composeapp.generated.resources.food_history_last
import macrotrack.composeapp.generated.resources.food_history_last_days
import macrotrack.composeapp.generated.resources.food_history_last_less
import macrotrack.composeapp.generated.resources.food_history_last_more
import macrotrack.composeapp.generated.resources.food_history_last_same
import macrotrack.composeapp.generated.resources.food_history_last_time
import macrotrack.composeapp.generated.resources.food_history_last_time_via
import macrotrack.composeapp.generated.resources.food_history_log_today
import macrotrack.composeapp.generated.resources.food_history_logged_to
import macrotrack.composeapp.generated.resources.food_history_meal_count
import macrotrack.composeapp.generated.resources.food_history_month_sum
import macrotrack.composeapp.generated.resources.food_history_mostly_at
import macrotrack.composeapp.generated.resources.food_history_nutrition
import macrotrack.composeapp.generated.resources.food_history_once_month
import macrotrack.composeapp.generated.resources.food_history_once_week
import macrotrack.composeapp.generated.resources.food_history_range
import macrotrack.composeapp.generated.resources.food_history_sentence
import macrotrack.composeapp.generated.resources.food_history_sub
import macrotrack.composeapp.generated.resources.food_history_sub_food
import macrotrack.composeapp.generated.resources.food_history_sub_recipe
import macrotrack.composeapp.generated.resources.food_history_times
import macrotrack.composeapp.generated.resources.food_history_times_month
import macrotrack.composeapp.generated.resources.food_history_times_week
import macrotrack.composeapp.generated.resources.food_history_today
import macrotrack.composeapp.generated.resources.food_history_twice_week
import macrotrack.composeapp.generated.resources.food_history_usual
import macrotrack.composeapp.generated.resources.food_history_usual_line
import macrotrack.composeapp.generated.resources.food_history_usual_of
import macrotrack.composeapp.generated.resources.food_history_via_barcode
import macrotrack.composeapp.generated.resources.food_history_via_copied
import macrotrack.composeapp.generated.resources.food_history_via_edited
import macrotrack.composeapp.generated.resources.food_history_via_library
import macrotrack.composeapp.generated.resources.food_history_via_meal_plan
import macrotrack.composeapp.generated.resources.food_history_via_my_foods
import macrotrack.composeapp.generated.resources.food_history_via_quick_add
import macrotrack.composeapp.generated.resources.food_history_via_recipes
import macrotrack.composeapp.generated.resources.food_history_via_repeating
import macrotrack.composeapp.generated.resources.food_history_via_what_fits
import macrotrack.composeapp.generated.resources.ic_pencil
import macrotrack.composeapp.generated.resources.ic_sparkle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.FoodHistoryEntry
import org.thelazybattley.macrotrack.domain.model.FoodHistoryPeriod
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodLogSource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.daysAgo
import org.thelazybattley.macrotrack.domain.model.mostlyAt
import org.thelazybattley.macrotrack.domain.model.stats
import org.thelazybattley.macrotrack.domain.model.timesPerWeek
import org.thelazybattley.macrotrack.features.foodhistory.FoodHistoryCallbacks
import org.thelazybattley.macrotrack.features.foodhistory.FoodHistoryViewModel
import org.thelazybattley.macrotrack.features.foodhistory.FoodHistoryViewState
import org.thelazybattley.macrotrack.features.photos.ui.formatReminderTime
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsBottomButtons
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsIconButton
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsScaffold
import org.thelazybattley.macrotrack.features.whatfits.ui.portionText
import org.thelazybattley.macrotrack.features.whatfits.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.formatPortion
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Every time one food or recipe was logged, with how much and how. */
@Composable
fun FoodHistoryScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    /** Back to the food log, which opens the entry tapped. */
    onOpenLog: () -> Unit
) {
    val viewModel = koinViewModel<FoodHistoryViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    FoodHistoryScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onEdit = { viewState.editRoute?.let(onNavigate) },
        onOpenNextDay = { onNavigate(AppDestinations.Root.NextDayAll.createRoute(query = viewState.name)) },
        onOpenEntry = { entry ->
            viewModel.onOpenEntry(entry = entry)
            onOpenLog()
        }
    )
}

@Composable
private fun FoodHistoryScreen(
    viewState: FoodHistoryViewState,
    callbacks: FoodHistoryCallbacks,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenNextDay: () -> Unit,
    onOpenEntry: (FoodHistoryEntry) -> Unit
) {
    val base = viewState.base
    val stats = viewState.stats
    val amount: @Composable (Double) -> String = { value -> portionText(isRecipe = viewState.isRecipe, unit = viewState.unit, amount = value) }
    WhatFitsScaffold(
        title = viewState.name,
        subtitle = when {
            base == null -> stringResource(resource = Res.string.food_history_sub)
            viewState.isRecipe -> stringResource(resource = Res.string.food_history_sub_recipe, base.macros.calories.withThousands())
            else -> stringResource(
                resource = Res.string.food_history_sub_food,
                base.macros.calories.withThousands(),
                formatPortion(amount = base.weight, storedUnit = base.unit)
            )
        },
        onBack = onBack,
        trailing = if (viewState.editRoute != null) {
            {
                WhatFitsIconButton(
                    icon = Res.drawable.ic_pencil,
                    description = stringResource(resource = Res.string.food_history_edit, viewState.name),
                    onClick = onEdit
                )
            }
        } else {
            null
        },
        bottomBar = {
            if (base != null && stats != null) {
                val loggedTo = viewState.loggedTo
                val logText = if (loggedTo != null) {
                    stringResource(resource = Res.string.food_history_logged_to, stringResource(resource = loggedTo.title))
                } else {
                    stringResource(resource = Res.string.food_history_log_today, amount(stats.usual))
                }
                if (viewState.editRoute != null) {
                    WhatFitsBottomButtons(
                        secondary = stringResource(resource = Res.string.food_history_nutrition),
                        onSecondary = onEdit,
                        primary = logText,
                        onPrimary = callbacks::onLogUsual,
                        isEnabled = loggedTo == null
                    )
                } else {
                    // A library food can't be edited, so it only gets Log today.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = colors.background)
                            .navigationBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
                    ) {
                        CommonPrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = logText,
                            enabled = loggedTo == null,
                            onClick = callbacks::onLogUsual
                        )
                    }
                }
            }
        }
    ) {
        if (!viewState.isLoaded) return@WhatFitsScaffold
        Segmented(
            options = FoodHistoryPeriod.entries.map { stringResource(resource = Res.string.food_history_days, it.days) },
            selected = FoodHistoryPeriod.entries.indexOf(viewState.period),
            onSelect = { callbacks.onPeriodSelected(FoodHistoryPeriod.entries[it]) }
        )
        if (stats == null) {
            Text(
                text = stringResource(resource = Res.string.food_history_empty, viewState.period.days),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 18.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            )
        } else {
            StatTiles(viewState = viewState, amount = amount)
            AmountChart(viewState = viewState, amount = amount)
            Insight(viewState = viewState, amount = amount)
        }
        // Eaten at dinner, All dinners searched for it shows what dinners with it do to the next morning's weigh-in.
        if (viewState.entries.any { it.log.mealType == MealType.DINNER }) {
            NextDayWeightLink(
                biggest = null,
                isNew = false,
                subtitle = stringResource(resource = Res.string.next_day_link_food_sub),
                onClick = onOpenNextDay
            )
        }
        val mealCounts = viewState.mealCounts
        if (mealCounts.size > 1) {
            val meals = listOf<MealType?>(null) + mealCounts.map { it.first }
            Segmented(
                options = listOf(stringResource(resource = Res.string.food_history_all, viewState.inPeriod.size)) +
                    mealCounts.map { (meal, count) -> stringResource(resource = Res.string.food_history_meal_count, stringResource(resource = meal.title), count) },
                selected = meals.indexOf(viewState.meal).coerceAtLeast(minimumValue = 0),
                onSelect = { callbacks.onMealSelected(meals[it]) }
            )
        }
        if (stats != null) {
            viewState.shown
                .groupBy { it.log.date.year to it.log.date.month }
                .forEach { (yearMonth, entries) ->
                    MonthGroup(
                        title = yearMonth.second.name.lowercase().replaceFirstChar { it.uppercase() } +
                            if (yearMonth.first != viewState.today.year) " ${yearMonth.first}" else "",
                        entries = entries,
                        viewState = viewState,
                        usual = stats.usual,
                        latestId = viewState.inPeriod.firstOrNull()?.log?.id,
                        amount = amount,
                        onOpenEntry = onOpenEntry
                    )
                }
            Text(text = stringResource(resource = Res.string.food_history_footer), style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
private fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .padding(all = 4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 40.dp)
                    .then(if (isSelected) Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(size = 10.dp)) else Modifier)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, style = typography.bold13, color = if (isSelected) colors.ink else colors.muted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun StatTiles(viewState: FoodHistoryViewState, amount: @Composable (Double) -> String) {
    val stats = viewState.stats ?: return
    val lastDate = stats.last.log.date
    val ago = daysAgo(date = lastDate, today = viewState.today)
    val tiles = listOf(
        Triple(
            stringResource(resource = Res.string.food_history_times),
            stats.times.toString(),
            stringResource(resource = Res.string.food_history_last_days, viewState.period.days)
        ),
        Triple(
            stringResource(resource = Res.string.food_history_usual),
            amount(stats.usual),
            stringResource(resource = Res.string.food_history_usual_of, stats.usualCount, stats.times)
        ),
        Triple(
            stringResource(resource = Res.string.food_history_average),
            amount(roundToStep(value = stats.average, isRecipe = viewState.isRecipe)),
            stringResource(resource = Res.string.food_history_range, rangeValue(value = stats.min, isRecipe = viewState.isRecipe), amount(stats.max))
        ),
        Triple(
            stringResource(resource = Res.string.food_history_last),
            if (ago == 0) stringResource(resource = Res.string.food_history_today) else pluralStringResource(resource = Res.plurals.food_history_days_count, quantity = ago, ago),
            if (ago == 0) lastDate.toAbbreviatedMonthDay() else stringResource(resource = Res.string.food_history_ago, lastDate.toAbbreviatedMonthDay())
        )
    )
    Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        tiles.forEach { (label, value, sub) ->
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                    .padding(horizontal = 8.dp, vertical = 9.dp)
            ) {
                Text(text = label, style = typography.medium11, color = colors.muted, maxLines = 1)
                Text(text = value, style = typography.bold16, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = sub, style = typography.regular10, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** The average to the nearest gram, or the nearest tenth of a serving. */
private fun roundToStep(value: Double, isRecipe: Boolean): Double =
    if (isRecipe) (value / 10).roundToInt() * 10.0 else value.roundToInt().toDouble()

/** The low end of a range, without its unit. */
private fun rangeValue(value: Double, isRecipe: Boolean): String =
    if (isRecipe) ((value / 10).roundToInt() / 10.0).let { if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString() } else value.roundToInt().toString()

/** A bar for each time in the period, oldest first, against a dashed line at the usual amount. */
@Composable
private fun AmountChart(viewState: FoodHistoryViewState, amount: @Composable (Double) -> String) {
    val stats = viewState.stats ?: return
    val bars = viewState.inPeriod.reversed()
    val latestId = bars.lastOrNull()?.log?.id
    val top = (bars.maxOf { it.amount }.coerceAtLeast(minimumValue = stats.usual) * 1.1).coerceAtLeast(minimumValue = 1.0)
    val accent = colors.accent
    val faded = colors.accent.copy(alpha = 0.45f).compositeOver(colors.divider)
    val track = colors.divider
    val line = colors.muted
    val description = stringResource(
        resource = Res.string.food_history_chart_aria,
        viewState.period.days,
        amount(stats.usual),
        stats.last.log.date.toAbbreviatedMonthDay(),
        amount(stats.last.amount)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(resource = Res.string.food_history_amount_each_time), style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Canvas(modifier = Modifier.width(width = 14.dp).height(height = 2.dp)) {
                drawLine(color = line, start = Offset(x = 0f, y = 0f), end = Offset(x = size.width, y = 0f), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
            }
            Text(
                text = " " + stringResource(resource = Res.string.food_history_usual_line, amount(stats.usual)),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 110.dp)
                .clearAndSetSemantics { contentDescription = description }
        ) {
            val gap = (if (bars.size > 30) 2.dp else 5.dp).toPx()
            val barWidth = ((size.width - gap * (bars.size - 1)) / bars.size).coerceAtLeast(minimumValue = 1f)
            val radius = CornerRadius(x = (barWidth / 2).coerceAtMost(maximumValue = 5.dp.toPx()))
            bars.forEachIndexed { index, entry ->
                val height = (entry.amount / top * size.height).toFloat()
                val isShown = viewState.meal == null || entry.log.mealType == viewState.meal
                drawRoundRect(
                    color = when {
                        entry.log.id == latestId -> accent
                        isShown -> faded
                        else -> track
                    },
                    topLeft = Offset(x = index * (barWidth + gap), y = size.height - height),
                    size = Size(width = barWidth, height = height),
                    cornerRadius = radius
                )
            }
            val usualY = (size.height - stats.usual / top * size.height).toFloat()
            drawLine(
                color = line,
                start = Offset(x = 0f, y = usualY),
                end = Offset(x = size.width, y = usualY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        }
        Row(modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}, horizontalArrangement = Arrangement.SpaceBetween) {
            val labels = listOfNotNull(bars.firstOrNull(), bars.getOrNull(bars.size / 2).takeIf { bars.size > 2 }, bars.lastOrNull().takeIf { bars.size > 1 })
            labels.forEach { Text(text = it.log.date.toAbbreviatedMonthDay(), style = typography.bold10, color = colors.muted) }
        }
    }
}

/** How often it's eaten and at which meal, and how the latest one compares with the usual. */
@Composable
private fun Insight(viewState: FoodHistoryViewState, amount: @Composable (Double) -> String) {
    val entries = viewState.inPeriod
    val all = entries.stats() ?: return
    val perWeek = timesPerWeek(times = entries.size, period = viewState.period)
    val perMonth = (perWeek * 30 / 7).roundToInt()
    val frequency = when {
        perWeek >= 1.5 && perWeek.roundToInt() == 2 -> stringResource(resource = Res.string.food_history_twice_week)
        perWeek >= 1.5 -> stringResource(resource = Res.string.food_history_times_week, perWeek.roundToInt())
        perWeek >= 0.75 -> stringResource(resource = Res.string.food_history_once_week)
        perMonth >= 2 -> stringResource(resource = Res.string.food_history_times_month, perMonth)
        else -> stringResource(resource = Res.string.food_history_once_month)
    }
    val mostly = entries.mostlyAt()
    val lead = if (mostly != null) {
        stringResource(resource = Res.string.food_history_mostly_at, frequency, stringResource(resource = mostly.title).lowercase())
    } else {
        stringResource(resource = Res.string.food_history_sentence, frequency)
    }
    val last = all.last
    val lastText = stringResource(
        resource = when {
            last.amount > all.usual -> Res.string.food_history_last_more
            last.amount < all.usual -> Res.string.food_history_last_less
            else -> Res.string.food_history_last_same
        },
        amount(last.amount),
        last.log.date.toAbbreviatedMonthDay()
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.nightSoft)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_sparkle),
            contentDescription = null,
            tint = colors.nightInk,
            modifier = Modifier.padding(top = 1.dp).size(size = 18.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(lead) }
                append(" ")
                append(lastText)
            },
            style = typography.regular13,
            color = colors.ink
        )
    }
}

@Composable
private fun MonthGroup(
    title: String,
    entries: List<FoodHistoryEntry>,
    viewState: FoodHistoryViewState,
    usual: Double,
    latestId: Long?,
    amount: @Composable (Double) -> String,
    onOpenEntry: (FoodHistoryEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = title, style = typography.bold12, color = colors.muted, modifier = Modifier.weight(weight = 1f))
            Text(
                text = pluralStringResource(resource = Res.plurals.food_history_month_sum, quantity = entries.size, entries.size, amount(entries.sumOf { it.amount })),
                style = typography.medium11,
                color = colors.muted
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                EntryRow(
                    entry = entry,
                    isRecipe = viewState.isRecipe,
                    unit = viewState.unit,
                    usual = usual,
                    isLatest = entry.log.id == latestId,
                    amount = amount,
                    onClick = { onOpenEntry(entry) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun EntryRow(
    entry: FoodHistoryEntry,
    isRecipe: Boolean,
    unit: String,
    usual: Double,
    isLatest: Boolean,
    amount: @Composable (Double) -> String,
    onClick: () -> Unit
) {
    val log = entry.log
    val (boxBackground, boxInk) = when {
        isLatest -> colors.accent to colors.onAccent
        log.mealType == MealType.DINNER -> colors.nightSoft to colors.nightInk
        log.mealType == MealType.LUNCH -> colors.accentSoft to colors.accentInk
        else -> colors.warmSoft to colors.warmInk
    }
    val time = log.loggedAtMillis?.let { millis ->
        val dateTime = Instant.fromEpochMilliseconds(epochMilliseconds = millis).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        formatReminderTime(minutes = dateTime.hour * 60 + dateTime.minute)
    }
    val via = howLogged(log = log, isRecipe = isRecipe, unit = unit)
    val viaText = when {
        isLatest && via != null -> stringResource(resource = Res.string.food_history_last_time_via, via)
        isLatest -> stringResource(resource = Res.string.food_history_last_time)
        else -> via
    }
    val mealName = stringResource(resource = log.mealType.title)
    val amountText = amount(entry.amount)
    val diff = entry.amount - usual
    val description = stringResource(
        resource = Res.string.food_history_entry_aria,
        log.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } + " " + log.date.toAbbreviatedMonthDay(),
        mealName + (time?.let { " · $it" } ?: ""),
        amountText + (viaText?.let { ", $it" } ?: "")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .size(size = 42.dp)
                .background(color = boxBackground, shape = RoundedCornerShape(size = 12.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 1.dp, alignment = Alignment.CenterVertically)
        ) {
            Text(text = log.date.day.toString(), style = typography.bold16, color = boxInk)
            Text(text = log.date.dayOfWeek.name.take(n = 3), style = typography.bold9, color = boxInk)
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = buildAnnotatedString {
                    append(mealName)
                    if (time != null) withStyle(style = SpanStyle(fontWeight = FontWeight.Medium, color = colors.muted)) { append(" · $time") }
                },
                style = typography.bold14,
                color = colors.ink,
                maxLines = 1
            )
            if (viaText != null) {
                Text(
                    text = viaText,
                    style = if (log.editedFrom != null || isLatest) typography.bold12 else typography.regular11,
                    color = when {
                        log.editedFrom != null -> colors.warmInk
                        isLatest -> colors.accentInk
                        else -> colors.muted
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                if (abs(diff) >= 0.5) {
                    Text(
                        text = (if (diff > 0) "+" else "−") + amount(abs(diff)),
                        style = typography.bold11,
                        color = if (diff > 0) colors.warmInk else colors.muted,
                        modifier = Modifier
                            .background(color = if (diff > 0) colors.warmSoft else colors.divider, shape = RoundedCornerShape(size = 6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(text = amountText, style = typography.bold16, color = colors.ink)
            }
            Text(
                text = stringResource(resource = Res.string.food_history_kcal_protein, log.calories.withThousands(), log.protein.roundToInt()),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

/** How [log] was added: edited, copied, planned and so on; null for logs from before this was kept. */
@Composable
private fun howLogged(log: FoodLog, isRecipe: Boolean, unit: String): String? {
    log.editedFrom?.let { return stringResource(resource = Res.string.food_history_via_edited, portionText(isRecipe = isRecipe, unit = unit, amount = it)) }
    return when (log.source) {
        FoodLogSource.MY_FOODS -> stringResource(resource = Res.string.food_history_via_my_foods)
        FoodLogSource.RECIPES -> stringResource(resource = Res.string.food_history_via_recipes)
        FoodLogSource.LIBRARY -> stringResource(resource = Res.string.food_history_via_library)
        FoodLogSource.BARCODE -> stringResource(resource = Res.string.food_history_via_barcode)
        FoodLogSource.QUICK_ADD -> stringResource(resource = Res.string.food_history_via_quick_add)
        FoodLogSource.COPIED -> log.copiedFrom?.let { stringResource(resource = Res.string.food_history_via_copied, it.toAbbreviatedMonthDay()) }
        FoodLogSource.MEAL_PLAN -> stringResource(resource = Res.string.food_history_via_meal_plan)
        FoodLogSource.REPEATING_MEAL -> stringResource(resource = Res.string.food_history_via_repeating)
        FoodLogSource.WHAT_FITS -> stringResource(resource = Res.string.food_history_via_what_fits)
        null -> null
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewFoodHistoryScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val logs = listOf(
        Triple(LocalDate(year = 2026, month = 10, day = 5), MealType.DINNER, 180.0),
        Triple(LocalDate(year = 2026, month = 9, day = 29), MealType.DINNER, 150.0),
        Triple(LocalDate(year = 2026, month = 9, day = 24), MealType.LUNCH, 150.0),
        Triple(LocalDate(year = 2026, month = 9, day = 18), MealType.LUNCH, 120.0)
    ).mapIndexed { index, (date, meal, grams) ->
        FoodHistoryEntry(
            log = FoodLog(
                mealType = meal,
                calories = (grams * 1.65).roundToInt(),
                protein = grams * 0.31,
                carbs = 0.0,
                fat = grams * 0.036,
                id = index.toLong(),
                foodName = "Chicken breast",
                weight = grams,
                dominantMacro = MacroType.PROTEIN,
                date = date,
                source = FoodLogSource.MY_FOODS,
                editedFrom = if (grams == 120.0) 150.0 else null
            ),
            amount = grams
        )
    }
    MacroTrackTheme {
        FoodHistoryScreen(
            viewState = FoodHistoryViewState(name = "Chicken breast", isRecipe = false, today = today, isLoaded = true, entries = logs),
            callbacks = FoodHistoryCallbacks.default(),
            onBack = {},
            onEdit = {},
            onOpenNextDay = {},
            onOpenEntry = {}
        )
    }
}
