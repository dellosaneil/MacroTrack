package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_breakfast
import macrotrack.composeapp.generated.resources.add_dinner
import macrotrack.composeapp.generated.resources.add_lunch
import macrotrack.composeapp.generated.resources.add_snack
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.search_food
import macrotrack.composeapp.generated.resources.value_added
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.addmeal.tabs.food.ui.AddFoodTabScreen
import org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.ui.AddRecipeTabScreen
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonSnackBarContent
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

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
            mainCallbacks = viewModel,
            recipeCallbacks = viewModel,
            foodCallbacks = viewModel
        )
    }
    SnackbarHost(
        hostState = snackBarHostState,
        modifier = Modifier
            .statusBarsPadding()
    ) { snackBarData ->
        CommonSnackBarContent(
            modifier = Modifier.padding(paddingValues = AppPadding),
            text = stringResource(resource = Res.string.value_added, snackBarData.visuals.message)
        ) {
            viewModel.onRevertLog()
        }
    }
}

@Composable
private fun AddMealScreen(
    modifier: Modifier = Modifier,
    viewState: AddMealViewState,
    mainCallbacks: AddMealCallbacks.MainScreenCallbacks,
    foodCallbacks: AddMealCallbacks.FoodCallbacks,
    recipeCallbacks: AddMealCallbacks.RecipeCallbacks
) {
    Column(
        modifier = modifier
            .padding(paddingValues = AppPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(resource = Res.string.search_food),
            prefixIcon = Res.drawable.ic_search,
            textValue = viewState.searchQuery
        ) { query ->
            mainCallbacks.onSearchQuery(query = query)
        }
        AddMealFilter(
            modifier = Modifier.fillMaxWidth(),
            viewState = viewState,
            onFilterSelected = { filter ->
                mainCallbacks.onMealFilterSelected(mealFilter = filter)
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
            mainCallbacks.onNavigateScreen(route = route)
        }
        when (viewState.selectedMealFilter) {
            MealFilter.FOODS -> {
                AddFoodTabScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewState = viewState,
                    callbacks = foodCallbacks
                )
            }

            MealFilter.RECIPES -> {
                AddRecipeTabScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewState = viewState,
                    callbacks = recipeCallbacks
                )
            }
        }
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
                mainCallbacks = AddMealCallbacks.defaultMainScreenCallbacks(),
                foodCallbacks = AddMealCallbacks.defaultFoodCallbacks(),
                recipeCallbacks = AddMealCallbacks.defaultRecipeCallbacks()
            )
        }
    }
}
