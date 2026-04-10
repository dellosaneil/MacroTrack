package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_breakfast
import macrotrack.composeapp.generated.resources.add_dinner
import macrotrack.composeapp.generated.resources.add_lunch
import macrotrack.composeapp.generated.resources.add_snack
import macrotrack.composeapp.generated.resources.ic_checkmark
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.search_food
import macrotrack.composeapp.generated.resources.undo
import macrotrack.composeapp.generated.resources.value_added
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.addmeal.tabs.food.AddFoodCallbacks
import org.thelazybattley.macrotrack.features.addmeal.tabs.food.ui.AddFoodTabScreen
import org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.AddRecipeCallbacks
import org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.ui.AddRecipeTabScreen
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealScreen(
    modifier: Modifier = Modifier,
    onBackButtonPressed: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<AddMealViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.destinationRoute) {
        viewState.destinationRoute?.let { route ->
            onNavigate(route)
            viewModel.resetNavigateScreen()
        }
    }

    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = viewState.loggedMeals.name) {
        if (viewState.loggedMeals.name.isEmpty()) {
            snackBarHostState.currentSnackbarData?.dismiss()
        }
        if (viewState.loggedMeals.name.isNotEmpty()) {
            snackBarHostState.showSnackbar(message = viewState.loggedMeals.name)
        }
    }
    Scaffold(
        modifier = modifier,
        containerColor = colors.white,
        topBar = {
            val textRes = when (viewState.selectedMealType) {
                MealType.BREAKFAST -> Res.string.add_breakfast
                MealType.LUNCH -> Res.string.add_lunch
                MealType.DINNER -> Res.string.add_dinner
                MealType.SNACK -> Res.string.add_snack
            }
            CommonTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(paddingValues = AppPadding),
                stringResource = textRes,
                onClick = onBackButtonPressed
            )
        },
        bottomBar = {
            if (viewState.loggedMeals.loggedMeals.isNotEmpty()) {
                AddMealSummary(
                    modifier = Modifier.fillMaxWidth().padding(paddingValues = AppPadding),
                    loggedFood = viewState.loggedMeals,
                    onDoneClicked = {
                        onBackButtonPressed()
                    }
                )
            }
        }
    ) { innerPadding ->
        AddMealScreen(
            modifier = Modifier.padding(paddingValues = innerPadding),
            viewState = viewState,
            addMealCallbacks = viewModel,
            addFoodCallbacks = viewModel,
            addRecipeCallbacks = viewModel
        )
    }
    SnackbarHost(
        hostState = snackBarHostState,
        modifier = Modifier
            .statusBarsPadding()
    ) { snackBarData ->
        MealAddedSnackBar(
            modifier = Modifier.padding(
                paddingValues = AppPadding
            ),
            foodName = snackBarData.visuals.message
        ) {
            viewModel.onRevertLog()
        }
    }
}

@Composable
private fun AddMealScreen(
    modifier: Modifier = Modifier,
    viewState: AddMealViewState,
    addMealCallbacks: AddMealCallbacks,
    addFoodCallbacks: AddFoodCallbacks,
    addRecipeCallbacks: AddRecipeCallbacks
) {
    Column(
        modifier = modifier
            .padding(paddingValues = AppPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = Res.string.search_food,
            prefixIcon = Res.drawable.ic_search,
            textValue = viewState.searchQuery
        ) { query ->
            addMealCallbacks.onSearchQuery(query = query)
        }
        AddMealFilter(
            modifier = Modifier.fillMaxWidth(),
            viewState = viewState,
            onFilterSelected = { filter ->
                addMealCallbacks.onMealFilterSelected(mealFilter = filter)
            }
        )
        AddMealLegend(modifier = Modifier.fillMaxWidth())
        AddMealCreateButton(
            modifier = Modifier.fillMaxWidth(),
            mealFilter = viewState.selectedMealFilter
        ) {
            val route = when (viewState.selectedMealFilter) {
                MealFilter.FOODS -> AppDestinations.Root.CreateFood.route
                MealFilter.RECIPES -> AppDestinations.Root.CreateRecipe.createRoute(recipeName = "")
            }
            addMealCallbacks.onNavigateScreen(route = route)
        }
        when (viewState.selectedMealFilter) {
            MealFilter.FOODS -> {
                AddFoodTabScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewState = viewState,
                    addFoodCallbacks = addFoodCallbacks
                )
            }

            MealFilter.RECIPES -> {
                AddRecipeTabScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewState = viewState,
                    callbacks = addRecipeCallbacks
                )
            }
        }
    }
}

@Composable
private fun MealAddedSnackBar(
    modifier: Modifier = Modifier,
    foodName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.lightAqua,
            contentColor = colors.mossGreen
        ),
        shape = RoundedCornerShape(size = 8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colors.lightGreen
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(all = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Box(
                modifier = Modifier.clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = colors.freshGreen)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_checkmark),
                    contentDescription = null,
                    modifier = Modifier.padding(all = 8.dp),
                    tint = colors.white
                )
            }
            Text(
                text = stringResource(resource = Res.string.value_added, foodName),
                style = typography.bold12,
            )
            Spacer(modifier = Modifier.weight(weight = 1f))
            Text(
                text = stringResource(resource = Res.string.undo),
                style = typography.bold12,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .clickable {
                        onClick()
                    }
                    .background(color = colors.lightGreen)
                    .padding(all = 4.dp)
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun PreviewMealAddedSnackBar() {
    MealAddedSnackBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        foodName = "Chicken"
    ) {

    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealScreen() {
    MacroTrackTheme {
        Scaffold(
            containerColor = colors.white
        ) {
            AddMealScreen(
                modifier = Modifier.padding(it),
                viewState = AddMealViewState(),
                addMealCallbacks = AddMealCallbacks.default(),
                addFoodCallbacks = AddFoodCallbacks.default(),
                addRecipeCallbacks = AddRecipeCallbacks.default()
            )
        }
    }
}
