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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import macrotrack.composeapp.generated.resources.stripe_dominant_macro
import macrotrack.composeapp.generated.resources.undo
import macrotrack.composeapp.generated.resources.value_added
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.MacroType
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

    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = viewState.latestLoggedFoodName) {
        if(viewState.latestLoggedFoodName.isEmpty()) {
            snackBarHostState.currentSnackbarData?.dismiss()
        }
        if(viewState.latestLoggedFoodName.isNotEmpty()) {
            snackBarHostState.showSnackbar(message = viewState.latestLoggedFoodName)
        }
    }
    Scaffold(
        modifier = modifier,
        containerColor = colors.white,
        topBar = {
            TitleBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(paddingValues = AppPadding),
                mealType = viewState.selectedMealType
            ) {
                onBackButtonPressed()
            }
        },
        snackbarHost = {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier.align(alignment = Alignment.TopCenter)
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
        }
    ) { innerPadding ->
        AddMealScreen(
            modifier = modifier
                .padding(paddingValues = innerPadding),
            viewState = viewState,
            callbacks = viewModel,
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
        DominantMacroLegend(modifier = Modifier.fillMaxWidth())
        AddMealCreateFood(
            modifier = Modifier.fillMaxWidth()
        ) {
            callbacks.onNavigateScreen(destination = AppDestinations.Root.AddFood)
        }
        LazyColumn {
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
                HorizontalDivider(
                    thickness = 1.dp, color = colors.lightGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DominantMacroLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        ),
        border = BorderStroke(width = 1.dp, color = colors.lightGray)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.stripe_dominant_macro),
                style = typography.bold14,
                color = colors.gray
            )
            Row(
                modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DominantMacroItem(modifier = Modifier, macroType = MacroType.PROTEIN)
                DominantMacroItem(modifier = Modifier, macroType = MacroType.CARBS)
                DominantMacroItem(modifier = Modifier, macroType = MacroType.FAT)
            }
        }

    }
}

@Composable
private fun DominantMacroItem(modifier: Modifier = Modifier, macroType: MacroType) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 3.dp))
                .size(size = 10.dp)
                .background(color = macroType.toColor())
        )
        Text(
            text = stringResource(resource = macroType.text()),
            style = typography.regular13,
            color = macroType.toColor()
        )
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
        val textRes = when (mealType) {
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
                    .background(color = colors.lightGreen)
                    .padding(all = 4.dp)
                    .clickable {
                        onClick()
                    }

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
                viewState = AddMealViewState(
                    filteredFoodList = listOf(
                        dummyFood.copy(name = "Rice 1"),
                        dummyFood.copy(name = "Rice 2"),
                        dummyFood.copy(name = "Rice 3")
                    )
                ),
                callbacks = AddMealCallbacks.default(),
            )
        }
    }
}
