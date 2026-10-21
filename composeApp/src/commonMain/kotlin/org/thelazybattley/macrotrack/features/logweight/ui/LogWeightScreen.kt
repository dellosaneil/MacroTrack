package org.thelazybattley.macrotrack.features.logweight.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.ui.common.formatTwoDecimals
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.change_from_last_time
import macrotrack.composeapp.generated.resources.decrease_weight
import macrotrack.composeapp.generated.resources.goal_gained_to_go
import macrotrack.composeapp.generated.resources.goal_lost_to_go
import macrotrack.composeapp.generated.resources.goal_progress
import macrotrack.composeapp.generated.resources.ic_nav_progress
import macrotrack.composeapp.generated.resources.increase_weight
import macrotrack.composeapp.generated.resources.last_weight_on
import macrotrack.composeapp.generated.resources.log_weight
import macrotrack.composeapp.generated.resources.month_change_with_entry
import macrotrack.composeapp.generated.resources.quick_adjust_title
import macrotrack.composeapp.generated.resources.replaces_entry
import macrotrack.composeapp.generated.resources.same
import macrotrack.composeapp.generated.resources.same_as_last_time
import macrotrack.composeapp.generated.resources.save_weight
import macrotrack.composeapp.generated.resources.this_month
import macrotrack.composeapp.generated.resources.your_weight
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.logweight.LogWeightViewModel
import org.thelazybattley.macrotrack.features.logweight.LogWeightViewState
import org.thelazybattley.macrotrack.ui.common.CommonDateButton
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import macrotrack.composeapp.generated.resources.ic_trend_down
import macrotrack.composeapp.generated.resources.ic_trend_flat
import org.thelazybattley.macrotrack.ui.common.formatWeight
import org.thelazybattley.macrotrack.ui.common.CommonKeypadField
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.KeypadPreview
import org.thelazybattley.macrotrack.core.KeypadField
import macrotrack.composeapp.generated.resources.keypad_last_weigh_in
import macrotrack.composeapp.generated.resources.keypad_from_last_time
import kotlin.math.roundToInt

private const val SAME_THRESHOLD_KG = 0.005

private const val WEIGHT_FIELD = "weight"
private const val MIN_WEIGHT_KG = 30.0
private const val MAX_WEIGHT_KG = 300.0

/** Quick-adjust steps from the last weigh-in, in whichever unit the weight is typed in. */
private val QUICK_ADJUSTMENTS = listOf(-0.5, -0.25, 0.0, 0.25, 0.5)

@Composable
fun LogWeightScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<LogWeightViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onPopBackStack()
    }
    LogWeightScreen(
        modifier = modifier,
        viewState = viewState,
        onBack = onPopBackStack,
        onDateSelected = viewModel::onDateSelected,
        onInputChanged = viewModel::onInputChanged,
        onStep = viewModel::onStep,
        onQuickAdjust = viewModel::onQuickAdjust,
        onSave = viewModel::onSave
    )
}

@Composable
private fun LogWeightScreen(
    modifier: Modifier = Modifier,
    viewState: LogWeightViewState,
    onBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onInputChanged: (String) -> Unit,
    onStep: (Boolean) -> Unit,
    onQuickAdjust: (Double) -> Unit,
    onSave: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(value = false) }
    var isKeypadOpen by remember { mutableStateOf(value = false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(title = Res.string.log_weight, onBack = onBack)
        },
        bottomBar = {
            CommonPrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                text = Res.string.save_weight,
                enabled = viewState.weight != null,
                onClick = onSave
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            CommonDateButton(
                modifier = Modifier.fillMaxWidth(),
                date = viewState.date,
                onClick = { showDatePicker = true }
            )
            WeightCard(viewState = viewState, isKeypadOpen = isKeypadOpen, onOpenKeypad = { isKeypadOpen = true }, onStep = onStep)
            viewState.previous?.let { previous ->
                QuickAdjust(
                    previousWeight = previous.weight,
                    previousDate = previous.date,
                    current = viewState.weight,
                    onQuickAdjust = onQuickAdjust
                )
            }
            ContextCard(viewState = viewState)
        }
    }

    if (isKeypadOpen) {
        WeightKeypad(viewState = viewState, onInputChanged = onInputChanged, onDismiss = { isKeypadOpen = false })
    }

    if (showDatePicker) {
        CommonDatePickerDialog(
            selected = viewState.date,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                showDatePicker = false
                onDateSelected(date)
            }
        )
    }
}

@Composable
private fun WeightCard(
    viewState: LogWeightViewState,
    isKeypadOpen: Boolean,
    onOpenKeypad: () -> Unit,
    onStep: (Boolean) -> Unit
) {
    val weight = viewState.weight
    val difference = weight?.let { current -> viewState.previous?.let { current - it.weight } }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Text(text = stringResource(resource = Res.string.your_weight), style = typography.regular11, color = colors.muted)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            StepButton(label = "−", description = stringResource(resource = Res.string.decrease_weight, weightUnitLabel())) { onStep(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CommonKeypadField(
                    modifier = Modifier.width(width = 150.dp).height(height = 64.dp),
                    value = viewState.input,
                    onClick = onOpenKeypad,
                    textAlign = TextAlign.Center,
                    textStyle = typography.bold32,
                    isOpen = isKeypadOpen,
                    isHighlighted = true,
                    cornerRadius = 16.dp,
                    horizontalPadding = 8.dp,
                    contentDescription = stringResource(resource = Res.string.your_weight)
                )
            }
            StepButton(label = "+", description = stringResource(resource = Res.string.increase_weight, weightUnitLabel())) { onStep(true) }
        }
        Text(text = weightUnitLabel(), style = typography.regular13, color = colors.muted)
        if (difference != null) {
            Text(
                text = if (abs(difference) < SAME_THRESHOLD_KG) {
                    stringResource(resource = Res.string.same_as_last_time)
                } else {
                    stringResource(
                        resource = Res.string.change_from_last_time,
                        (if (difference < 0) "↓ " else "↑ ") + abs(difference).formatWeight(),
                        weightUnitLabel()
                    )
                },
                style = typography.bold13,
                color = difference.changeColor(),
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = colors.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        viewState.existing?.let { existing ->
            Text(
                text = stringResource(resource = Res.string.replaces_entry, existing.weight.formatWeight(), weightUnitLabel()),
                style = typography.regular11,
                color = colors.muted,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Types the weight on the keypad; − and + still step it by 0.1. */
@Composable
private fun WeightKeypad(
    viewState: LogWeightViewState,
    onInputChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val unitSystem = LocalUnitSystem.current
    val unit = weightUnitLabel()
    val previous = viewState.previous?.let { unitSystem.weightFromKg(kg = it.weight) }
    CommonKeypadSheet(
        fields = listOf(
            KeypadField(
                id = WEIGHT_FIELD,
                label = stringResource(resource = Res.string.your_weight),
                subtitle = previous?.let { stringResource(resource = Res.string.keypad_last_weigh_in, it.formatTwoDecimals(), unit) }.orEmpty(),
                unit = unit,
                value = viewState.input,
                wholeDigits = 3,
                decimals = 2,
                min = unitSystem.weightFromKg(kg = MIN_WEIGHT_KG).roundToInt().toDouble(),
                max = unitSystem.weightFromKg(kg = MAX_WEIGHT_KG).roundToInt().toDouble()
            )
        ),
        openId = WEIGHT_FIELD,
        onOpen = {},
        onCommit = { _, text -> onInputChanged(text) },
        onDismiss = onDismiss,
        preview = { id, live ->
            val typed = live(id).toDoubleOrNull()?.takeIf { it > 0 }
            if (typed == null || previous == null) {
                null
            } else {
                val difference = typed - previous
                KeypadPreview(
                    value = if (abs(difference) < SAME_THRESHOLD_KG) {
                        stringResource(resource = Res.string.same)
                    } else {
                        (if (difference < 0) "↓ " else "↑ ") + abs(difference).formatTwoDecimals() + " " + unit
                    },
                    caption = stringResource(resource = Res.string.keypad_from_last_time)
                )
            }
        }
    )
}

@Composable
private fun StepButton(
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 48.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.heroTrack, shape = RoundedCornerShape(size = 14.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = typography.bold24, color = colors.ink)
    }
}

@Composable
private fun QuickAdjust(
    previousWeight: Double,
    previousDate: LocalDate,
    current: Double?,
    onQuickAdjust: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(resource = Res.string.quick_adjust_title),
                style = typography.bold12,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = stringResource(
                    resource = Res.string.last_weight_on,
                    previousWeight.formatWeight(),
                    previousDate.toAbbreviatedMonthDay(),
                    weightUnitLabel()
                ),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            val unitSystem = LocalUnitSystem.current
            val previousValue = unitSystem.weightFromKg(kg = previousWeight)
            val currentValue = current?.let { unitSystem.weightFromKg(kg = it) }
            QUICK_ADJUSTMENTS.forEach { adjustment ->
                val value = previousValue + adjustment
                val isSelected = currentValue != null && abs(currentValue - value) < SAME_THRESHOLD_KG
                val shape = RoundedCornerShape(size = 12.dp)
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .heightIn(min = 52.dp)
                        .clip(shape = shape)
                        .background(color = if (isSelected) colors.accentSoft else colors.surface)
                        .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                        .clickable { onQuickAdjust(value) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when {
                            adjustment == 0.0 -> stringResource(resource = Res.string.same)
                            adjustment > 0 -> "+${adjustment.toCleanString()}"
                            else -> "−${(-adjustment).toCleanString()}"
                        },
                        style = typography.bold13,
                        color = if (isSelected) colors.accentInk else adjustment.changeColor()
                    )
                    Text(text = value.formatTwoDecimals(), style = typography.regular10, color = colors.muted)
                }
            }
        }
    }
}

@Composable
private fun ContextCard(viewState: LogWeightViewState) {
    val weight = viewState.weight ?: return
    val userDetails = viewState.userDetails
    val target = userDetails?.targetWeight
    val start = userDetails?.targetStartWeight
    val goalText = if (target != null && start != null && target != start) {
        val isGaining = target > start
        val achieved = (if (isGaining) weight - start else start - weight).coerceAtLeast(minimumValue = 0.0)
        val total = abs(target - start)
        val toGo = (if (isGaining) target - weight else weight - target).coerceAtLeast(minimumValue = 0.0)
        stringResource(
            resource = if (isGaining) Res.string.goal_gained_to_go else Res.string.goal_lost_to_go,
            achieved.formatWeight(),
            total.formatWeight(),
            toGo.formatWeight(),
            weightUnitLabel()
        )
    } else {
        null
    }
    val baseline = viewState.monthBaseline
    val monthText = baseline?.let {
        val change = weight - it.weight
        stringResource(
            resource = Res.string.month_change_with_entry,
            (if (change < 0) "↓ " else if (change > 0) "↑ " else "") + abs(change).formatWeight(),
            it.date.toAbbreviatedMonthDay(),
            weightUnitLabel()
        )
    }
    if (goalText == null && monthText == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
    ) {
        if (goalText != null) {
            ContextRow(
                icon = Res.drawable.ic_trend_down,
                iconBackground = colors.accentSoft,
                iconTint = colors.accentInk,
                title = stringResource(resource = Res.string.goal_progress),
                subtitle = goalText
            )
        }
        if (goalText != null && monthText != null) {
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
        }
        if (monthText != null) {
            ContextRow(
                icon = Res.drawable.ic_nav_progress,
                iconBackground = colors.warmSoft,
                iconTint = colors.warmInk,
                title = stringResource(resource = Res.string.this_month),
                subtitle = monthText
            )
        }
    }
}

@Composable
private fun ContextRow(
    icon: DrawableResource,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .background(color = iconBackground, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = subtitle, style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
private fun Double.changeColor(): Color = when {
    this < -SAME_THRESHOLD_KG -> colors.accentInk
    this > SAME_THRESHOLD_KG -> colors.fat
    else -> colors.ink
}


@Preview(showBackground = true)
@Composable
private fun PreviewLogWeightScreen() {
    MacroTrackTheme {
        LogWeightScreen(
            viewState = LogWeightViewState(input = "74"),
            onBack = {},
            onDateSelected = {},
            onInputChanged = {},
            onStep = {},
            onQuickAdjust = {},
            onSave = {}
        )
    }
}
