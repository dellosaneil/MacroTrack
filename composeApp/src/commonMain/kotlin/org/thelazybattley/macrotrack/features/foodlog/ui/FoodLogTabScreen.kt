package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.nothing_logged_for_value
import macrotrack.composeapp.generated.resources.recipe
import macrotrack.composeapp.generated.resources.sign_add
import macrotrack.composeapp.generated.resources.value_gram
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.buildMacroNutrientText
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.features.foodlog.FoodLogCallbacks
import org.thelazybattley.macrotrack.features.foodlog.FoodLogFoodListByMealType
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewModel
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewState
import org.thelazybattley.macrotrack.ui.common.CommonSurface
import org.thelazybattley.macrotrack.ui.common.CommonSwipeToDismissBox
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography


@Composable
fun FoodLogTabScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<FoodLogViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewState.navigateMealTypeParameter) {
        if (viewState.navigateMealTypeParameter != null) {
            onNavigate(
                AppDestinations.Root.AddMeal.createRoute(
                    mealType = viewState.navigateMealTypeParameter?.name ?: ""
                )
            )
            viewModel.resetNavigateMealTypeParameter()
        }
    }

    FoodLogTabScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel
    )
}

@Composable
private fun FoodLogTabScreen(
    modifier: Modifier = Modifier,
    viewState: FoodLogViewState,
    callbacks: FoodLogCallbacks
) {
    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        FoodLogTotalMacros(
            modifier = Modifier.fillMaxWidth(),
            viewState = viewState
        )
        LoggedFoodMealType(
            foodList = viewState.breakfast,
            onDeleteFoodLog = { id ->
                callbacks.onDeleteFoodLog(id = id)
            },
            onNavigate = {
                callbacks.onNavigate(mealType = MealType.BREAKFAST)
            }
        )

        LoggedFoodMealType(
            foodList = viewState.lunch,
            onDeleteFoodLog = { id ->
                callbacks.onDeleteFoodLog(id = id)
            },
            onNavigate = {
                callbacks.onNavigate(mealType = MealType.LUNCH)
            }
        )

        LoggedFoodMealType(
            foodList = viewState.dinner,
            onDeleteFoodLog = { id ->
                callbacks.onDeleteFoodLog(id = id)
            },
            onNavigate = {
                callbacks.onNavigate(mealType = MealType.DINNER)
            }
        )

        LoggedFoodMealType(
            foodList = viewState.snack,
            onDeleteFoodLog = { id ->
                callbacks.onDeleteFoodLog(id = id)
            },
            onNavigate = {
                callbacks.onNavigate(mealType = MealType.SNACK)
            }
        )

    }
}


@Composable
private fun LoggedFoodMealType(
    modifier: Modifier = Modifier,
    foodList: FoodLogFoodListByMealType,
    onDeleteFoodLog: (Long) -> Unit,
    onNavigate: () -> Unit
) {
    Column(
        modifier = modifier,
    ) {
        FoodLogMealType(
            modifier = Modifier.fillMaxWidth(), mealType = foodList.mealType,
            totalCalories = foodList.calories,
            onNavigate = onNavigate
        )
        Spacer(modifier = Modifier.height(height = 4.dp))
        if (foodList.foodList.isEmpty()) {
            val mealTypeText = stringResource(resource = foodList.mealType.title)
            Text(
                text = stringResource(resource = Res.string.nothing_logged_for_value, mealTypeText),
                style = typography.regular11,
                color = colors.mediumGray,
                modifier = Modifier.fillMaxWidth()
                    .border(
                        width = 1.dp, color = colors.mediumGray,
                        shape = RoundedCornerShape(size = 8.dp)
                    )
                    .padding(all = 12.dp),
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = colors.lightGray,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .padding(all = 8.dp)
        ) {
            foodList.foodList.forEachIndexed { index, food ->
                key(food.id) {
                    CommonSwipeToDismissBox(
                        modifier = Modifier,
                        onSwipeToDismiss = {
                            onDeleteFoodLog(food.id)
                        }
                    ) {
                        FoodLogItem(
                            modifier = Modifier.fillMaxWidth(),
                            food = food
                        )
                    }
                }
                if (index == foodList.foodList.lastIndex) return@forEachIndexed
                Spacer(modifier = Modifier.height(height = 8.dp))
                HorizontalDivider(thickness = 1.dp, color = colors.lightGray)
            }
        }
    }
}

@Composable
private fun FoodLogItem(modifier: Modifier = Modifier, food: FoodLog) {
    val color = food.dominantMacro.toColor()
    Box(
        modifier = modifier
            .clip(shape = CardDefaults.shape)
            .drawWithContent {
                val height = size.height - 8.dp.toPx()
                drawContent()
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x = 2.dp.toPx(), y = 4.dp.toPx()),
                    size = size.copy(width = 4.dp.toPx(), height = height),
                    cornerRadius = CornerRadius(x = 8.dp.toPx(), y = 8.dp.toPx())
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth().padding(
                    top = 4.dp, bottom = 4.dp,
                    start = 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(
                    text = food.foodName,
                    style = typography.bold13,
                    color = colors.black
                )
                if (food.weight == 0.0) {
                    Box(
                        modifier = Modifier.background(
                            color = colors.paleBlue,
                            shape = RoundedCornerShape(size = 4.dp)
                        )
                    ) {
                        Text(
                            text = stringResource(resource = Res.string.recipe),
                            style = typography.bold10,
                            color = colors.deepBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildMacroNutrientText(
                        protein = food.protein.toInt(),
                        fat = food.fat.toInt(),
                        carbs = food.carbs.toInt()
                    ),
                    style = typography.bold10
                )
                if (food.weight != 0.0) {
                    Spacer(modifier = Modifier.width(width = 12.dp))
                    Text(
                        text = stringResource(
                            resource = Res.string.value_gram,
                            food.weight.toInt()
                        ),
                        style = typography.bold11,
                        color = colors.mediumGray
                    )
                }
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(
                    text = stringResource(resource = Res.string.kcal, food.calories),
                    style = typography.bold12,
                    color = colors.black
                )
            }
        }
    }
}


@Composable
private fun FoodLogMealType(
    modifier: Modifier = Modifier,
    mealType: MealType,
    totalCalories: Int,
    onNavigate: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(resource = mealType.title),
            color = colors.black,
            style = typography.bold12
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
        if (totalCalories > 0) {
            Text(
                text = stringResource(resource = Res.string.kcal, totalCalories),
                style = typography.bold11,
                color = colors.gray
            )
        }
        CommonSurface(
            shape = RoundedCornerShape(size = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clickable {
                        onNavigate()
                    }
                    .border(
                        width = 1.dp,
                        color = colors.skyBlue,
                        shape = RoundedCornerShape(size = 6.dp)
                    )
                    .background(color = colors.paleBlue),
            ) {
                Text(
                    text = stringResource(resource = Res.string.sign_add),
                    style = typography.bold11,
                    color = colors.deepBlue,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewFoodLogTabScreen() {
    MacroTrackTheme {
        FoodLogTabScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            viewState = FoodLogViewState(
                breakfast = FoodLogFoodListByMealType(
                    mealType = MealType.BREAKFAST,
                    foodList = listOf(
                        dummyFoodLog.copy(
                            weight = 0.0
                        ),
                        dummyFoodLog.copy(id = 2),
                        dummyFoodLog.copy(id = 3),
                    )
                )
            ),
            callbacks = FoodLogCallbacks.default()
        )
    }
}
