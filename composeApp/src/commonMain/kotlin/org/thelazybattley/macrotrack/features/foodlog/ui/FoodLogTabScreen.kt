package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_meal
import macrotrack.composeapp.generated.resources.c_value_gram
import macrotrack.composeapp.generated.resources.f_value_gram
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.p_value_gram
import macrotrack.composeapp.generated.resources.value_gram
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.features.foodlog.FoodLogCallbacks
import org.thelazybattley.macrotrack.features.foodlog.FoodLogFoodListByMealType
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewModel
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewState
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography


@Composable
fun FoodLogTabScreen(
    modifier: Modifier = Modifier,
    onNavigate: (MacroTrackDestination) -> Unit
) {
    val viewModel = koinViewModel<FoodLogViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    FoodLogTabScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onNavigate = onNavigate
    )
}

@Composable
private fun FoodLogTabScreen(
    modifier: Modifier = Modifier,
    viewState: FoodLogViewState,
    callbacks: FoodLogCallbacks,
    onNavigate: (MacroTrackDestination) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        stickyHeader {
            FoodLogTotalMacros(
                modifier = Modifier.fillMaxWidth(),
                viewState = viewState
            )
        }
        loggedFoodByMealType(
            foodList = viewState.breakfast
        ) {
            onNavigate(MacroTrackDestination.ADD_MEAL)
        }
        loggedFoodByMealType(
            foodList = viewState.lunch
        ) {
            onNavigate(MacroTrackDestination.ADD_MEAL)
        }
        loggedFoodByMealType(
            foodList = viewState.dinner
        ) {
            onNavigate(MacroTrackDestination.ADD_MEAL)
        }
        loggedFoodByMealType(
            foodList = viewState.snack
        ) {
            onNavigate(MacroTrackDestination.ADD_MEAL)
        }
    }
}

private fun LazyListScope.loggedFoodByMealType(
    foodList: FoodLogFoodListByMealType,
    onNavigate: () -> Unit
) {
    item {
        Spacer(modifier = Modifier)
    }
    item {
        FoodLogMeals(
            modifier = Modifier.fillMaxWidth(), mealType = foodList.mealType,
            totalCalories = foodList.calories
        )
    }
    items(items = foodList.foodList, key = { it.id }) { food ->
        FoodLogItem(
            modifier = Modifier.fillMaxWidth(),
            food = food
        )
    }
    item {
        AddMealButton(modifier = Modifier.fillMaxWidth()) {
            onNavigate()
        }
    }
}

@Composable
private fun FoodLogItem(modifier: Modifier = Modifier, food: FoodLog) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Text(
                text = food.foodName,
                style = typography.bold13,
                color = colors.black
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(resource = Res.string.value_gram, food.weight.toInt()),
                    style = typography.bold11,
                    color = colors.mediumGray
                )
                Spacer(modifier = Modifier.width(width = 12.dp))
                Text(
                    text = macroNutrientDetails(
                        protein = food.protein.toInt(),
                        fat = food.fat.toInt(),
                        carbs = food.carbs.toInt()
                    ),
                    style = typography.bold10
                )
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(
                    text = stringResource(resource = Res.string.kcal, food.calories),
                    style = typography.bold13,
                    color = colors.black
                )
            }
        }
    }
}

@Composable
private fun macroNutrientDetails(
    protein: Int,
    fat: Int,
    carbs: Int
): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MacroType.PROTEIN.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.p_value_gram, protein))

        }
        withStyle(style = SpanStyle(color = colors.black)) {
            append("\t·\t")
        }
        withStyle(
            style = SpanStyle(
                color = MacroType.FAT.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.f_value_gram, fat))

        }
        withStyle(style = SpanStyle(color = colors.black)) {
            append("\t·\t")
        }
        withStyle(
            style = SpanStyle(
                color = MacroType.CARBS.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.c_value_gram, carbs))
        }
    }
}

@Composable
private fun FoodLogMeals(
    modifier: Modifier = Modifier,
    mealType: MealType,
    totalCalories: Int,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(resource = mealType.title),
            color = colors.black,
            style = typography.bold14
        )
        Text(
            text = stringResource(resource = Res.string.kcal, totalCalories),
            style = typography.bold11,
            color = colors.deepBlue
        )
    }
}

@Composable
private fun AddMealButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(size = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 1.dp,
            color = colors.skyBlue
        )
    ) {
        Text(
            text = stringResource(resource = Res.string.add_meal),
            style = typography.bold13,
            color = colors.deepBlue
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewFoodLogTabScreen() {
    MacroTrackTheme {
        FoodLogTabScreen(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            viewState = FoodLogViewState(
                breakfast = FoodLogFoodListByMealType(
                    mealType = MealType.BREAKFAST,
                    foodList = listOf(
                        dummyFoodLog,
                        dummyFoodLog.copy(id = 2),
                        dummyFoodLog.copy(id = 3),
                    )
                )
            ),
            callbacks = FoodLogCallbacks.default()
        ) {

        }
    }
}
