package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonBackButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealScreen(
    modifier: Modifier = Modifier,
    onBackButtonPressed: () -> Unit,
    onNavigate: (AppDestinations.Root) -> Unit
) {
    val viewModel = koinViewModel<AddMealViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.navigateDestination) {
        viewState.navigateDestination?.let { destination ->
            onNavigate(destination)
            viewModel.resetNavigateScreen()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.white,
        topBar = {
            TitleBar(
                modifier = Modifier.fillMaxWidth()
                    .statusBarsPadding().padding(paddingValues = AppPadding),
                mealType = viewState.selectedMealType

            ) {
                onBackButtonPressed()
            }
        }
    ) { innerPadding ->
        AddMealScreen(
            modifier = modifier
                .padding(paddingValues = innerPadding),
            viewState = viewState,
            callbacks = viewModel
        )
    }
}

@Composable
private fun AddMealScreen(
    modifier: Modifier = Modifier,
    viewState: AddMealViewState,
    callbacks: AddMealCallbacks
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues = AppPadding),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = Res.string.search_food,
            prefixIcon = Res.drawable.ic_search
        ) {

        }
        AddMealFoodFilterSelection(
            modifier = Modifier.fillMaxWidth(),
            callbacks = callbacks,
            viewState = viewState
        )
        AddMealCreateFood(
            modifier = Modifier.fillMaxWidth()
        ) {
            callbacks.onNavigateScreen(destination = AppDestinations.Root.AddFood)
        }
        LazyColumn {
            item {
                HorizontalDivider(thickness = 1.dp, color = colors.lightGray)
            }
            items(
                items = viewState.filteredFoodList,
                key = { food -> food.name }
            ) { food ->
                AddMealFoodDetails(
                    modifier = Modifier.fillMaxWidth(),
                    food = food
                ) {
                    callbacks.onInsertFoodLog(food = food)
                }
                HorizontalDivider(thickness = 1.dp, color = colors.lightGray)
            }
        }

    }
}

@Composable
private fun TitleBar(
    modifier: Modifier = Modifier,
    mealType: MealType,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
    ) {
        CommonBackButton(modifier = Modifier.align(alignment = Alignment.CenterStart)) {
            onClick()
        }
        val textRes = when(mealType) {
            MealType.BREAKFAST -> Res.string.add_breakfast
            MealType.LUNCH -> Res.string.add_lunch
            MealType.DINNER -> Res.string.add_dinner
            MealType.SNACK -> Res.string.add_snack
        }
        Text(
            text = stringResource(resource = textRes),
            style = typography.bold16,
            color = colors.black,
            modifier = Modifier.align(alignment = Alignment.Center)
        )
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
                viewState = AddMealViewState(
                    filteredFoodList = listOf(
                        dummyFood.copy(name = "Rice 1"),
                        dummyFood.copy(name = "Rice 2"),
                        dummyFood.copy(name = "Rice 3")
                    )
                ),
                callbacks = AddMealCallbacks.default()
            )
        }
    }
}
