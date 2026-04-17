package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealScreen
import org.thelazybattley.macrotrack.features.createfood.ui.AddIngredientScreen
import org.thelazybattley.macrotrack.features.createrecipe.ui.CreateRecipeScreen
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingScreen
import org.thelazybattley.macrotrack.features.profile.personalinformation.ui.PersonalInformationScreen
import org.thelazybattley.macrotrack.features.splash.ui.SplashScreen
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.RECIPE_NAME
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
@Preview(showBackground = true)
fun AppBottomNav() {
    val navController = rememberNavController()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    MacroTrackTheme {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Root.Splash.route,
            modifier = Modifier.pointerInput(key1 = Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
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
                    modifier = Modifier.fillMaxSize()
                ) { destination ->
                    navController.navigate(route = destination)
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
                route = AppDestinations.Root.AddMeal.routeWithArgs!!,
                arguments = listOf(
                    navArgument(name = MEAL_TYPE) {
                        type = NavType.StringType
                    }
                )
            ) {
                AddMealScreen(
                    modifier = Modifier,
                    onBackButtonPressed = {
                        navController.popBackStack()
                    }
                ) { route ->
                    navController.navigate(route = route)
                }
            }
            composable(route = AppDestinations.Root.CreateFood.route) {
                AddIngredientScreen(
                    modifier = Modifier
                ) {
                    navController.popBackStack()
                }
            }
            composable(
                route = AppDestinations.Root.CreateRecipe.routeWithArgs!!,
                arguments = listOf(
                    navArgument(name = RECIPE_NAME) {
                        type = NavType.StringType
                    }
                )
            ) {
                CreateRecipeScreen(
                    modifier = Modifier
                ) {
                    navController.popBackStack()
                }
            }

            composable(
                route = AppDestinations.Profile.PersonalInfo.route
            ) {
                PersonalInformationScreen(
                    modifier = Modifier,
                    onPopBackStack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = AppDestinations.Profile.NutritionGoals.route
            ) {
                Text("nutritionGoals")
            }
            composable(
                route = AppDestinations.Profile.WeightHistory.route
            ) {
                Text("weight")
            }

        }
    }
}

val AppPadding = PaddingValues(
    horizontal = 16.dp,
    vertical = 8.dp
)
