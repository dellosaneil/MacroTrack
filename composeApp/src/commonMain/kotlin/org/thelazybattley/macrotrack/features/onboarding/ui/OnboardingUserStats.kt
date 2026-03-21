package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun OnboardingUserStats(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<OnboardingViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    OnboardingUserStats(modifier = modifier, viewState = viewState, callbacks = viewModel)
}

@Composable
fun OnboardingUserStats(
    modifier: Modifier = Modifier, viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography
    Scaffold(
        modifier = modifier,
        containerColor = colors.white
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {

        }
    }
}

@Preview
@Composable
private fun Preview() {
    OnboardingUserStats(
        modifier = Modifier,
        viewState = OnboardingViewState(),
        callbacks = OnboardingCallbacks.default()
    )
}
