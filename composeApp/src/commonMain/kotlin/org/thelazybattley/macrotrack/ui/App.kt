package org.thelazybattley.macrotrack.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingScreen
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
@Preview(showBackground = true)
fun App() {
    MacroTrackTheme {
        OnboardingScreen()
    }
}
