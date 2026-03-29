package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_meal
import macrotrack.composeapp.generated.resources.kcal
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.foodlog.FoodLogCallbacks
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
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        stickyHeader {
            FoodLogCaloriesEaten()
        }
        item {
            FoodLogMeals(modifier = Modifier.fillMaxWidth(), mealType = MealType.BREAKFAST)
        }
        items(items = viewState.breakfastFood) {
            Text(text = it.toString())
        }
        item {
            AddMealButton(modifier = Modifier.fillMaxWidth()) {
                onNavigate(MacroTrackDestination.ADD_MEAL)
            }
        }


        item {
            FoodLogMeals(modifier = Modifier.fillMaxWidth(), mealType = MealType.LUNCH)
        }
        items(items = viewState.lunchFood) {
            Text(text = it.toString())
        }
        item {
            AddMealButton(modifier = Modifier.fillMaxWidth()) {

                onNavigate(MacroTrackDestination.ADD_MEAL)
            }
        }

        item {
            FoodLogMeals(modifier = Modifier.fillMaxWidth(), mealType = MealType.DINNER)
        }
        items(items = viewState.dinnerFood) {
            Text(text = it.toString())
        }
        item {
            AddMealButton(modifier = Modifier.fillMaxWidth()) {

                onNavigate(MacroTrackDestination.ADD_MEAL)
            }
        }

        item {
            FoodLogMeals(modifier = Modifier.fillMaxWidth(), mealType = MealType.SNACK)
        }
        items(items = viewState.snackFood) {
            Text(text = it.toString())
        }
        item {
            AddMealButton(modifier = Modifier.fillMaxWidth()) {
                onNavigate(MacroTrackDestination.ADD_MEAL)
            }
        }
    }
}

@Composable
private fun FoodLogMeals(
    modifier: Modifier = Modifier,
    mealType: MealType
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
            text = stringResource(resource = Res.string.kcal, 333),
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
                .padding(all = 16.dp)
        ) {

        }
    }
}
