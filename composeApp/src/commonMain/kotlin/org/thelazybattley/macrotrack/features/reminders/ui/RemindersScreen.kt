package org.thelazybattley.macrotrack.features.reminders.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DayOfWeek
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.reminders_days_some
import macrotrack.composeapp.generated.resources.reminders_master_on
import macrotrack.composeapp.generated.resources.ic_bell
import macrotrack.composeapp.generated.resources.ic_bell_off
import macrotrack.composeapp.generated.resources.ic_scale
import macrotrack.composeapp.generated.resources.ic_send
import macrotrack.composeapp.generated.resources.reminders_days_every
import macrotrack.composeapp.generated.resources.reminders_days_none
import macrotrack.composeapp.generated.resources.reminders_ease_sub
import macrotrack.composeapp.generated.resources.reminders_ease_title
import macrotrack.composeapp.generated.resources.reminders_hero_body
import macrotrack.composeapp.generated.resources.reminders_hero_title
import macrotrack.composeapp.generated.resources.reminders_master
import macrotrack.composeapp.generated.resources.reminders_master_off
import macrotrack.composeapp.generated.resources.reminders_meal_sub
import macrotrack.composeapp.generated.resources.reminders_meals_header
import macrotrack.composeapp.generated.resources.reminders_off
import macrotrack.composeapp.generated.resources.reminders_options
import macrotrack.composeapp.generated.resources.reminders_exact_allow
import macrotrack.composeapp.generated.resources.reminders_exact_body
import macrotrack.composeapp.generated.resources.reminders_exact_title
import macrotrack.composeapp.generated.resources.reminders_perm_allow
import macrotrack.composeapp.generated.resources.reminders_perm_allowed
import macrotrack.composeapp.generated.resources.reminders_perm_blocked
import macrotrack.composeapp.generated.resources.reminders_perm_body
import macrotrack.composeapp.generated.resources.reminders_perm_settings
import macrotrack.composeapp.generated.resources.reminders_perm_title
import macrotrack.composeapp.generated.resources.reminders_quick_sub
import macrotrack.composeapp.generated.resources.reminders_quick_title
import macrotrack.composeapp.generated.resources.reminders_quiet_sub
import macrotrack.composeapp.generated.resources.reminders_quiet_title
import macrotrack.composeapp.generated.resources.reminders_switch_a11y
import macrotrack.composeapp.generated.resources.reminders_test
import macrotrack.composeapp.generated.resources.reminders_test_sent
import macrotrack.composeapp.generated.resources.reminders_time_a11y
import macrotrack.composeapp.generated.resources.reminders_title
import macrotrack.composeapp.generated.resources.reminders_weigh_in_days
import macrotrack.composeapp.generated.resources.reminders_weigh_in_switch
import macrotrack.composeapp.generated.resources.reminders_weight
import macrotrack.composeapp.generated.resources.reminders_weight_header
import macrotrack.composeapp.generated.resources.reminders_weight_sub
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.platform.rememberExactAlarmPermissionState
import org.thelazybattley.macrotrack.core.platform.rememberNotificationPermissionState
import org.thelazybattley.macrotrack.domain.model.LogReminderKind
import org.thelazybattley.macrotrack.domain.model.LogReminderSettings
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.photos.ui.formatReminderTime
import org.thelazybattley.macrotrack.features.reminders.RemindersCallbacks
import org.thelazybattley.macrotrack.features.reminders.RemindersViewModel
import org.thelazybattley.macrotrack.features.reminders.RemindersViewState
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<RemindersViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberNotificationPermissionState()
    val exactAlarms = rememberExactAlarmPermissionState()
    RemindersScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        isPermissionGranted = permission.isGranted,
        isPermanentlyDenied = permission.isPermanentlyDenied,
        onRequestPermission = permission.request,
        isOnTime = exactAlarms.isGranted,
        onAllowOnTime = exactAlarms.request,
        onBack = onBack
    )
}

@Composable
private fun RemindersScreen(
    modifier: Modifier = Modifier,
    viewState: RemindersViewState,
    callbacks: RemindersCallbacks,
    isPermissionGranted: Boolean,
    isPermanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    isOnTime: Boolean,
    onAllowOnTime: () -> Unit,
    onBack: () -> Unit
) {
    val settings = viewState.settings
    var picking by remember { mutableStateOf<LogReminderKind?>(value = null) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = { CommonScreenHeader(title = Res.string.reminders_title, onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            if (settings.isOn && !isPermissionGranted) {
                PermissionBanner(
                    title = Res.string.reminders_perm_title,
                    body = Res.string.reminders_perm_body,
                    button = if (isPermanentlyDenied) Res.string.reminders_perm_settings else Res.string.reminders_perm_allow,
                    onClick = onRequestPermission
                )
            } else if (settings.isOn && !isOnTime) {
                PermissionBanner(
                    title = Res.string.reminders_exact_title,
                    body = Res.string.reminders_exact_body,
                    button = Res.string.reminders_exact_allow,
                    onClick = onAllowOnTime
                )
            }
            HeroCard()
            MasterRow(
                settings = settings,
                onToggle = {
                    // Turning them on is when Android asks to allow notifications.
                    if (!settings.isOn && !isPermissionGranted) onRequestPermission()
                    callbacks.onMasterToggled()
                }
            )
            Column(
                modifier = Modifier.alpha(alpha = if (settings.isOn) 1f else 0.55f),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp)
            ) {
                MealsSection(settings = settings, callbacks = callbacks, onPickTime = { picking = it })
                WeightSection(settings = settings, callbacks = callbacks, onPickTime = { picking = LogReminderKind.WEIGHT })
                OptionsSection(settings = settings, callbacks = callbacks)
                TestSection(
                    canSendTest = viewState.canSendTest && settings.isOn && isPermissionGranted,
                    isTestSent = viewState.isTestSent,
                    isPermissionGranted = isPermissionGranted,
                    onSendTest = callbacks::onSendTest
                )
            }
        }
    }
    picking?.let { kind ->
        val mealType = kind.mealType
        ReminderTimeSheet(
            kind = kind,
            minutes = mealType?.let { settings.meal(mealType = it).minutes } ?: settings.weight.minutes,
            usualMinutes = mealType?.let { viewState.usualMinutes[it] },
            onDismiss = { picking = null },
            onDone = { minutes ->
                picking = null
                if (mealType != null) callbacks.onMealTimePicked(mealType = mealType, minutes = minutes) else callbacks.onWeightTimePicked(minutes = minutes)
            }
        )
    }
}

@Composable
private fun PermissionBanner(title: StringResource, body: StringResource, button: StringResource, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.warmSoft)
            .padding(all = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_bell_off),
            contentDescription = null,
            tint = colors.warmInk,
            modifier = Modifier.size(size = 20.dp)
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Column {
                Text(text = stringResource(resource = title), style = typography.bold14, color = colors.warmInk)
                Text(text = stringResource(resource = body), style = typography.regular13, color = colors.warmInk)
            }
            Box(
                modifier = Modifier
                    .height(height = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = button), style = typography.bold14, color = colors.ink)
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.hero)
            .padding(all = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_bell),
                contentDescription = null,
                tint = colors.accentInk,
                modifier = Modifier.size(size = 20.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = stringResource(resource = Res.string.reminders_hero_title), style = typography.bold14, color = colors.ink)
            Text(text = stringResource(resource = Res.string.reminders_hero_body), style = typography.regular13, color = colors.muted)
        }
    }
}

@Composable
private fun MasterRow(settings: LogReminderSettings, onToggle: () -> Unit) {
    val title = stringResource(resource = Res.string.reminders_master)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(
                text = if (settings.isOn) {
                    pluralStringResource(resource = Res.plurals.reminders_master_on, quantity = settings.activeCount, settings.activeCount)
                } else {
                    stringResource(resource = Res.string.reminders_master_off)
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
        CommonSwitch(isOn = settings.isOn, description = title, onToggle = onToggle)
    }
}

@Composable
private fun MealsSection(settings: LogReminderSettings, callbacks: RemindersCallbacks, onPickTime: (LogReminderKind) -> Unit) {
    Section(header = Res.string.reminders_meals_header) {
        MealType.entries.forEachIndexed { index, mealType ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            val meal = settings.meal(mealType = mealType)
            val isOn = settings.isOn && meal.isOn
            val name = stringResource(resource = mealType.title)
            val time = formatReminderTime(minutes = meal.minutes)
            val (soft, ink) = mealType.colors()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 36.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = soft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = name.take(n = 1), style = typography.bold13, color = ink)
                }
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = name, style = typography.bold14, color = colors.ink)
                    Text(
                        text = if (isOn) stringResource(resource = Res.string.reminders_meal_sub, time) else stringResource(resource = Res.string.reminders_off),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
                TimeButton(
                    time = time,
                    isOn = isOn,
                    description = stringResource(resource = Res.string.reminders_time_a11y, name, time),
                    onClick = { onPickTime(LogReminderKind.of(mealType = mealType)) }
                )
                CommonSwitch(
                    isOn = meal.isOn,
                    description = stringResource(resource = Res.string.reminders_switch_a11y, name),
                    onToggle = { if (settings.isOn) callbacks.onMealToggled(mealType = mealType) }
                )
            }
        }
    }
}

@Composable
private fun WeightSection(settings: LogReminderSettings, callbacks: RemindersCallbacks, onPickTime: () -> Unit) {
    val weight = settings.weight
    val isOn = settings.isOn && weight.isOn
    val time = formatReminderTime(minutes = weight.minutes)
    val name = stringResource(resource = Res.string.reminders_weight)
    Section(header = Res.string.reminders_weight_header) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 36.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_scale),
                        contentDescription = null,
                        tint = colors.accentInk,
                        modifier = Modifier.size(size = 18.dp)
                    )
                }
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = name, style = typography.bold14, color = colors.ink)
                    Text(
                        text = stringResource(resource = if (isOn) Res.string.reminders_weight_sub else Res.string.reminders_off),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
                TimeButton(
                    time = time,
                    isOn = isOn,
                    description = stringResource(resource = Res.string.reminders_time_a11y, name, time),
                    onClick = onPickTime
                )
                CommonSwitch(
                    isOn = weight.isOn,
                    description = stringResource(resource = Res.string.reminders_weigh_in_switch),
                    onToggle = { if (settings.isOn) callbacks.onWeightToggled() }
                )
            }
            val daysLabel = stringResource(resource = Res.string.reminders_weigh_in_days)
            Row(
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = daysLabel },
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                DayOfWeek.entries.forEach { day ->
                    val isPicked = isOn && weight.isOnDay(dayOfWeek = day)
                    val dayName = day.name.lowercase().replaceFirstChar { it.uppercase() }
                    Box(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .height(height = 40.dp)
                            .clip(shape = RoundedCornerShape(size = 12.dp))
                            .background(color = if (isPicked) colors.accent else colors.surface)
                            .border(width = 1.dp, color = if (isPicked) colors.accent else colors.line, shape = RoundedCornerShape(size = 12.dp))
                            .clickable(enabled = isOn, role = Role.Checkbox) { callbacks.onWeightDayToggled(day = day) }
                            .semantics { contentDescription = dayName },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = dayName.take(n = 1), style = typography.bold13, color = if (isPicked) colors.onAccent else colors.faint)
                    }
                }
            }
            val dayCount = weight.days.size
            Text(
                text = when (dayCount) {
                    DayOfWeek.entries.size -> stringResource(resource = Res.string.reminders_days_every)
                    0 -> stringResource(resource = Res.string.reminders_days_none)
                    else -> pluralStringResource(resource = Res.plurals.reminders_days_some, quantity = dayCount, dayCount)
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun OptionsSection(settings: LogReminderSettings, callbacks: RemindersCallbacks) {
    val options = listOf(
        Triple(Res.string.reminders_quick_title to Res.string.reminders_quick_sub, settings.hasQuickActions, callbacks::onQuickActionsToggled),
        Triple(Res.string.reminders_ease_title to Res.string.reminders_ease_sub, settings.easesOff, callbacks::onEaseOffToggled),
        Triple(Res.string.reminders_quiet_title to Res.string.reminders_quiet_sub, settings.isQuietOnWeekends, callbacks::onQuietWeekendsToggled)
    )
    Section(header = Res.string.reminders_options) {
        options.forEachIndexed { index, (text, isOn, onToggle) ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            val title = stringResource(resource = text.first)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                    Text(text = title, style = typography.bold14, color = colors.ink)
                    Text(text = stringResource(resource = text.second), style = typography.regular11, color = colors.muted)
                }
                CommonSwitch(isOn = isOn, description = title, onToggle = { if (settings.isOn) onToggle() })
            }
        }
    }
}

@Composable
private fun TestSection(canSendTest: Boolean, isTestSent: Boolean, isPermissionGranted: Boolean, onSendTest: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        if (canSendTest) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 48.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                    .clickable(onClick = onSendTest),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_send),
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(size = 18.dp)
                )
                Text(
                    text = stringResource(resource = if (isTestSent) Res.string.reminders_test_sent else Res.string.reminders_test),
                    style = typography.bold14,
                    color = colors.ink
                )
            }
        }
        Text(
            text = stringResource(resource = if (isPermissionGranted) Res.string.reminders_perm_allowed else Res.string.reminders_perm_blocked),
            style = typography.regular11,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Section(header: StringResource, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = header), style = typography.bold12, color = colors.muted)
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
private fun TimeButton(time: String, isOn: Boolean, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(height = 36.dp)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = if (isOn) colors.accentSoft else Color.Transparent)
            .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = RoundedCornerShape(size = 10.dp))
            .clickable(enabled = isOn, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = time, style = typography.bold13, color = if (isOn) colors.accentInk else colors.faint)
    }
}

/** The letter tile's colors for each meal, as on the design. */
@Composable
private fun MealType.colors(): Pair<Color, Color> = when (this) {
    MealType.BREAKFAST -> colors.warmSoft to colors.warmInk
    MealType.LUNCH -> colors.accentSoft to colors.accentInk
    MealType.DINNER -> colors.nightSoft to colors.nightInk
    MealType.SNACK -> colors.divider to colors.muted
}

@Preview(showBackground = true)
@Composable
private fun PreviewRemindersScreen() {
    MacroTrackTheme {
        RemindersScreen(
            viewState = RemindersViewState(
                settings = LogReminderSettings(isOn = true),
                usualMinutes = mapOf(MealType.LUNCH to 760),
                canSendTest = true
            ),
            callbacks = RemindersCallbacks.default(),
            isPermissionGranted = false,
            isPermanentlyDenied = true,
            onRequestPermission = {},
            isOnTime = true,
            onAllowOnTime = {},
            onBack = {}
        )
    }
}
