package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingScreen
import org.thelazybattley.macrotrack.features.splash.ui.SplashScreen
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
@Preview(showBackground = true)
fun SplashScreenNavigationNavigation() {
    val navController = rememberNavController()
    MacroTrackTheme {
        NavHost(
            navController = navController,
            startDestination = MacroTrackDestination.SPLASH_SCREEN.route
        ) {
            composable(route = MacroTrackDestination.ONBOARDING.route) {
                OnboardingScreen(
                    modifier = Modifier
                ) {
                    navController.navigate(route = MacroTrackDestination.MAIN.route)
                }
            }
            composable(route = MacroTrackDestination.MAIN.route) {
                MainScreenNavigation(
                    modifier = Modifier
                )
            }
            composable(route = MacroTrackDestination.SPLASH_SCREEN.route) {
                SplashScreen { destination ->
                    navController.navigate(
                        route = destination.route
                    ) {
                        popUpTo(route = MacroTrackDestination.SPLASH_SCREEN.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}

val AppPadding = PaddingValues(
    horizontal = 16.dp,
    vertical = 8.dp
)
