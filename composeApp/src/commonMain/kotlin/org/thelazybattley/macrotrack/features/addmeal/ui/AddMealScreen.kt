package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_meal
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonBackButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<AddMealViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        containerColor = colors.white
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
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        TitleBar(modifier = Modifier.fillMaxWidth())
        MealTypeScreen(
            modifier = Modifier.fillMaxWidth(), callbacks = callbacks,
            selectedMealType = viewState.selectedMealType
        )
    }
}

@Composable
private fun MealTypeScreen(
    modifier: Modifier = Modifier,
    selectedMealType: MealType,
    callbacks: AddMealCallbacks
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {

        MealType.entries.forEach { mealType ->
            MealTypeCard(
                modifier = Modifier.weight(weight = 1f),
                mealType = mealType,
                isSelected = selectedMealType == mealType,
            ) {
                callbacks.onMealTypeSelected(mealType = mealType)
            }
        }
    }
}

@Composable
private fun MealTypeCard(
    modifier: Modifier = Modifier,
    mealType: MealType,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    val containerColor = if (isSelected) {
        colors.deepBlue
    } else {
        colors.offWhite
    }
    val textColor = if (isSelected) {
        colors.white
    } else {
        colors.gray
    }


    Card(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(size = 8.dp)
            ).clickable {
                onSelected()
            },
        shape = RoundedCornerShape(size = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(resource = mealType.title),
                style = typography.bold12,
                color = textColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
    ) {
        CommonBackButton(modifier = Modifier.align(alignment = Alignment.CenterStart)) {

        }
        Text(
            text = stringResource(resource = Res.string.add_meal),
            style = typography.bold16,
            color = colors.black,
            modifier = Modifier.align(alignment = Alignment.Center)
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewAddMealScreen() {
    MacroTrackTheme {
        AddMealScreen(
            modifier = Modifier,
            viewState = AddMealViewState(),
            callbacks = AddMealCallbacks.default()
        )
    }
}
