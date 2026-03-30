package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.thelazybattley.macrotrack.features.addfood.ui.AddIngredientScreen
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealScreen
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingScreen
import org.thelazybattley.macrotrack.features.splash.ui.SplashScreen
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
@Preview(showBackground = true)
fun SplashScreenNavigationNavigation() {
    val navController = rememberNavController()
    MacroTrackTheme {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Root.Splash.route
        ) {
            composable(route = AppDestinations.Root.Onboarding.route) {
                OnboardingScreen(
                    modifier = Modifier
                ) {
                    navController.navigate(route = AppDestinations.Root.MainScreen.route)
                }
            }
            composable(route = AppDestinations.Root.MainScreen.route) {
                MainScreenNavigation(
                    modifier = Modifier
                ) { destination ->
                    navController.navigate(route = destination.route)
                }
            }
            composable(route = AppDestinations.Root.Splash.route) {
                SplashScreen { destination ->
                    navController.navigate(
                        route = destination.route
                    ) {
                        popUpTo(route = AppDestinations.Root.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            }
            composable(
                route = AppDestinations.Root.AddMeal.route,
                arguments = listOf(
                    navArgument(name = "mealType") {
                        type = NavType.StringType
                    }
                )
            ) {
                AddMealScreen(
                    modifier = Modifier,
                    onBackButtonPressed = {
                        navController.popBackStack()
                    }
                ) { destination ->
                    navController.navigate(route = destination.route)
                }
            }
            composable(route = AppDestinations.Root.AddFood.route) {
                AddIngredientScreen(
                    modifier = Modifier
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}

val AppPadding = PaddingValues(
    horizontal = 16.dp,
    vertical = 8.dp
)
