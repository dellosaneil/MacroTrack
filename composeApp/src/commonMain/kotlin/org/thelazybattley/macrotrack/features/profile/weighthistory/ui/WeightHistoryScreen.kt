package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.weight_history
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.to2Decimal
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryCallbacks
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewModel
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewState
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun WeightHistoryScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<WeightHistoryViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = colors.white,
        topBar = {
            CommonTopBar(
                stringResource = Res.string.weight_history,
                onClick = onPopBackStack
            )
        }
    ) { innerPadding ->
        WeightHistoryScreen(
            modifier = modifier.padding(paddingValues = innerPadding)
                .padding(paddingValues = AppPadding),
            viewState = viewState,
            callbacks = viewModel
        )
    }

}

@Composable
fun WeightHistoryScreen(
    modifier: Modifier = Modifier,
    viewState: WeightHistoryViewState,
    callbacks: WeightHistoryCallbacks
) {
    if (viewState.weightList.isEmpty()) return
    Column(
        modifier = modifier,
    ) {
        WeightHistoryStatistics(
            modifier = Modifier.fillMaxWidth(),
            viewState = viewState
        )
        WeightHistoryGraph(
            modifier = Modifier
                .height(height = 200.dp)
                .fillMaxWidth(),
            weightList = viewState.weightList
        )

        Text(
            text = viewState.weightList.map { it.weight }.average().to2Decimal().toString()
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryScreen() {
    MacroTrackTheme {
        WeightHistoryScreen(
            modifier = Modifier,
            viewState = WeightHistoryViewState(
                weightList = listOf(
                    Weight(
                        date = getCurrentDate(),
                        weight = 64.3,
                    ),
                    Weight(
                        date = getCurrentDate(),
                        weight = 66.3,
                    ),
                    Weight(
                        date = getCurrentDate(),
                        weight = 62.3,
                    ),
                )
            ),
            callbacks = WeightHistoryCallbacks.default()
        )
    }
}
