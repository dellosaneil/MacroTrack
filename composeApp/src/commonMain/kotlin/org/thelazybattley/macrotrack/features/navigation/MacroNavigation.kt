package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.thelazybattley.macrotrack.features.home.ui.HomeTabScreen
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingScreen
import org.thelazybattley.macrotrack.features.splash.ui.SplashScreen
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
@Preview(showBackground = true)
fun AppNavigation() {
    val navController = rememberNavController()
    var currentDestination: MacroTrackDestination by remember { mutableStateOf(value = MacroTrackDestination.HOME) }
    LaunchedEffect(key1 = navController.currentDestination) {
        val destination =
            MacroTrackDestination.entries.find { destination -> destination.route == navController.currentDestination?.route }
                ?: return@LaunchedEffect
        currentDestination = destination
    }
    MacroTrackTheme {
        Scaffold(
            containerColor = colors.white,
            bottomBar = {
                MacroBottomNavBar(
                    modifier = Modifier,
                    onDestinationClicked = { destination ->
                        navController.navigate(route = destination.route)
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues = paddingValues),
                navController = navController,
                startDestination = MacroTrackDestination.SPLASH_SCREEN.route
            ) {
                composable(route = MacroTrackDestination.ONBOARDING.route) {
                    OnboardingScreen(
                        modifier = Modifier.padding(paddingValues = AppPadding)
                    ) {
                        navController.navigate(route = MacroTrackDestination.HOME.route)
                    }
                }
                composable(route = MacroTrackDestination.HOME.route) {
                    HomeTabScreen(
                        modifier = Modifier.padding(paddingValues = AppPadding)
                    )
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

                composable(route = MacroTrackDestination.SPLASH_SCREEN.route) {
                    SplashScreen { destination ->
                        navController.navigate(route = destination.route) {
                            popUpTo(route = MacroTrackDestination.SPLASH_SCREEN.route) {
                                inclusive = true
                            }
                        }
                    }
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

val AppPadding = PaddingValues(all = 16.dp)
