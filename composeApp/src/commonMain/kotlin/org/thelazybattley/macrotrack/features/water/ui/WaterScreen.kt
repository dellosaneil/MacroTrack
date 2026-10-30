package org.thelazybattley.macrotrack.features.water.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_bottle
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_glass
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_sliders
import macrotrack.composeapp.generated.resources.photo_time_am
import macrotrack.composeapp.generated.resources.photo_time_pm
import macrotrack.composeapp.generated.resources.undo
import macrotrack.composeapp.generated.resources.water_add_amount
import macrotrack.composeapp.generated.resources.water_add_bottle
import macrotrack.composeapp.generated.resources.water_add_glass
import macrotrack.composeapp.generated.resources.water_add_other
import macrotrack.composeapp.generated.resources.water_add_title
import macrotrack.composeapp.generated.resources.water_added
import macrotrack.composeapp.generated.resources.water_added_goal
import macrotrack.composeapp.generated.resources.water_after
import macrotrack.composeapp.generated.resources.water_after_goal
import macrotrack.composeapp.generated.resources.water_after_left
import macrotrack.composeapp.generated.resources.water_amount
import macrotrack.composeapp.generated.resources.water_average
import macrotrack.composeapp.generated.resources.water_bottle
import macrotrack.composeapp.generated.resources.water_bottle_fill
import macrotrack.composeapp.generated.resources.water_daily_goal_line
import macrotrack.composeapp.generated.resources.water_day_today
import macrotrack.composeapp.generated.resources.water_drinks
import macrotrack.composeapp.generated.resources.water_glass
import macrotrack.composeapp.generated.resources.water_goal_met_days
import macrotrack.composeapp.generated.resources.water_goal_reached
import macrotrack.composeapp.generated.resources.water_last_7_days
import macrotrack.composeapp.generated.resources.water_logged_at
import macrotrack.composeapp.generated.resources.water_note
import macrotrack.composeapp.generated.resources.water_nothing_yet
import macrotrack.composeapp.generated.resources.water_of_goal
import macrotrack.composeapp.generated.resources.water_other
import macrotrack.composeapp.generated.resources.water_pace_ahead
import macrotrack.composeapp.generated.resources.water_pace_behind
import macrotrack.composeapp.generated.resources.water_remove
import macrotrack.composeapp.generated.resources.water_settings
import macrotrack.composeapp.generated.resources.water_title
import macrotrack.composeapp.generated.resources.water_to_go
import macrotrack.composeapp.generated.resources.water_today
import macrotrack.composeapp.generated.resources.water_todays_log
import macrotrack.composeapp.generated.resources.water_type_amount
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toDayMonthDay
import org.thelazybattley.macrotrack.domain.model.DEFAULT_WATER_GOAL_ML
import org.thelazybattley.macrotrack.domain.model.WATER_AMOUNT_MAX_ML
import org.thelazybattley.macrotrack.domain.model.WATER_AMOUNT_MIN_ML
import org.thelazybattley.macrotrack.domain.model.WaterDay
import org.thelazybattley.macrotrack.domain.model.WaterEntry
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WaterSettings
import org.thelazybattley.macrotrack.domain.model.WaterSummary
import org.thelazybattley.macrotrack.domain.model.waterFromMl
import org.thelazybattley.macrotrack.domain.model.waterPaceMl
import org.thelazybattley.macrotrack.domain.model.waterToMl
import org.thelazybattley.macrotrack.domain.model.waterUnit
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.water.WaterCallbacks
import org.thelazybattley.macrotrack.features.water.WaterViewModel
import org.thelazybattley.macrotrack.features.water.WaterViewState
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.KeypadPreview
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val AMOUNT_FIELD = "water_amount"
private const val NOTICE_MILLIS = 4_000L

/** A water amount as shown: "900 ml", or "30 fl oz" in imperial. */
@Composable
fun waterText(ml: Int): String {
    val unitSystem = LocalUnitSystem.current
    return stringResource(resource = Res.string.value_unit, unitSystem.waterFromMl(ml = ml).withThousands(), unitSystem.waterUnit)
}

/** Minutes after midnight as "2:40 PM". */
@Composable
fun clockText(minutes: Int): String {
    val hour = minutes / 60 % 24
    return stringResource(
        resource = if (hour < 12) Res.string.photo_time_am else Res.string.photo_time_pm,
        if (hour % 12 == 0) 12 else hour % 12,
        (minutes % 60).toString().padStart(length = 2, padChar = '0')
    )
}

@OptIn(ExperimentalTime::class)
private fun nowMinutes(): Int = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault()).let { it.hour * 60 + it.minute }

@OptIn(ExperimentalTime::class)
private fun WaterEntry.minutes(): Int =
    Instant.fromEpochMilliseconds(epochMilliseconds = loggedAtMillis).toLocalDateTime(timeZone = TimeZone.currentSystemDefault()).let { it.hour * 60 + it.minute }

@Composable
fun WaterScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onOpenSettings: () -> Unit) {
    val viewModel = koinViewModel<WaterViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    WaterScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onBack = onBack, onOpenSettings = onOpenSettings)
}

@Composable
private fun WaterScreen(
    modifier: Modifier = Modifier,
    viewState: WaterViewState,
    callbacks: WaterCallbacks,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val summary = viewState.summary
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(title = Res.string.water_title, onBack = onBack) {
                Box(
                    modifier = Modifier
                        .size(size = 40.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.surface)
                        .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                        .clickable(role = Role.Button, onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_sliders),
                        contentDescription = stringResource(resource = Res.string.water_settings),
                        tint = colors.ink,
                        modifier = Modifier.size(size = 18.dp)
                    )
                }
            }
        },
        bottomBar = {
            val added = viewState.lastAdded
            if (added != null && summary != null) {
                AddedNotice(
                    modifier = Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
                    entry = added,
                    summary = summary,
                    onUndo = callbacks::onUndo,
                    onDismiss = callbacks::onDismissNotice
                )
            }
        }
    ) { innerPadding ->
        if (summary == null) return@Scaffold
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Text(text = getCurrentDate().toDayMonthDay(), style = typography.regular13, color = colors.muted)
            TodayCard(summary = summary)
            QuickAdd(settings = summary.settings, onAdd = callbacks::onAdd, onOther = callbacks::onOpenKeypad)
            TodaysLog(entries = summary.entries, onRemove = callbacks::onRemove)
            LastSevenDays(days = summary.days, goalMl = summary.goalMl)
            Text(text = stringResource(resource = Res.string.water_note), style = typography.regular11, color = colors.muted)
        }
    }
    if (viewState.isTyping && summary != null) {
        AmountKeypad(summary = summary, onDone = callbacks::onAddAmount, onDismiss = callbacks::onCloseKeypad)
    }
}

@Composable
private fun TodayCard(summary: WaterSummary) {
    val now = nowMinutes()
    val pace = waterPaceMl(goalMl = summary.goalMl, minutes = now)
    val percent = (summary.progress * 100).roundToInt()
    val description = stringResource(resource = Res.string.water_bottle_fill, percent, waterText(ml = summary.totalMl), waterText(ml = summary.goalMl))
    val shape = RoundedCornerShape(size = 22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.waterSoft)
            .padding(all = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 18.dp)
    ) {
        WaterBottle(
            modifier = Modifier
                .size(width = 64.dp, height = 112.dp)
                .clearAndSetSemantics { contentDescription = description },
            progress = summary.progress
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(text = stringResource(resource = Res.string.water_today), style = typography.bold12, color = colors.waterInk)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = waterText(ml = summary.totalMl), style = typography.bold32, color = colors.ink, modifier = Modifier.alignByBaseline())
                Text(
                    text = stringResource(resource = Res.string.water_of_goal, waterText(ml = summary.goalMl)),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Text(
                text = if (summary.isGoalMet) {
                    stringResource(resource = Res.string.water_goal_reached)
                } else {
                    stringResource(resource = Res.string.water_to_go, waterText(ml = summary.leftMl))
                },
                style = typography.bold14,
                color = colors.waterInk
            )
            if (!summary.isGoalMet) {
                Text(
                    text = if (summary.totalMl >= pace) {
                        stringResource(resource = Res.string.water_pace_ahead, clockText(minutes = now))
                    } else {
                        stringResource(resource = Res.string.water_pace_behind, clockText(minutes = now), waterText(ml = pace))
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
    }
}

/** A bottle outline filled to [progress]. */
@Composable
fun WaterBottle(modifier: Modifier = Modifier, progress: Float) {
    val outline = colors.water
    val fill = colors.water.copy(alpha = 0.55f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val neck = w * 0.36f
        val body = Path().apply {
            moveTo(x = (w - neck) / 2, y = 0f)
            lineTo(x = (w + neck) / 2, y = 0f)
            lineTo(x = (w + neck) / 2, y = h * 0.12f)
            cubicTo(x1 = w, y1 = h * 0.18f, x2 = w, y2 = h * 0.24f, x3 = w, y3 = h * 0.3f)
            lineTo(x = w, y = h * 0.92f)
            quadraticTo(x1 = w, y1 = h, x2 = w * 0.88f, y2 = h)
            lineTo(x = w * 0.12f, y = h)
            quadraticTo(x1 = 0f, y1 = h, x2 = 0f, y2 = h * 0.92f)
            lineTo(x = 0f, y = h * 0.3f)
            cubicTo(x1 = 0f, y1 = h * 0.24f, x2 = 0f, y2 = h * 0.18f, x3 = (w - neck) / 2, y3 = h * 0.12f)
            close()
        }
        clipPath(path = body) {
            val top = h * (1f - progress)
            drawRect(color = fill, topLeft = Offset(x = 0f, y = top), size = Size(width = w, height = h - top))
        }
        drawPath(path = body, color = outline, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun QuickAdd(settings: WaterSettings, onAdd: (WaterKind) -> Unit, onOther: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        QuickAddButton(
            modifier = Modifier.weight(weight = 1f),
            icon = Res.drawable.ic_glass,
            label = stringResource(resource = Res.string.water_glass),
            sub = stringResource(resource = Res.string.water_add_amount, waterText(ml = settings.glassMl)),
            description = stringResource(resource = Res.string.water_add_glass, waterText(ml = settings.glassMl)),
            onClick = { onAdd(WaterKind.GLASS) }
        )
        QuickAddButton(
            modifier = Modifier.weight(weight = 1f),
            icon = Res.drawable.ic_bottle,
            label = stringResource(resource = Res.string.water_bottle),
            sub = stringResource(resource = Res.string.water_add_amount, waterText(ml = settings.bottleMl)),
            description = stringResource(resource = Res.string.water_add_bottle, waterText(ml = settings.bottleMl)),
            onClick = { onAdd(WaterKind.BOTTLE) }
        )
        QuickAddButton(
            modifier = Modifier.weight(weight = 1f),
            icon = Res.drawable.ic_plus,
            label = stringResource(resource = Res.string.water_other),
            sub = stringResource(resource = Res.string.water_type_amount),
            description = stringResource(resource = Res.string.water_add_other),
            onClick = onOther
        )
    }
}

@Composable
private fun QuickAddButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    label: String,
    sub: String,
    description: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.water, modifier = Modifier.size(size = 24.dp))
        Text(text = label, style = typography.bold14, color = colors.ink)
        Text(text = sub, style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun TodaysLog(entries: List<WaterEntry>, onRemove: (WaterEntry) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(resource = Res.string.water_todays_log), style = typography.bold13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(
                text = pluralStringResource(resource = Res.plurals.water_drinks, quantity = entries.size, entries.size),
                style = typography.regular11,
                color = colors.muted
            )
        }
        val shape = RoundedCornerShape(size = 18.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
        ) {
            if (entries.isEmpty()) {
                Text(
                    text = stringResource(resource = Res.string.water_nothing_yet),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.padding(all = 16.dp)
                )
            }
            entries.reversed().forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                val time = clockText(minutes = entry.minutes())
                val amount = waterText(ml = entry.amountMl)
                Row(
                    modifier = Modifier.fillMaxWidth().height(height = 52.dp).padding(start = 14.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Box(modifier = Modifier.size(size = 8.dp).background(color = colors.water, shape = CircleShape))
                    Text(text = time, style = typography.regular13, color = colors.muted, modifier = Modifier.width(width = 72.dp))
                    Text(text = amount, style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
                    Text(
                        text = stringResource(
                            resource = when (entry.kind) {
                                WaterKind.GLASS -> Res.string.water_glass
                                WaterKind.BOTTLE -> Res.string.water_bottle
                                WaterKind.OTHER -> Res.string.water_other
                            }
                        ),
                        style = typography.regular11,
                        color = colors.muted
                    )
                    Box(
                        modifier = Modifier
                            .size(size = 40.dp)
                            .clip(shape = RoundedCornerShape(size = 12.dp))
                            .clickable(role = Role.Button) { onRemove(entry) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.ic_close),
                            contentDescription = stringResource(resource = Res.string.water_remove, amount, time),
                            tint = colors.muted,
                            modifier = Modifier.size(size = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LastSevenDays(days: List<WaterDay>, goalMl: Int) {
    val shape = RoundedCornerShape(size = 18.dp)
    val past = days.dropLast(n = 1)
    val met = past.count { it.totalMl >= goalMl }
    val average = if (days.isEmpty()) 0 else days.sumOf { it.totalMl } / days.size
    val max = maxOf(goalMl * 1.15f, days.maxOfOrNull { it.totalMl }?.toFloat() ?: 0f).coerceAtLeast(minimumValue = 1f)
    val goalLine = colors.ink.copy(alpha = 0.45f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = stringResource(resource = Res.string.water_last_7_days), style = typography.bold13, color = colors.ink)
                Text(text = stringResource(resource = Res.string.water_average, waterText(ml = average)), style = typography.regular11, color = colors.muted)
            }
            Text(
                text = stringResource(resource = Res.string.water_goal_met_days, met, past.size),
                style = typography.bold11,
                color = colors.waterInk
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(height = 110.dp)) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                days.forEachIndexed { index, day ->
                    val isToday = index == days.lastIndex
                    Column(
                        modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier.weight(weight = 1f).fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val fraction = (day.totalMl / max).coerceIn(0f, 1f)
                            if (fraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction = fraction)
                                        .clip(shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                        .background(color = if (day.totalMl >= goalMl) colors.water else colors.waterSoft)
                                        .then(
                                            if (isToday) {
                                                Modifier.border(
                                                    width = 2.dp,
                                                    color = colors.water,
                                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(height = 4.dp))
                        Text(
                            text = if (isToday) stringResource(resource = Res.string.water_day_today) else day.date.dayOfWeek.name.take(n = 1),
                            style = if (isToday) typography.bold10 else typography.regular10,
                            color = if (isToday) colors.ink else colors.muted,
                            maxLines = 1
                        )
                    }
                }
            }
            // The goal line, above the day labels.
            Canvas(modifier = Modifier.fillMaxWidth().height(height = 110.dp - 18.dp).clearAndSetSemantics {}) {
                val y = size.height * (1f - goalMl / max)
                drawLine(
                    color = goalLine,
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = size.width, y = y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Canvas(modifier = Modifier.size(width = 16.dp, height = 2.dp)) {
                drawRoundRect(color = goalLine, cornerRadius = CornerRadius(x = 1f, y = 1f))
            }
            Text(
                text = stringResource(resource = Res.string.water_daily_goal_line, waterText(ml = goalMl)),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun AddedNotice(modifier: Modifier = Modifier, entry: WaterEntry, summary: WaterSummary, onUndo: () -> Unit, onDismiss: () -> Unit) {
    LaunchedEffect(key1 = entry.id) {
        delay(timeMillis = NOTICE_MILLIS)
        onDismiss()
    }
    val after = if (summary.isGoalMet) {
        stringResource(resource = Res.string.water_added_goal)
    } else {
        stringResource(resource = Res.string.water_to_go, waterText(ml = summary.leftMl)) + "."
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.ink)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(resource = Res.string.water_added, waterText(ml = entry.amountMl), after),
            style = typography.regular13,
            color = colors.background,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = stringResource(resource = Res.string.undo),
            style = typography.bold14,
            color = colors.background,
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onUndo)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

/** Types another amount on the shared keypad, whole ml or fl oz. */
@Composable
private fun AmountKeypad(summary: WaterSummary, onDone: (Int) -> Unit, onDismiss: () -> Unit) {
    val unitSystem = LocalUnitSystem.current
    val goalReached = stringResource(resource = Res.string.water_after_goal)
    CommonKeypadSheet(
        fields = listOf(
            KeypadField(
                id = AMOUNT_FIELD,
                label = stringResource(resource = Res.string.water_add_title),
                shortLabel = stringResource(resource = Res.string.water_amount),
                subtitle = stringResource(resource = Res.string.water_logged_at, clockText(minutes = nowMinutes())),
                unit = unitSystem.waterUnit,
                value = "",
                wholeDigits = 4,
                min = unitSystem.waterFromMl(ml = WATER_AMOUNT_MIN_ML).coerceAtLeast(minimumValue = 1).toDouble(),
                max = unitSystem.waterFromMl(ml = WATER_AMOUNT_MAX_ML).toDouble()
            )
        ),
        openId = AMOUNT_FIELD,
        onOpen = {},
        onCommit = { _, text -> text.toIntOrNull()?.let { onDone(unitSystem.waterToMl(shown = it)) } },
        onDismiss = onDismiss,
        preview = { id, live ->
            val typed = live(id).toIntOrNull()?.takeIf { it > 0 } ?: return@CommonKeypadSheet null
            val total = summary.totalMl + unitSystem.waterToMl(shown = typed)
            KeypadPreview(
                value = stringResource(resource = Res.string.water_after, waterText(ml = total)),
                caption = if (total >= summary.goalMl) goalReached else stringResource(resource = Res.string.water_after_left, waterText(ml = summary.goalMl - total))
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewWaterScreen() {
    val today = getCurrentDate()
    MacroTrackTheme {
        WaterScreen(
            viewState = WaterViewState(
                summary = WaterSummary(
                    date = today,
                    entries = listOf(WaterEntry(id = 1, date = today, amountMl = 250, kind = WaterKind.GLASS, loggedAtMillis = 0)),
                    goalMl = DEFAULT_WATER_GOAL_ML,
                    autoGoalMl = DEFAULT_WATER_GOAL_ML,
                    weightKg = null,
                    settings = WaterSettings(),
                    days = emptyList()
                )
            ),
            callbacks = WaterCallbacks.default(),
            onBack = {},
            onOpenSettings = {}
        )
    }
}
