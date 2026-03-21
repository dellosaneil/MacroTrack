package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun OnboardingActivityAndTargets(
    modifier: Modifier = Modifier, viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography

}

@Preview
@Composable
private fun Preview() {
    OnboardingActivityAndTargets(
        modifier = Modifier,
        viewState = OnboardingViewState(),
        callbacks = OnboardingCallbacks.default()
    )
}
