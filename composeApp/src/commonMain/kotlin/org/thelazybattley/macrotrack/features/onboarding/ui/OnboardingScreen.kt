package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_level_camel
import macrotrack.composeapp.generated.resources.age
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.calorie_deficit
import macrotrack.composeapp.generated.resources.calorie_surplus
import macrotrack.composeapp.generated.resources.cm
import macrotrack.composeapp.generated.resources.continue_text
import macrotrack.composeapp.generated.resources.daily_calorie_goal
import macrotrack.composeapp.generated.resources.estimated_daily_target
import macrotrack.composeapp.generated.resources.female
import macrotrack.composeapp.generated.resources.goal
import macrotrack.composeapp.generated.resources.height
import macrotrack.composeapp.generated.resources.kcal_per_day
import macrotrack.composeapp.generated.resources.ft
import macrotrack.composeapp.generated.resources.height_feet
import macrotrack.composeapp.generated.resources.height_inches
import macrotrack.composeapp.generated.resources.unit_in
import macrotrack.composeapp.generated.resources.lets_go
import macrotrack.composeapp.generated.resources.maintenance_tdee
import macrotrack.composeapp.generated.resources.male
import macrotrack.composeapp.generated.resources.sex
import macrotrack.composeapp.generated.resources.signed_kcal
import macrotrack.composeapp.generated.resources.stay_balanced
import macrotrack.composeapp.generated.resources.step_of
import macrotrack.composeapp.generated.resources.weight_kg_label
import macrotrack.composeapp.generated.resources.your_main_goal_title
import macrotrack.composeapp.generated.resources.your_stats_title
import macrotrack.composeapp.generated.resources.yrs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.round
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.FeetInches
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.titleRes
import org.thelazybattley.macrotrack.features.createfood.ui.MacroPreviewCard
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.common.CommonActivityLevelOption
import org.thelazybattley.macrotrack.ui.common.CommonInputField
import org.thelazybattley.macrotrack.ui.common.CommonUnitSystemToggle
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightInputText
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, onFinishButtonClicked: () -> Unit) {
    val viewModel: OnboardingViewModel = koinViewModel<OnboardingViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isFinished) {
        if (viewState.isFinished) onFinishButtonClicked()
    }
    OnboardingScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel
    )
}

@Composable
private fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    val step = viewState.currentStep
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            BottomButtons(
                showBack = step != OnboardingStep.GOAL_AND_STATS,
                continueText = if (step == OnboardingStep.SUMMARY) Res.string.lets_go else Res.string.continue_text,
                continueEnabled = viewState.canContinue,
                onBack = callbacks::onBackClicked,
                onContinue = callbacks::onContinueClicked
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .statusBarsPadding()
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            StepHeader(step = step)
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { currentStep ->
                when (currentStep) {
                    OnboardingStep.GOAL_AND_STATS -> GoalAndStatsStep(viewState = viewState, callbacks = callbacks)
                    OnboardingStep.ACTIVITY_LEVEL -> ActivityLevelStep(viewState = viewState, callbacks = callbacks)
                    OnboardingStep.SUMMARY -> SummaryStep(viewState = viewState)
                }
            }
        }
    }
}

@Composable
private fun StepHeader(step: OnboardingStep) {
    val total = OnboardingStep.entries.size
    Column(verticalArrangement = Arrangement.spacedBy(space = 14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            OnboardingStep.entries.forEach { entry ->
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 4.dp)
                        .background(
                            color = if (entry.ordinal <= step.ordinal) colors.accent else colors.line,
                            shape = RoundedCornerShape(size = 2.dp)
                        )
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = stringResource(resource = Res.string.step_of, step.ordinal + 1, total),
                style = typography.bold12,
                color = colors.muted
            )
            Text(text = stringResource(resource = step.titleRes), style = typography.bold32, color = colors.ink)
            Text(text = stringResource(resource = step.descriptionRes), style = typography.regular13, color = colors.muted)
        }
    }
}

@Composable
private fun SectionLabel(text: StringResource) {
    Text(text = stringResource(resource = text), style = typography.bold12, color = colors.muted)
}

@Composable
private fun GoalAndStatsStep(
    viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            SectionLabel(text = Res.string.your_main_goal_title)
            Goal.entries.forEach { goal ->
                GoalOption(
                    goal = goal,
                    isSelected = goal == viewState.selectedGoal,
                    onClick = { callbacks.onGoalSelected(goal = goal) }
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            val unitSystem = LocalUnitSystem.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(weight = 1f)) {
                    SectionLabel(text = Res.string.your_stats_title)
                }
                CommonUnitSystemToggle(selected = unitSystem, onSelected = callbacks::onUnitSystemSelected)
            }
            Row(
                modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                StatField(
                    modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                    label = Res.string.age,
                    unit = stringResource(resource = Res.string.yrs),
                    initial = viewState.age.takeIf { it > 0 }?.toString().orEmpty(),
                    keyboardType = KeyboardType.Number,
                    onValue = { callbacks.onAgeUpdated(age = it.toIntOrNull() ?: 0) }
                )
                SexToggle(
                    modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                    selected = viewState.selectedGender,
                    onSelected = callbacks::onGenderSelected
                )
            }
            Row(
                modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                if (unitSystem == UnitSystem.IMPERIAL) {
                    FeetInchesField(
                        modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                        initialCm = viewState.height,
                        onHeightCm = { callbacks.onHeightUpdated(height = it) }
                    )
                } else {
                    StatField(
                        modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                        label = Res.string.height,
                        unit = stringResource(resource = Res.string.cm),
                        initial = viewState.height.takeIf { it > 0 }?.let { (round(it * 10) / 10).toCleanString() }.orEmpty(),
                        keyboardType = KeyboardType.Decimal,
                        onValue = { callbacks.onHeightUpdated(height = it.toDoubleOrNull() ?: 0.0) }
                    )
                }
                // Keyed on the unit so switching units shows the typed weight converted.
                key(unitSystem) {
                    StatField(
                        modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                        label = Res.string.weight_kg_label,
                        unit = weightUnitLabel(),
                        initial = viewState.weight.takeIf { it > 0 }?.let { unitSystem.weightInputText(kg = it) }.orEmpty(),
                        keyboardType = KeyboardType.Decimal,
                        onValue = { callbacks.onWeightUpdated(weight = unitSystem.weightToKg(value = it.toDoubleOrNull() ?: 0.0)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalOption(
    goal: Goal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        RadioDot(isSelected = isSelected)
        Column {
            Text(text = stringResource(resource = goal.description), style = typography.bold15, color = colors.ink)
            Text(
                text = stringResource(
                    resource = when (goal) {
                        Goal.LOSE_WEIGHT -> Res.string.calorie_deficit
                        Goal.MAINTAIN_WEIGHT -> Res.string.stay_balanced
                        Goal.GAIN_WEIGHT -> Res.string.calorie_surplus
                    }
                ),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun RadioDot(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(size = 18.dp)
            .background(color = colors.surface, shape = CircleShape)
            .border(
                width = if (isSelected) 6.dp else 2.dp,
                color = if (isSelected) colors.accent else colors.faint,
                shape = CircleShape
            )
    )
}

@Composable
private fun StatField(
    modifier: Modifier = Modifier,
    label: StringResource,
    unit: String,
    initial: String,
    keyboardType: KeyboardType,
    onValue: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf(value = initial) }
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
    ) {
        Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            CommonInputField(
                modifier = Modifier.weight(weight = 1f).height(height = 36.dp),
                value = text,
                onValueChange = { value ->
                    val allowed = if (keyboardType == KeyboardType.Number) {
                        value.all { it.isDigit() } && value.length <= 3
                    } else {
                        value.count { it == '.' } <= 1 && value.all { it.isDigit() || it == '.' } && value.length <= 5
                    }
                    if (allowed) {
                        text = value
                        onValue(value)
                    }
                },
                placeholder = "0",
                keyboardType = keyboardType,
                textStyle = typography.bold24,
                cornerRadius = 0.dp,
                horizontalPadding = 0.dp,
                showBorder = false
            )
            Text(text = unit, style = typography.regular13, color = colors.muted)
        }
    }
}

/** Height typed as whole feet plus inches; reports the height in cm, or 0 until the feet are typed. */
@Composable
private fun FeetInchesField(
    modifier: Modifier = Modifier,
    initialCm: Double,
    onHeightCm: (Double) -> Unit
) {
    val initial = initialCm.takeIf { it > 0 }?.let { FeetInches.fromCm(cm = it) }
    var feet by rememberSaveable { mutableStateOf(value = initial?.feet?.toString().orEmpty()) }
    var inches by rememberSaveable { mutableStateOf(value = initial?.inches?.toCleanString().orEmpty()) }
    fun report() {
        val wholeFeet = feet.toIntOrNull()
        val extraInches = inches.ifEmpty { "0" }.toDoubleOrNull()
        onHeightCm(if (wholeFeet == null || extraInches == null) 0.0 else FeetInches(feet = wholeFeet, inches = extraInches).cm)
    }
    val feetDescription = stringResource(resource = Res.string.height_feet)
    val inchesDescription = stringResource(resource = Res.string.height_inches)
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
    ) {
        Text(text = stringResource(resource = Res.string.height), style = typography.regular11, color = colors.muted)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            CommonInputField(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 36.dp)
                    .semantics { contentDescription = feetDescription },
                value = feet,
                onValueChange = { value ->
                    if (value.length <= 1 && value.all { it.isDigit() }) {
                        feet = value
                        report()
                    }
                },
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                textStyle = typography.bold24,
                cornerRadius = 0.dp,
                horizontalPadding = 0.dp,
                showBorder = false
            )
            Text(text = stringResource(resource = Res.string.ft), style = typography.regular13, color = colors.muted)
            CommonInputField(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 36.dp)
                    .semantics { contentDescription = inchesDescription },
                value = inches,
                onValueChange = { value ->
                    val isValid = value.count { it == '.' } <= 1 && value.all { it.isDigit() || it == '.' } && value.length <= 4
                    if (isValid && (value.toDoubleOrNull() ?: 0.0) < UnitSystem.INCHES_PER_FOOT) {
                        inches = value
                        report()
                    }
                },
                placeholder = "0",
                keyboardType = KeyboardType.Decimal,
                textStyle = typography.bold24,
                cornerRadius = 0.dp,
                horizontalPadding = 0.dp,
                showBorder = false
            )
            Text(text = stringResource(resource = Res.string.unit_in), style = typography.regular13, color = colors.muted)
        }
    }
}

@Composable
private fun SexToggle(
    modifier: Modifier = Modifier,
    selected: UserSex?,
    onSelected: (UserSex) -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Text(text = stringResource(resource = Res.string.sex), style = typography.regular11, color = colors.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            listOf(UserSex.MALE to Res.string.male, UserSex.FEMALE to Res.string.female).forEach { (sex, label) ->
                val isSelected = sex == selected
                val shape = RoundedCornerShape(size = 10.dp)
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 34.dp)
                        .clip(shape = shape)
                        .background(color = if (isSelected) colors.accentSoft else colors.divider)
                        .border(width = 1.5.dp, color = if (isSelected) colors.accent else Color.Transparent, shape = shape)
                        .clickable { onSelected(sex) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resource = label),
                        style = typography.bold13,
                        color = if (isSelected) colors.accentInk else colors.muted
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityLevelStep(
    viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        ActivityLevel.entries.forEachIndexed { index, level ->
            CommonActivityLevelOption(
                level = index,
                activityLevel = level,
                isSelected = level == viewState.selectedActivityLevel,
                onClick = { callbacks.onActivityLevelSelected(activityLevel = level) }
            )
        }
        val macroGoals = viewState.macroGoals
        val goal = viewState.selectedGoal
        if (macroGoals != null && goal != null) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = colors.hero)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = stringResource(resource = Res.string.estimated_daily_target), style = typography.regular11, color = colors.muted)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                    ) {
                        Text(text = macroGoals.calories.withThousands(), style = typography.bold24, color = colors.ink)
                        Text(
                            text = stringResource(resource = Res.string.kcal_per_day),
                            style = typography.regular13,
                            color = colors.muted,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(resource = goal.description), style = typography.bold12, color = colors.accentInk)
                    Text(
                        text = stringResource(resource = Res.string.signed_kcal, goal.calorieAdjust.toSigned()),
                        style = typography.regular11,
                        color = colors.accentInk
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStep(viewState: OnboardingViewState) {
    val macroGoals = viewState.macroGoals ?: return
    val goal = viewState.selectedGoal ?: return
    val activityLevel = viewState.selectedActivityLevel ?: return
    Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        MacroPreviewCard(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(resource = Res.string.daily_calorie_goal).uppercase(),
            calories = macroGoals.calories,
            protein = macroGoals.protein.toDouble(),
            carbs = macroGoals.carbs.toDouble(),
            fat = macroGoals.fat.toDouble(),
            footnote = stringResource(resource = Res.string.kcal_per_day)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            SummaryRow(
                label = stringResource(resource = Res.string.goal),
                value = stringResource(resource = goal.description) + " · " +
                    stringResource(resource = Res.string.signed_kcal, goal.calorieAdjust.toSigned())
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            SummaryRow(
                label = stringResource(resource = Res.string.activity_level_camel),
                value = stringResource(resource = activityLevel.titleRes())
            )
            viewState.maintenanceCalories?.let { maintenance ->
                HorizontalDivider(thickness = 1.dp, color = colors.divider)
                SummaryRow(
                    label = stringResource(resource = Res.string.maintenance_tdee),
                    value = stringResource(resource = Res.string.signed_kcal, maintenance.withThousands())
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = typography.regular13, color = colors.muted, modifier = Modifier.weight(weight = 1f))
        Text(text = value, style = typography.bold13, color = colors.ink)
    }
}

@Composable
private fun BottomButtons(
    showBack: Boolean,
    continueText: StringResource,
    continueEnabled: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .width(width = 96.dp)
                    .height(height = 54.dp)
                    .clip(shape = RoundedCornerShape(size = 16.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = Res.string.back), style = typography.bold15, color = colors.ink)
            }
        }
        Box(
            modifier = Modifier
                .weight(weight = 1f)
                .height(height = 54.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = if (continueEnabled) colors.accent else colors.line)
                .clickable(enabled = continueEnabled, onClick = onContinue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(resource = continueText),
                style = typography.bold16,
                color = if (continueEnabled) colors.onAccent else colors.faint
            )
        }
    }
}

private fun Int.toSigned(): String = when {
    this > 0 -> "+$this"
    this < 0 -> "−${-this}"
    else -> "±0"
}

@Preview(showBackground = true)
@Composable
private fun PreviewOnboardingScreen() {
    MacroTrackTheme {
        OnboardingScreen(
            viewState = OnboardingViewState(),
            callbacks = OnboardingCallbacks.default()
        )
    }
}
