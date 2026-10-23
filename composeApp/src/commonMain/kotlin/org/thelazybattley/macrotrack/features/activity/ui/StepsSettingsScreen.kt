package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_lock
import macrotrack.composeapp.generated.resources.steps_extra_calories
import macrotrack.composeapp.generated.resources.steps_extra_distance
import macrotrack.composeapp.generated.resources.steps_extra_to_goal
import macrotrack.composeapp.generated.resources.steps_goal_nudge
import macrotrack.composeapp.generated.resources.steps_line_kcal
import macrotrack.composeapp.generated.resources.steps_line_reached
import macrotrack.composeapp.generated.resources.steps_line_to_go
import macrotrack.composeapp.generated.resources.steps_line_to_goal
import macrotrack.composeapp.generated.resources.steps_settings_alerts
import macrotrack.composeapp.generated.resources.steps_settings_counting
import macrotrack.composeapp.generated.resources.steps_settings_counting_body
import macrotrack.composeapp.generated.resources.steps_settings_counting_off_note
import macrotrack.composeapp.generated.resources.steps_settings_counting_switch
import macrotrack.composeapp.generated.resources.steps_settings_daily_goal
import macrotrack.composeapp.generated.resources.steps_settings_footer
import macrotrack.composeapp.generated.resources.steps_settings_goal_alert
import macrotrack.composeapp.generated.resources.steps_settings_goal_alert_body
import macrotrack.composeapp.generated.resources.steps_settings_goal_value
import macrotrack.composeapp.generated.resources.steps_settings_lock
import macrotrack.composeapp.generated.resources.steps_settings_lock_body
import macrotrack.composeapp.generated.resources.steps_settings_notification
import macrotrack.composeapp.generated.resources.steps_settings_nudge_body
import macrotrack.composeapp.generated.resources.steps_settings_preview
import macrotrack.composeapp.generated.resources.steps_settings_preview_app
import macrotrack.composeapp.generated.resources.steps_settings_preview_description
import macrotrack.composeapp.generated.resources.steps_settings_preview_lock
import macrotrack.composeapp.generated.resources.steps_settings_preview_off
import macrotrack.composeapp.generated.resources.steps_settings_preview_steps
import macrotrack.composeapp.generated.resources.steps_settings_recording
import macrotrack.composeapp.generated.resources.steps_settings_recording_always
import macrotrack.composeapp.generated.resources.steps_settings_recording_body
import macrotrack.composeapp.generated.resources.steps_settings_recording_title
import macrotrack.composeapp.generated.resources.steps_settings_show
import macrotrack.composeapp.generated.resources.steps_settings_show_body
import macrotrack.composeapp.generated.resources.steps_settings_show_with
import macrotrack.composeapp.generated.resources.steps_settings_step_goal
import macrotrack.composeapp.generated.resources.steps_settings_title
import macrotrack.composeapp.generated.resources.steps_settings_unsupported
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.platform.rememberNotificationPermissionState
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.model.StepNotificationExtra
import org.thelazybattley.macrotrack.domain.model.StepNotificationSettings
import org.thelazybattley.macrotrack.features.activity.StepsSettingsCallbacks
import org.thelazybattley.macrotrack.features.activity.StepsSettingsViewModel
import org.thelazybattley.macrotrack.features.activity.StepsSettingsViewState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** The red of the recording dot, as on the live map and the recording bar. */
private val RecordingRed = Color(color = 0xFFD23F1F)

@Composable
fun StepsSettingsScreen(onBack: () -> Unit, onOpenGoal: () -> Unit) {
    val viewModel = koinViewModel<StepsSettingsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    StepsSettingsScreen(viewState = viewState, callbacks = viewModel, onBack = onBack, onOpenGoal = onOpenGoal)
}

@Composable
private fun StepsSettingsScreen(
    viewState: StepsSettingsViewState,
    callbacks: StepsSettingsCallbacks,
    onBack: () -> Unit,
    onOpenGoal: () -> Unit
) {
    val settings = viewState.settings
    val notifications = rememberNotificationPermissionState()
    // Anything that posts needs the permission; asked as it's switched on, never to switch something off.
    val askingFirst: (Boolean, () -> Unit) -> () -> Unit = { isTurningOn, toggle ->
        {
            if (isTurningOn && !notifications.isGranted) notifications.request()
            toggle()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            SquareButton(
                icon = Res.drawable.ic_chevron_left,
                description = stringResource(resource = Res.string.back),
                onClick = onBack
            )
            Text(text = stringResource(resource = Res.string.steps_settings_title), style = typography.bold24, color = colors.ink)
        }
        Section(title = stringResource(resource = Res.string.steps_settings_counting)) {
            SettingsCard {
                SwitchRow(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    title = stringResource(resource = Res.string.steps_settings_counting_switch),
                    body = stringResource(resource = Res.string.steps_settings_counting_body),
                    isOn = viewState.isCountingOn,
                    onToggle = callbacks::onCountingToggled
                )
            }
        }
        // Everything below shows steps, so it waits while counting is off.
        Column(
            modifier = Modifier.alpha(alpha = if (viewState.isCountingOn) 1f else 0.45f),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
        if (!viewState.isCountingOn) {
            Text(text = stringResource(resource = Res.string.steps_settings_counting_off_note), style = typography.regular11, color = colors.muted)
        }
        if (viewState.isNotificationSupported) {
            Section(title = stringResource(resource = Res.string.steps_settings_preview)) {
                NotificationPreview(viewState = viewState)
            }
        }
        Section(title = stringResource(resource = Res.string.steps_settings_daily_goal)) {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable(role = Role.Button, onClick = onOpenGoal)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Text(
                        text = stringResource(resource = Res.string.steps_settings_step_goal),
                        style = typography.medium13,
                        color = colors.ink,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(
                        text = stringResource(resource = Res.string.steps_settings_goal_value, viewState.goal.withThousands()),
                        style = typography.regular13,
                        color = colors.muted
                    )
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = colors.faint,
                        modifier = Modifier.size(size = 16.dp)
                    )
                }
            }
        }
        if (viewState.isNotificationSupported) {
            Section(title = stringResource(resource = Res.string.steps_settings_notification)) {
                SettingsCard {
                    SwitchRow(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        title = stringResource(resource = Res.string.steps_settings_show),
                        body = stringResource(resource = Res.string.steps_settings_show_body),
                        isOn = settings.isOn,
                        onToggle = askingFirst(!settings.isOn, callbacks::onShowStepsToggled)
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                    Column(modifier = Modifier.alpha(alpha = if (settings.isOn) 1f else 0.45f)) {
                        SwitchRow(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            title = stringResource(resource = Res.string.steps_settings_lock),
                            body = stringResource(resource = Res.string.steps_settings_lock_body),
                            isOn = settings.isOn && settings.isOnLockScreen,
                            onToggle = { if (settings.isOn) callbacks.onLockScreenToggled() }
                        )
                        HorizontalDivider(thickness = 1.dp, color = colors.divider)
                        ExtraPicker(settings = settings, onPick = callbacks::onExtraPicked)
                    }
                }
            }
        }
        Section(title = stringResource(resource = Res.string.steps_settings_alerts)) {
            SettingsCard {
                if (viewState.isNotificationSupported) {
                    SwitchRow(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        title = stringResource(resource = Res.string.steps_settings_goal_alert),
                        body = stringResource(resource = Res.string.steps_settings_goal_alert_body, viewState.goal.withThousands()),
                        isOn = settings.isGoalAlertOn,
                        onToggle = askingFirst(!settings.isGoalAlertOn, callbacks::onGoalAlertToggled)
                    )
                }
                if (viewState.isNotificationSupported && viewState.isNudgeSupported) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
                if (viewState.isNudgeSupported) {
                    SwitchRow(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        title = stringResource(resource = Res.string.steps_goal_nudge),
                        body = stringResource(resource = Res.string.steps_settings_nudge_body, StepGoal.NUDGE_MIN_SHORT.withThousands()),
                        isOn = viewState.isNudgeOn,
                        onToggle = askingFirst(!viewState.isNudgeOn, callbacks::onNudgeToggled)
                    )
                    if (viewState.isNudgeOn) {
                        NudgeTimes(selected = settings.nudgeHour, onPick = callbacks::onNudgeHourPicked)
                    }
                }
                if (!viewState.isNotificationSupported && !viewState.isNudgeSupported) {
                    Text(
                        modifier = Modifier.padding(all = 14.dp),
                        text = stringResource(resource = Res.string.steps_settings_unsupported),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
        }
        if (viewState.isNotificationSupported) {
            Section(title = stringResource(resource = Res.string.steps_settings_recording)) {
                RecordingNote()
            }
        }
        }
        Text(text = stringResource(resource = Res.string.steps_settings_footer), style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = title, style = typography.bold12, color = colors.muted)
        content()
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(size = 20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
    ) {
        content()
    }
}

/** What the notification will say right now, or a dashed box when it's off. */
@Composable
private fun NotificationPreview(viewState: StepsSettingsViewState) {
    val settings = viewState.settings
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.divider)
            .padding(all = 14.dp)
    ) {
        if (settings.isOn) {
            val steps = viewState.day.totalSteps.withThousands()
            val line = previewLine(viewState = viewState)
            val description = stringResource(resource = Res.string.steps_settings_preview_description, steps, line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 18.dp))
                    .background(color = colors.surface)
                    .semantics { contentDescription = description }
                    .padding(all = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                ProgressRing(
                    modifier = Modifier.size(size = 44.dp),
                    progress = if (viewState.goal > 0) viewState.day.totalSteps.toFloat() / viewState.goal else 0f,
                    strokeWidth = 6.dp
                )
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = stringResource(resource = Res.string.steps_settings_preview_app), style = typography.regular11, color = colors.muted)
                    Text(
                        text = stringResource(resource = Res.string.steps_settings_preview_steps, steps),
                        style = typography.bold16,
                        color = colors.ink
                    )
                    Text(text = line, style = typography.regular13, color = colors.muted)
                }
                if (settings.isOnLockScreen) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_lock),
                        contentDescription = stringResource(resource = Res.string.steps_settings_preview_lock),
                        tint = colors.muted,
                        modifier = Modifier.size(size = 16.dp)
                    )
                }
            }
        } else {
            val dashed = colors.faint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 68.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = dashed,
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                            ),
                            cornerRadius = CornerRadius(x = 18.dp.toPx(), y = 18.dp.toPx())
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = Res.string.steps_settings_preview_off), style = typography.regular13, color = colors.ink)
            }
        }
    }
}

/** The notification's second line, as the phone will show it. */
@Composable
private fun previewLine(viewState: StepsSettingsViewState): String {
    val settings = viewState.settings
    val left = viewState.stepsLeft
    val toGoal = if (left == 0) {
        stringResource(resource = Res.string.steps_line_reached, viewState.goal.withThousands())
    } else {
        stringResource(resource = Res.string.steps_line_to_goal, left.withThousands(), viewState.goal.withThousands())
    }
    val isPro = LocalProAccess.current.isActive
    val extra = when (settings.extra.coercedFor(isPro = isPro)) {
        StepNotificationExtra.STEPS_TO_GOAL -> return toGoal
        StepNotificationExtra.CALORIES -> viewState.activeKcal?.let {
            stringResource(resource = Res.string.steps_line_kcal, it.withThousands())
        } ?: distanceText(meters = viewState.distanceMeters)
        StepNotificationExtra.DISTANCE -> distanceText(meters = viewState.distanceMeters)
    }
    return if (left == 0) {
        stringResource(resource = Res.string.steps_line_reached, extra)
    } else {
        stringResource(resource = Res.string.steps_line_to_go, left.withThousands(), extra)
    }
}

/** Calories burned is Pro, so without it the notification shows distance instead. */
private fun StepNotificationExtra.coercedFor(isPro: Boolean): StepNotificationExtra =
    if (this == StepNotificationExtra.CALORIES && !isPro) StepNotificationExtra.DISTANCE else this

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExtraPicker(settings: StepNotificationSettings, onPick: (StepNotificationExtra) -> Unit) {
    val isPro = LocalProAccess.current.isActive
    val selected = settings.extra.coercedFor(isPro = isPro)
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(text = stringResource(resource = Res.string.steps_settings_show_with), style = typography.bold14, color = colors.ink)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 6.dp), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            StepNotificationExtra.entries.forEach { extra ->
                val isLocked = extra == StepNotificationExtra.CALORIES && !isPro
                ChoiceChip(
                    modifier = Modifier.alpha(alpha = if (isLocked) 0.6f else 1f),
                    label = stringResource(
                        resource = when (extra) {
                            StepNotificationExtra.STEPS_TO_GOAL -> Res.string.steps_extra_to_goal
                            StepNotificationExtra.DISTANCE -> Res.string.steps_extra_distance
                            StepNotificationExtra.CALORIES -> Res.string.steps_extra_calories
                        }
                    ),
                    isSelected = selected == extra,
                    leading = if (isLocked) ({ ProBadge() }) else null,
                    onClick = { if (settings.isOn && !isLocked) onPick(extra) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NudgeTimes(selected: Int, onPick: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        StepNotificationSettings.NUDGE_HOURS.forEach { hour ->
            ChoiceChip(label = hourLabel(hour = hour), isSelected = selected == hour, onClick = { onPick(hour) })
        }
    }
}

/** The recording notification can't be turned off: background GPS needs it. */
@Composable
private fun RecordingNote() {
    val shape = RoundedCornerShape(size = 20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(size = 12.dp)
                .background(color = RecordingRed, shape = CircleShape)
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(
                    text = stringResource(resource = Res.string.steps_settings_recording_title),
                    style = typography.bold14,
                    color = colors.ink,
                    modifier = Modifier.weight(weight = 1f)
                )
                Text(text = stringResource(resource = Res.string.steps_settings_recording_always), style = typography.bold12, color = colors.muted)
            }
            Text(text = stringResource(resource = Res.string.steps_settings_recording_body), style = typography.regular11, color = colors.muted)
        }
    }
}
