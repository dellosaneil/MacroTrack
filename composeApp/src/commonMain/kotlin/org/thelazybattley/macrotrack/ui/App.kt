package org.thelazybattley.macrotrack.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingScreen
import org.thelazybattley.macrotrack.features.splash.ui.SplashScreen
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
@Preview(showBackground = true)
fun App() {
    val navController = rememberNavController()
    val currentDestination: MacroTrackDestination by remember { mutableStateOf(value = MacroTrackDestination.HOME) }

    MacroTrackTheme {
        Scaffold(
            containerColor = colors.white
        ) { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues = paddingValues),
                navController = navController,
                startDestination = MacroTrackDestination.SPLASH_SCREEN.route
            ) {
                composable(route = MacroTrackDestination.ONBOARDING.route) {
                    OnboardingScreen {
                        navController.navigate(route = MacroTrackDestination.HOME.route)
                    }
                }
                composable(route = MacroTrackDestination.HOME.route) {
                    Text(text = "HOME!")
                }
                composable(route = MacroTrackDestination.SPLASH_SCREEN.route) {
                    SplashScreen { destination ->
                        navController.navigate(route = destination.route) {
                            popUpTo(route = MacroTrackDestination.SPLASH_SCREEN.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }
}
