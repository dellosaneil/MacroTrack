package org.thelazybattley.macrotrack.features.profile.nutritiongoals.ui

import org.thelazybattley.macrotrack.ui.common.CommonGoalPaceStepper
import org.thelazybattley.macrotrack.domain.model.GoalPace
import org.thelazybattley.macrotrack.domain.model.calorieAdjust
import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.ui.common.formatWeightWithUnit
import org.thelazybattley.macrotrack.ui.common.formatHeightWithUnit
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.about_kg_per_month
import macrotrack.composeapp.generated.resources.adjustment
import macrotrack.composeapp.generated.resources.based_on_your_personal_information
import macrotrack.composeapp.generated.resources.daily_calorie_goal
import macrotrack.composeapp.generated.resources.daily_macro_targets
import macrotrack.composeapp.generated.resources.deficit_to_lose_weight
import macrotrack.composeapp.generated.resources.expected_change
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_nav_progress
import macrotrack.composeapp.generated.resources.ic_trend_down
import macrotrack.composeapp.generated.resources.ic_trend_flat
import macrotrack.composeapp.generated.resources.kcal_per_day_label
import macrotrack.composeapp.generated.resources.unit_per_week
import macrotrack.composeapp.generated.resources.unit_per_week_stays_the_same
import macrotrack.composeapp.generated.resources.maintenance_tdee
import macrotrack.composeapp.generated.resources.nutrition_goals
import macrotrack.composeapp.generated.resources.percent_value
import macrotrack.composeapp.generated.resources.personal_info_summary
import macrotrack.composeapp.generated.resources.save_goal
import macrotrack.composeapp.generated.resources.signed_kcal
import macrotrack.composeapp.generated.resources.surplus_to_gain_weight
import macrotrack.composeapp.generated.resources.weight_change_estimate_note
import macrotrack.composeapp.generated.resources.weight_change_estimate_note_imperial
import macrotrack.composeapp.generated.resources.your_goal
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.titleRes
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.profile.nutritiongoals.NutritionGoalsCallbacks
import org.thelazybattley.macrotrack.features.profile.nutritiongoals.NutritionGoalsViewModel
import org.thelazybattley.macrotrack.features.profile.nutritiongoals.NutritionGoalsViewState
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import androidx.compose.ui.semantics.Role
import macrotrack.composeapp.generated.resources.change_past_goal
import macrotrack.composeapp.generated.resources.goal_history
import macrotrack.composeapp.generated.resources.goal_history_hint
import macrotrack.composeapp.generated.resources.ic_clock
import macrotrack.composeapp.generated.resources.ic_chevron_right
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.features.profile.pastgoal.ui.GoalPeriodList

@Composable
fun NutritionGoalsScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    onNavigateToPersonalInformation: () -> Unit,
    onNavigateToPastGoal: () -> Unit
) {
    val viewModel = koinViewModel<NutritionGoalsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onPopBackStack()
    }

    NutritionGoalsScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onPopBackStack,
        onNavigateToPersonalInformation = onNavigateToPersonalInformation,
        onNavigateToPastGoal = onNavigateToPastGoal
    )
}

@Composable
private fun NutritionGoalsScreen(
    modifier: Modifier = Modifier,
    viewState: NutritionGoalsViewState,
    callbacks: NutritionGoalsCallbacks,
    onBack: () -> Unit,
    onNavigateToPersonalInformation: () -> Unit,
    onNavigateToPastGoal: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(title = Res.string.nutrition_goals, onBack = onBack)
        },
        bottomBar = {
            CommonPrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                text = Res.string.save_goal,
                enabled = viewState.canSave,
                onClick = callbacks::onSave
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            val goalPace = viewState.goalPace ?: GoalPace.DEFAULT_KG_PER_MONTH
            GoalSection(
                selected = viewState.selectedGoal,
                goalPace = goalPace,
                onSelected = callbacks::onGoalSelected,
                onPaceChanged = callbacks::onGoalPaceChanged
            )
            CalorieGoalCard(
                goal = viewState.selectedGoal,
                goalPace = goalPace,
                maintenanceCalories = viewState.maintenanceCalories,
                macroGoals = viewState.macroGoals
            )
            viewState.macroGoals?.let { macroGoals ->
                MacroTargetsSection(macroGoals = macroGoals)
            }
            if (viewState.history.isNotEmpty()) {
                GoalHistorySection(
                    viewState = viewState,
                    onChangePastGoal = onNavigateToPastGoal
                )
            }
            viewState.userDetails?.let { userDetails ->
                PersonalInformationLink(
                    userDetails = userDetails,
                    onClick = onNavigateToPersonalInformation
                )
            }
        }
    }
}

@Composable
private fun GoalSection(
    selected: Goal?,
    goalPace: Double,
    onSelected: (Goal) -> Unit,
    onPaceChanged: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.your_goal))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Goal.entries.forEach { goal ->
                val isSelected = goal == selected
                val shape = RoundedCornerShape(size = 16.dp)
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .clip(shape = shape)
                        .background(color = if (isSelected) colors.accentSoft else colors.surface)
                        .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                        .clickable { onSelected(goal) }
                        .padding(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 2.dp)
                ) {
                    Text(
                        text = stringResource(resource = goal.title),
                        style = typography.bold14,
                        color = colors.ink
                    )
                    Text(
                        text = stringResource(resource = Res.string.signed_kcal, goal.calorieAdjust(paceKgPerMonth = goalPace).toSigned()),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
        }
        selected?.let { goal ->
            CommonGoalPaceStepper(goal = goal, kgPerMonth = goalPace, onChanged = onPaceChanged)
        }
    }
}

@Composable
private fun CalorieGoalCard(
    goal: Goal?,
    goalPace: Double,
    maintenanceCalories: Int?,
    macroGoals: MacroGoals?
) {
    val calorieAdjust = goal?.calorieAdjust(paceKgPerMonth = goalPace) ?: 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Column {
            Text(
                text = stringResource(resource = Res.string.daily_calorie_goal),
                style = typography.regular11,
                color = colors.muted
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Text(
                    text = macroGoals?.calories?.withThousands() ?: "–",
                    style = typography.bold36,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.kcal_per_day_label),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            BreakdownRow(
                label = stringResource(resource = Res.string.maintenance_tdee),
                value = stringResource(
                    resource = Res.string.signed_kcal,
                    maintenanceCalories?.withThousands() ?: "–"
                )
            )
            BreakdownRow(
                label = stringResource(
                    resource = when (goal) {
                        Goal.LOSE_WEIGHT -> Res.string.deficit_to_lose_weight
                        Goal.GAIN_WEIGHT -> Res.string.surplus_to_gain_weight
                        else -> Res.string.adjustment
                    }
                ),
                value = stringResource(
                    resource = Res.string.signed_kcal,
                    calorieAdjust.toSigned()
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 1.dp)
                .background(color = colors.heroTrack)
        )
        ExpectedWeightChange(dailyCalorieAdjust = calorieAdjust)
    }
}

@Composable
private fun ExpectedWeightChange(dailyCalorieAdjust: Int) {
    val unitSystem = LocalUnitSystem.current
    val weeklyChange = unitSystem.weightFromKg(kg = dailyCalorieAdjust * DAYS_PER_WEEK / KCAL_PER_KG)
    val monthlyChange = unitSystem.weightFromKg(kg = dailyCalorieAdjust * DAYS_PER_MONTH / KCAL_PER_KG)
    val icon = when {
        dailyCalorieAdjust < 0 -> Res.drawable.ic_trend_down
        dailyCalorieAdjust > 0 -> Res.drawable.ic_nav_progress
        else -> Res.drawable.ic_trend_flat
    }

    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .background(color = colors.surface, shape = RoundedCornerShape(size = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = icon),
                    contentDescription = null,
                    tint = colors.accentInk,
                    modifier = Modifier.size(size = 20.dp)
                )
            }
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(resource = Res.string.expected_change),
                    style = typography.regular11,
                    color = colors.muted
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
                ) {
                    Text(
                        text = if (dailyCalorieAdjust == 0) "±0" else weeklyChange.toSignedDecimal(decimals = 2),
                        style = typography.bold24,
                        color = colors.accentInk
                    )
                    Text(
                        text = stringResource(
                            resource = if (dailyCalorieAdjust == 0) {
                                Res.string.unit_per_week_stays_the_same
                            } else {
                                Res.string.unit_per_week
                            },
                            weightUnitLabel()
                        ),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            if (dailyCalorieAdjust != 0) {
                Text(
                    text = stringResource(
                        resource = Res.string.about_kg_per_month,
                        monthlyChange.toSignedDecimal(decimals = 1),
                        weightUnitLabel()
                    ),
                    style = typography.regular11,
                    color = colors.muted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(width = 76.dp)
                )
            }
        }
        Text(
            text = stringResource(
                resource = if (unitSystem == UnitSystem.IMPERIAL) {
                    Res.string.weight_change_estimate_note_imperial
                } else {
                    Res.string.weight_change_estimate_note
                }
            ),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

private const val KCAL_PER_KG = 7700.0
private const val DAYS_PER_WEEK = 7
private const val DAYS_PER_MONTH = 30.4

private fun Double.toSignedDecimal(decimals: Int): String {
    val factor = if (decimals == 1) 10 else 100
    val scaled = kotlin.math.round(kotlin.math.abs(this) * factor).toInt()
    val whole = scaled / factor
    val fraction = (scaled % factor).toString().padStart(length = decimals, padChar = '0')
    val sign = when {
        scaled == 0 -> ""
        this < 0 -> "−"
        else -> "+"
    }
    return "$sign$whole.$fraction"
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = typography.regular13,
            color = colors.muted,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = value,
            style = typography.bold13,
            color = colors.ink
        )
    }
}

private data class MacroShare(
    val macroType: MacroType,
    val color: Color,
    val grams: Int,
    val calories: Int
)

@Composable
private fun MacroTargetsSection(macroGoals: MacroGoals) {
    val macros = listOf(
        MacroShare(MacroType.PROTEIN, colors.protein, macroGoals.protein, macroGoals.protein * 4),
        MacroShare(MacroType.CARBS, colors.carbs, macroGoals.carbs, macroGoals.carbs * 4),
        MacroShare(MacroType.FAT, colors.fat, macroGoals.fat, macroGoals.fat * 9)
    )
    val totalCalories = macros.sumOf { it.calories }.coerceAtLeast(minimumValue = 1)

    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.daily_macro_targets))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
                .padding(all = 14.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 10.dp)
                    .clip(shape = RoundedCornerShape(size = 5.dp)),
                horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                macros.filter { it.calories > 0 }.forEach { macro ->
                    Box(
                        modifier = Modifier
                            .weight(weight = macro.calories.toFloat())
                            .fillMaxHeight()
                            .background(color = macro.color)
                    )
                }
            }
            macros.forEach { macro ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 10.dp)
                            .background(color = macro.color, shape = RoundedCornerShape(size = 3.dp))
                    )
                    Text(
                        text = stringResource(resource = macro.macroType.text()),
                        style = typography.medium13,
                        color = colors.ink,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(
                        text = stringResource(resource = Res.string.signed_kcal, macro.calories.withThousands()),
                        style = typography.regular11,
                        color = colors.muted,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = 64.dp)
                    )
                    Text(
                        text = stringResource(
                            resource = Res.string.percent_value,
                            macro.calories * 100 / totalCalories
                        ),
                        style = typography.regular11,
                        color = colors.muted,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = 36.dp)
                    )
                    Text(
                        text = "${macro.grams}g",
                        style = typography.bold16,
                        color = colors.ink,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = 52.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalInformationLink(
    userDetails: UserDetails,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .border(width = 1.5.dp, color = colors.dashed, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = Res.string.based_on_your_personal_information),
                style = typography.bold13,
                color = colors.ink
            )
            Text(
                text = stringResource(
                    resource = Res.string.personal_info_summary,
                    userDetails.age,
                    userDetails.height.formatHeightWithUnit(),
                    userDetails.weight.formatWeightWithUnit(),
                    stringResource(resource = userDetails.activityLevel.titleRes()).lowercase()
                ),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_left),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp).rotate(degrees = 180f)
        )
    }
}

@Composable
private fun GoalHistorySection(
    viewState: NutritionGoalsViewState,
    onChangePastGoal: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.goal_history))
        GoalPeriodList(
            periods = viewState.history,
            today = getCurrentDate(),
            footer = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 48.dp)
                        .clickable(role = Role.Button, onClick = onChangePastGoal)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_clock),
                        contentDescription = null,
                        tint = colors.accentInk,
                        modifier = Modifier.size(size = 16.dp)
                    )
                    Text(
                        text = stringResource(resource = Res.string.change_past_goal),
                        style = typography.bold14,
                        color = colors.accentInk,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = colors.faint,
                        modifier = Modifier.size(size = 16.dp)
                    )
                }
            }
        )
        Text(
            text = stringResource(resource = Res.string.goal_history_hint),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = typography.bold12,
        color = colors.muted
    )
}

private fun Int.toSigned(): String = when {
    this > 0 -> "+${withThousands()}"
    this < 0 -> "−${(-this).withThousands()}"
    else -> "0"
}

@Preview(showBackground = true)
@Composable
private fun PreviewNutritionGoalsScreen() {
    MacroTrackTheme {
        NutritionGoalsScreen(
            viewState = NutritionGoalsViewState(
                userDetails = UserDetails(
                    weight = 74.2,
                    age = 29,
                    height = 175.0,
                    sex = UserSex.MALE,
                    activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                    goal = Goal.LOSE_WEIGHT
                ),
                selectedGoal = Goal.MAINTAIN_WEIGHT,
                maintenanceCalories = 2650,
                macroGoals = MacroGoals(calories = 2650, protein = 148, carbs = 345, fat = 52)
            ),
            callbacks = NutritionGoalsCallbacks.default(),
            onBack = {},
            onNavigateToPersonalInformation = {},
            onNavigateToPastGoal = {}
        )
    }
}
