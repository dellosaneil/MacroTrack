package org.thelazybattley.macrotrack.ui.navigation

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.home
import macrotrack.composeapp.generated.resources.ic_home
import macrotrack.composeapp.generated.resources.ic_log
import macrotrack.composeapp.generated.resources.ic_profile
import macrotrack.composeapp.generated.resources.ic_progress
import macrotrack.composeapp.generated.resources.ic_steps
import macrotrack.composeapp.generated.resources.log
import macrotrack.composeapp.generated.resources.profile
import macrotrack.composeapp.generated.resources.progress
import macrotrack.composeapp.generated.resources.steps
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

sealed class AppDestinations {

    sealed class BottomNavigation(
        val route: String,
        val icon: DrawableResource,
        val label: StringResource
    ) : AppDestinations() {
        object Home : BottomNavigation(
            route = "home", label = Res.string.home,
            icon = Res.drawable.ic_home
        )

        object Log : BottomNavigation(
            route = "log", label = Res.string.log,
            icon = Res.drawable.ic_log
        )

        object Steps : BottomNavigation(
            route = "steps",
            label = Res.string.steps,
            icon = Res.drawable.ic_steps
        )

        object Progress : BottomNavigation(
            route = "progress", label = Res.string.progress,
            icon = Res.drawable.ic_progress
        )

        object Profile : BottomNavigation(
            route = "profile", label = Res.string.profile,
            icon = Res.drawable.ic_profile
        )

        companion object {
            val entries: List<BottomNavigation>
                get() = listOf(
                    Home, Log, Steps, Progress, Profile
                )
        }
    }

    sealed class Root(val route: String, val routeWithArgs: String? = null) : AppDestinations() {
        object Splash : Root(route = "splash")
        object Onboarding : Root(route = "onboarding")
        object AddMeal : Root(route = "add_meal", routeWithArgs = "add_meal/{$MEAL_TYPE}") {
            fun createRoute(mealType: String): String {
                return "$route/$mealType"
            }
        }

        object CreateFood : Root(route = "create_food")
        object CreateRecipe :
            Root(route = "create_recipe", routeWithArgs = "create_recipe/{$RECIPE_NAME}") {
            fun createRoute(recipeName: String): String {
                return "$route/$recipeName"
            }
        }

        object MainScreen : Root(route = "main")
    }

    sealed class Profile(val route: String) : AppDestinations() {
        object PersonalInfo : Profile(route = ProfileSectionEnum.PERSONAL_INFO.name)
        object NutritionGoals : Profile(route = ProfileSectionEnum.NUTRITION_GOALS.name)
        object WeightHistory : Profile(route = ProfileSectionEnum.WEIGHT_HISTORY.name)
    }

    companion object {
        const val MEAL_TYPE = "mealType"
        const val RECIPE_NAME = "recipeName"
    }
}
