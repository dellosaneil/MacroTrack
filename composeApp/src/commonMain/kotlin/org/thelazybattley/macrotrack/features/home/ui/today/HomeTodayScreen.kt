package org.thelazybattley.macrotrack.features.home.ui.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_a_meal
import macrotrack.composeapp.generated.resources.breakfast
import macrotrack.composeapp.generated.resources.burned
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.dinner
import macrotrack.composeapp.generated.resources.eaten
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.goal
import macrotrack.composeapp.generated.resources.ic_heartbeat_lightly_active
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.kcal_burned_steps
import macrotrack.composeapp.generated.resources.lunch
import macrotrack.composeapp.generated.resources.meals_logged_today
import macrotrack.composeapp.generated.resources.p_c_f
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.remaining
import macrotrack.composeapp.generated.resources.steps_today
import macrotrack.composeapp.generated.resources.view
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.home.HomeTabCallbacks
import org.thelazybattley.macrotrack.features.home.HomeTabViewState
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

@Composable
fun HomeTodayScreen(
    modifier: Modifier = Modifier,
    viewState: HomeTabViewState,
    callbacks: HomeTabCallbacks,
    onNavigate: (MacroTrackDestination) -> Unit
) {
    LaunchedEffect(key1 = viewState.isNavigationTriggered) {
        if (viewState.isNavigationTriggered) {
            onNavigate(viewState.onNavigation ?: return@LaunchedEffect)
        }
    }
    LaunchedEffect(key1 = Unit) {
        callbacks.resetNavigation()
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        overscrollEffect = null
    ) {
        item {
            CaloriesCard(
                modifier = Modifier.fillMaxWidth(),
                goalCalories = viewState.macroGoals?.calories ?: 0
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                MacroCardDetails(
                    macro = Res.string.protein,
                    modifier = Modifier.weight(weight = 1f),
                    currentValue = 67,
                    goalValue = viewState.macroGoals?.protein ?: 0,
                    macroColor = colors.deepBlue
                )
                MacroCardDetails(
                    macro = Res.string.carbs,
                    modifier = Modifier.weight(weight = 1f),
                    currentValue = 23,
                    goalValue = viewState.macroGoals?.carbs ?: 0,
                    macroColor = colors.green
                )
                MacroCardDetails(
                    macro = Res.string.fat,
                    modifier = Modifier.weight(weight = 1f),
                    currentValue = 34,
                    goalValue = viewState.macroGoals?.fat ?: 0,
                    macroColor = colors.orange
                )
            }
        }
        item {
            StepDetailsCard(
                modifier = Modifier.fillMaxWidth(),
                steps = 521,
                goalSteps = 2000,
                burned = 50
            )
        }
        item {
            LoggedMealsCard(modifier = Modifier.fillMaxWidth()) {
                callbacks.onNavigation(destination = MacroTrackDestination.ADD_MEAL)
            }
        }
    }
}

@Composable
private fun LoggedMealsCard(
    modifier: Modifier,
    addMealTriggered: () -> Unit
) {
    Text(
        text = stringResource(resource = Res.string.meals_logged_today),
        color = colors.black,
        style = typography.bold15
    )
    Spacer(modifier = Modifier.height(height = 8.dp))
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        )
    ) {
        LoggedMealsDetails(
            modifier = Modifier.fillMaxWidth(),
            meal = Res.string.breakfast
        )
        HorizontalDivider(thickness = 1.dp, color = colors.lightGray)
        LoggedMealsDetails(
            modifier = Modifier.fillMaxWidth(),
            meal = Res.string.lunch
        )
        HorizontalDivider(thickness = 1.dp, color = colors.lightGray)
        LoggedMealsDetails(
            modifier = Modifier.fillMaxWidth(),
            meal = Res.string.dinner
        )
        HorizontalDivider(thickness = 1.dp, color = colors.lightGray)
        Text(
            text = stringResource(resource = Res.string.add_a_meal),
            color = colors.deepBlue,
            style = typography.bold14,
            modifier = Modifier.padding(all = 16.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .clickable {
                    addMealTriggered()
                }

        )
    }
}

@Composable
private fun LoggedMealsDetails(
    modifier: Modifier = Modifier,
    meal: StringResource
) {
    Row(
        modifier = modifier.padding(all = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(size = 36.dp)
                .background(
                    color = colors.iceBlue,
                    shape = RoundedCornerShape(size = 4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_heartbeat_lightly_active),
                contentDescription = null,
                modifier = Modifier.size(size = 16.dp),
                tint = colors.blue
            )
        }
        Column {
            Text(
                text = stringResource(resource = meal),
                color = colors.black,
                style = typography.bold13
            )
            Text(
                text = stringResource(resource = Res.string.p_c_f, 100, 100, 100),
                color = colors.mediumGray,
                style = typography.regular10
            )
        }
        Spacer(modifier = Modifier.weight(weight = 1f))
        Text(
            text = stringResource(resource = Res.string.kcal, 200),
            color = colors.black,
            style = typography.bold13
        )
    }
}

@Composable
private fun StepDetailsCard(
    modifier: Modifier,
    steps: Int,
    goalSteps: Int,
    burned: Int
) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1500
        )
    )
    LaunchedEffect(key1 = Unit) {
        progress = steps.toFloat() / goalSteps.toFloat()
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.iceBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.skyBlue,
                        shape = RoundedCornerShape(size = 8.dp)
                    ).size(size = 42.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_heartbeat_lightly_active),
                    contentDescription = null,
                    modifier = Modifier.size(size = 16.dp),
                    tint = colors.blue
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(space = 4.dp),
                modifier = Modifier.weight(weight = 1f)
            ) {
                Text(
                    text = stringResource(resource = Res.string.steps_today, steps),
                    color = colors.deepBlue,
                    style = typography.bold13
                )
                Text(
                    text = stringResource(
                        resource = Res.string.kcal_burned_steps,
                        burned, (progress * 100f).roundToInt()
                    ),
                    color = colors.deepBlue,
                    style = typography.regular10
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.lightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(color = colors.deepBlue, shape = RoundedCornerShape(8.dp))
                    )
                }
            }
            Text(
                text = stringResource(resource = Res.string.view),
                color = colors.deepBlue,
                style = typography.bold13
            )
        }
    }
}

@Composable
private fun CaloriesCard(
    modifier: Modifier = Modifier,
    goalCalories: Int
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(size = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var progress by remember { mutableStateOf(0f) }
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(
                    durationMillis = 1500
                )
            )
            LaunchedEffect(key1 = Unit) {
                progress = 0.5f
            }

            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 90.dp),
                    color = colors.deepBlue,
                    strokeWidth = 10.dp,
                    trackColor = colors.lightGray,
                    gapSize = 0.dp,
                    progress = { animatedProgress }
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = goalCalories.toString(),
                        color = colors.black,
                        style = typography.bold15
                    )
                    Text(
                        text = stringResource(resource = Res.string.remaining),
                        color = colors.mediumGray,
                        style = typography.regular8
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                CalorieCardDetails(
                    modifier = Modifier,
                    title = Res.string.goal,
                    kcal = goalCalories,
                )
                HorizontalDivider(color = colors.lightGray)
                CalorieCardDetails(
                    modifier = Modifier,
                    title = Res.string.eaten,
                    kcal = 2000,
                )
                HorizontalDivider(color = colors.lightGray)
                CalorieCardDetails(
                    modifier = Modifier,
                    title = Res.string.burned,
                    kcal = 2000,
                    color = colors.green
                )
            }
        }
    }
}

@Composable
private fun MacroCardDetails(
    modifier: Modifier = Modifier,
    macro: StringResource,
    currentValue: Int,
    goalValue: Int,
    macroColor: Color
) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1500
        )
    )
    LaunchedEffect(key1 = goalValue) {
        if (goalValue == 0) return@LaunchedEffect
        progress = currentValue.toFloat() / goalValue.toFloat()
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.white
        ),
        border = BorderStroke(width = 1.dp, color = colors.lightGray)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = stringResource(resource = macro),
                color = colors.gray,
                style = typography.regular10
            )
            Text(
                text = "${currentValue}g",
                style = typography.bold16,
                color = macroColor
            )
            Text(
                text = "/ ${goalValue}g",
                style = typography.regular10,
                color = colors.mediumGray
            )
            Spacer(modifier = Modifier.height(height = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.lightGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(color = macroColor, shape = RoundedCornerShape(8.dp))
                )
            }
        }

    }
}

@Composable
private fun CalorieCardDetails(
    modifier: Modifier = Modifier,
    title: StringResource,
    kcal: Int,
    color: Color = colors.black,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(resource = title),
            color = colors.gray,
            style = typography.regular13
        )
        Text(
            text = stringResource(resource = Res.string.kcal, kcal),
            color = color,
            style = typography.bold13
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
private fun PreviewHomeTodayScreen() {
    MacroTrackTheme {
        HomeTodayScreen(
            viewState = HomeTabViewState(),
            callbacks = HomeTabCallbacks.default(),
        ) {

        }
    }
}
