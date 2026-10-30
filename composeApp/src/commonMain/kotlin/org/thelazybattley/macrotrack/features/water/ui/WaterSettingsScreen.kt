package org.thelazybattley.macrotrack.features.water.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_bottle
import macrotrack.composeapp.generated.resources.ic_glass
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.lb
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.water_bigger
import macrotrack.composeapp.generated.resources.water_bottle
import macrotrack.composeapp.generated.resources.water_check_every
import macrotrack.composeapp.generated.resources.water_daily_goal
import macrotrack.composeapp.generated.resources.water_daily_goal_title
import macrotrack.composeapp.generated.resources.water_earlier
import macrotrack.composeapp.generated.resources.water_from
import macrotrack.composeapp.generated.resources.water_glass
import macrotrack.composeapp.generated.resources.water_glasses_a_day
import macrotrack.composeapp.generated.resources.water_goal_from_weight
import macrotrack.composeapp.generated.resources.water_goal_own
import macrotrack.composeapp.generated.resources.water_goal_why
import macrotrack.composeapp.generated.resources.water_goal_why_none
import macrotrack.composeapp.generated.resources.water_hours
import macrotrack.composeapp.generated.resources.water_later
import macrotrack.composeapp.generated.resources.water_remind_behind
import macrotrack.composeapp.generated.resources.water_remind_behind_body
import macrotrack.composeapp.generated.resources.water_reminder_note
import macrotrack.composeapp.generated.resources.water_reminders
import macrotrack.composeapp.generated.resources.water_settings_title
import macrotrack.composeapp.generated.resources.water_show_on_today
import macrotrack.composeapp.generated.resources.water_show_on_today_body
import macrotrack.composeapp.generated.resources.water_sizes
import macrotrack.composeapp.generated.resources.water_smaller
import macrotrack.composeapp.generated.resources.water_suggested
import macrotrack.composeapp.generated.resources.water_tap_to_type
import macrotrack.composeapp.generated.resources.water_until
import macrotrack.composeapp.generated.resources.water_your_goal
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.rememberNotificationPermissionState
import org.thelazybattley.macrotrack.domain.model.DEFAULT_WATER_GOAL_ML
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WATER_GOAL_MAX_ML
import org.thelazybattley.macrotrack.domain.model.WATER_GOAL_MIN_ML
import org.thelazybattley.macrotrack.domain.model.WATER_ML_PER_KG
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WaterSettings
import org.thelazybattley.macrotrack.domain.model.WaterSummary
import org.thelazybattley.macrotrack.domain.model.reminderMinutes
import org.thelazybattley.macrotrack.domain.model.waterFromMl
import org.thelazybattley.macrotrack.domain.model.waterSizeStepMl
import org.thelazybattley.macrotrack.domain.model.waterToMl
import org.thelazybattley.macrotrack.domain.model.waterUnit
import org.thelazybattley.macrotrack.features.water.WaterSettingsCallbacks
import org.thelazybattley.macrotrack.features.water.WaterSettingsViewModel
import org.thelazybattley.macrotrack.features.water.WaterSettingsViewState
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.common.KeypadPreview
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatTwoDecimals
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private const val GOAL_FIELD = "water_goal"
private const val GLASS_ML = 250

@Composable
fun WaterSettingsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val viewModel = koinViewModel<WaterSettingsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberNotificationPermissionState()
    WaterSettingsScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        isNotificationAllowed = permission.isGranted,
        onRequestNotifications = permission.request,
        onBack = onBack
    )
}

@Composable
private fun WaterSettingsScreen(
    modifier: Modifier = Modifier,
    viewState: WaterSettingsViewState,
    callbacks: WaterSettingsCallbacks,
    isNotificationAllowed: Boolean,
    onRequestNotifications: () -> Unit,
    onBack: () -> Unit
) {
    val summary = viewState.summary
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = { CommonScreenHeader(title = Res.string.water_settings_title, onBack = onBack) }
    ) { innerPadding ->
        if (summary == null) return@Scaffold
        val settings = summary.settings
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            Section(header = Res.string.water_daily_goal) { GoalSection(summary = summary, callbacks = callbacks) }
            Section(header = Res.string.water_sizes) { SizesSection(settings = settings, callbacks = callbacks) }
            Section(header = Res.string.water_reminders) {
                RemindersSection(
                    settings = settings,
                    callbacks = callbacks,
                    onTurnOn = { if (!isNotificationAllowed) onRequestNotifications() }
                )
            }
            Section(header = null) {
                SwitchRow(
                    title = stringResource(resource = Res.string.water_show_on_today),
                    body = stringResource(resource = Res.string.water_show_on_today_body),
                    isOn = settings.isShownOnToday,
                    onToggle = callbacks::onShowOnTodayToggled
                )
            }
        }
    }
    if (viewState.isTypingGoal && summary != null) {
        GoalKeypad(summary = summary, onDone = callbacks::onCustomGoal, onDismiss = callbacks::onCloseGoalKeypad)
    }
}

@Composable
private fun Section(header: StringResource?, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        header?.let { Text(text = stringResource(resource = it), style = typography.bold12, color = colors.muted) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun GoalSection(summary: WaterSummary, callbacks: WaterSettingsCallbacks) {
    val settings = summary.settings
    val unitSystem = LocalUnitSystem.current
    Column(modifier = Modifier.padding(all = 14.dp), verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.divider)
                .padding(all = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            listOf(true to Res.string.water_goal_from_weight, false to Res.string.water_goal_own).forEach { (isFromWeight, label) ->
                val isOn = settings.isGoalFromWeight == isFromWeight
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 38.dp)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .background(color = if (isOn) colors.surface else Color.Transparent)
                        .semantics { selected = isOn }
                        .clickable(role = Role.Tab) { callbacks.onGoalFromWeight(isFromWeight = isFromWeight) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(resource = label), style = typography.bold13, color = if (isOn) colors.ink else colors.muted)
                }
            }
        }
        if (settings.isGoalFromWeight) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Text(text = waterText(ml = summary.autoGoalMl), style = typography.bold24, color = colors.ink)
                val weight = summary.weightKg
                Text(
                    text = if (weight == null) {
                        stringResource(resource = Res.string.water_goal_why_none, waterText(ml = DEFAULT_WATER_GOAL_ML))
                    } else {
                        val shown = stringResource(
                            resource = Res.string.value_unit,
                            unitSystem.weightFromKg(kg = weight).formatTwoDecimals(),
                            stringResource(resource = if (unitSystem == UnitSystem.IMPERIAL) Res.string.lb else Res.string.kg)
                        )
                        stringResource(resource = Res.string.water_goal_why, WATER_ML_PER_KG, shown)
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = stringResource(resource = Res.string.water_your_goal), style = typography.bold12, color = colors.muted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .border(width = 1.5.dp, color = colors.accent, shape = RoundedCornerShape(size = 12.dp))
                        .clickable(role = Role.Button, onClick = callbacks::onOpenGoalKeypad)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = waterText(ml = settings.customGoalMl), style = typography.bold18, color = colors.ink, modifier = Modifier.weight(weight = 1f))
                    Text(text = stringResource(resource = Res.string.water_tap_to_type), style = typography.regular11, color = colors.muted)
                }
                Text(
                    text = stringResource(resource = Res.string.water_suggested, waterText(ml = summary.autoGoalMl)),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
private fun SizesSection(settings: WaterSettings, callbacks: WaterSettingsCallbacks) {
    val step = LocalUnitSystem.current.waterSizeStepMl
    listOf(
        Triple(WaterKind.GLASS, Res.string.water_glass, Res.drawable.ic_glass) to settings.glassMl,
        Triple(WaterKind.BOTTLE, Res.string.water_bottle, Res.drawable.ic_bottle) to settings.bottleMl
    ).forEachIndexed { index, (kind, ml) ->
        if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
        val name = stringResource(resource = kind.second)
        StepperRow(
            icon = kind.third,
            title = name,
            value = waterText(ml = ml),
            decreaseLabel = stringResource(resource = Res.string.water_smaller, name),
            increaseLabel = stringResource(resource = Res.string.water_bigger, name),
            onDecrease = { callbacks.onSizeStep(kind = kind.first, stepMl = -step) },
            onIncrease = { callbacks.onSizeStep(kind = kind.first, stepMl = step) }
        )
    }
}

@Composable
private fun RemindersSection(settings: WaterSettings, callbacks: WaterSettingsCallbacks, onTurnOn: () -> Unit) {
    SwitchRow(
        title = stringResource(resource = Res.string.water_remind_behind),
        body = stringResource(resource = Res.string.water_remind_behind_body),
        isOn = settings.isReminderOn,
        onToggle = {
            if (!settings.isReminderOn) onTurnOn()
            callbacks.onReminderToggled()
        }
    )
    HorizontalDivider(thickness = 1.dp, color = colors.divider)
    Column(
        modifier = Modifier
            .alpha(alpha = if (settings.isReminderOn) 1f else 0.5f)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(text = stringResource(resource = Res.string.water_check_every), style = typography.bold12, color = colors.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            WaterSettings.REMINDER_HOURS.forEach { hours ->
                val isOn = settings.reminderEveryHours == hours
                val shape = RoundedCornerShape(size = 12.dp)
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 40.dp)
                        .clip(shape = shape)
                        .background(color = if (isOn) colors.accentSoft else colors.surface)
                        .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = shape)
                        .semantics { selected = isOn }
                        .clickable(enabled = settings.isReminderOn, role = Role.Tab) { callbacks.onReminderEvery(hours = hours) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pluralStringResource(resource = Res.plurals.water_hours, quantity = hours, hours),
                        style = typography.bold13,
                        color = if (isOn) colors.accentInk else colors.ink
                    )
                }
            }
        }
    }
    listOf(true to settings.reminderStartMinutes, false to settings.reminderEndMinutes).forEach { (isStart, minutes) ->
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        val title = stringResource(resource = if (isStart) Res.string.water_from else Res.string.water_until)
        Box(modifier = Modifier.alpha(alpha = if (settings.isReminderOn) 1f else 0.5f)) {
            StepperRow(
                icon = null,
                title = title,
                value = clockText(minutes = minutes),
                decreaseLabel = stringResource(resource = Res.string.water_earlier, title),
                increaseLabel = stringResource(resource = Res.string.water_later, title),
                isEnabled = settings.isReminderOn,
                onDecrease = { callbacks.onReminderWindowStep(isStart = isStart, hours = -1) },
                onIncrease = { callbacks.onReminderWindowStep(isStart = isStart, hours = 1) }
            )
        }
    }
    if (settings.isReminderOn) {
        val count = settings.reminderMinutes().size
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        Text(
            text = pluralStringResource(resource = Res.plurals.water_reminder_note, quantity = count, count),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.padding(all = 14.dp)
        )
    }
}

@Composable
private fun SwitchRow(title: String, body: String, isOn: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = body, style = typography.regular11, color = colors.muted)
        }
        CommonSwitch(isOn = isOn, description = title, onToggle = onToggle)
    }
}

@Composable
private fun StepperRow(
    icon: DrawableResource?,
    title: String,
    value: String,
    decreaseLabel: String,
    increaseLabel: String,
    isEnabled: Boolean = true,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        icon?.let { Icon(painter = painterResource(resource = it), contentDescription = null, tint = colors.water, modifier = Modifier.size(size = 22.dp)) }
        Text(text = title, style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
        StepButton(icon = Res.drawable.ic_minus, description = decreaseLabel, isEnabled = isEnabled, onClick = onDecrease)
        Text(text = value, style = typography.bold14, color = colors.ink, textAlign = TextAlign.Center, modifier = Modifier.width(width = 76.dp))
        StepButton(icon = Res.drawable.ic_plus, description = increaseLabel, isEnabled = isEnabled, onClick = onIncrease)
    }
}

@Composable
private fun StepButton(icon: DrawableResource, description: String, isEnabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.divider)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = description, tint = colors.ink, modifier = Modifier.size(size = 16.dp))
    }
}

@Composable
private fun GoalKeypad(summary: WaterSummary, onDone: (Int) -> Unit, onDismiss: () -> Unit) {
    val unitSystem = LocalUnitSystem.current
    CommonKeypadSheet(
        fields = listOf(
            KeypadField(
                id = GOAL_FIELD,
                label = stringResource(resource = Res.string.water_daily_goal_title),
                subtitle = stringResource(resource = Res.string.water_suggested, waterText(ml = summary.autoGoalMl)),
                unit = unitSystem.waterUnit,
                value = unitSystem.waterFromMl(ml = summary.settings.customGoalMl).toString(),
                wholeDigits = 4,
                min = unitSystem.waterFromMl(ml = WATER_GOAL_MIN_ML).toDouble(),
                max = unitSystem.waterFromMl(ml = WATER_GOAL_MAX_ML).toDouble()
            )
        ),
        openId = GOAL_FIELD,
        onOpen = {},
        onCommit = { _, text -> text.toIntOrNull()?.let { onDone(unitSystem.waterToMl(shown = it)) } },
        onDismiss = onDismiss,
        preview = { id, live ->
            val typed = live(id).toIntOrNull()?.takeIf { it > 0 } ?: return@CommonKeypadSheet null
            val ml = unitSystem.waterToMl(shown = typed)
            KeypadPreview(
                value = waterText(ml = ml),
                caption = stringResource(resource = Res.string.water_glasses_a_day, (ml + GLASS_ML / 2) / GLASS_ML)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewWaterSettingsScreen() {
    MacroTrackTheme {
        WaterSettingsScreen(
            viewState = WaterSettingsViewState(
                summary = WaterSummary(
                    date = getCurrentDate(),
                    entries = emptyList(),
                    goalMl = 2_600,
                    autoGoalMl = 2_600,
                    weightKg = 74.2,
                    settings = WaterSettings(isReminderOn = true),
                    days = emptyList()
                )
            ),
            callbacks = WaterSettingsCallbacks.default(),
            isNotificationAllowed = true,
            onRequestNotifications = {},
            onBack = {}
        )
    }
}
