package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_meal
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.search_food
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonBackButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
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
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        TitleBar(modifier = Modifier.fillMaxWidth())
        AddMealTypeSelection(
            modifier = Modifier.fillMaxWidth(), callbacks = callbacks,
            selectedMealType = viewState.selectedMealType
        )
        MacrosDetail(modifier = Modifier.fillMaxWidth())
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = Res.string.search_food,
            prefixIcon = Res.drawable.ic_search
        ) {

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
                callbacks = AddMealCallbacks.default()
            )
        }
    }
}
