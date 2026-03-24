package org.thelazybattley.macrotrack.features.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.home.HomeTabCallbacks
import org.thelazybattley.macrotrack.features.home.HomeTabViewModel
import org.thelazybattley.macrotrack.features.home.HomeTabViewState
import org.thelazybattley.macrotrack.features.home.ui.today.HomeTodayScreen
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun HomeTabScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<HomeTabViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    HomeTabScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel
    )
}

@Composable
fun HomeTabScreen(
    modifier: Modifier = Modifier,
    viewState: HomeTabViewState,
    callbacks: HomeTabCallbacks
) {
    HomeTodayScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = callbacks
    )
}


@Preview
@Composable
private fun PreviewHomeTabScreen() {
    MacroTrackTheme {
        HomeTabScreen(
            viewState = HomeTabViewState(),
            callbacks = HomeTabCallbacks.default(),
        )
    }
}
