package org.thelazybattley.macrotrack.ui.navigation

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_nav_insights
import macrotrack.composeapp.generated.resources.ic_nav_log
import macrotrack.composeapp.generated.resources.ic_nav_profile
import macrotrack.composeapp.generated.resources.ic_nav_progress
import macrotrack.composeapp.generated.resources.insights
import macrotrack.composeapp.generated.resources.log
import macrotrack.composeapp.generated.resources.profile
import macrotrack.composeapp.generated.resources.progress
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

sealed class AppDestinations {

    sealed class BottomNavigation(
        val route: String,
        val icon: DrawableResource,
        val label: StringResource
    ) : AppDestinations() {
        object Log : BottomNavigation(
            route = "log", label = Res.string.log,
            icon = Res.drawable.ic_nav_log
        )

        object Insights : BottomNavigation(
            route = "insights", label = Res.string.insights,
            icon = Res.drawable.ic_nav_insights
        )

        object Progress : BottomNavigation(
            route = "progress", label = Res.string.progress,
            icon = Res.drawable.ic_nav_progress
        )

        object Profile : BottomNavigation(
            route = "profile", label = Res.string.profile,
            icon = Res.drawable.ic_nav_profile
        )

        companion object {
            val entries: List<BottomNavigation>
                get() = listOf(
                    Log, Insights, Progress, Profile
                )
        }
    }

    sealed class Root(val route: String, val routeWithArgs: String? = null) : AppDestinations() {
        object Splash : Root(route = "splash")
        object Walkthrough : Root(route = "walkthrough")
        object Onboarding : Root(route = "onboarding")
        object AddMeal : Root(route = "add_meal", routeWithArgs = "add_meal/{$MEAL_TYPE}") {
            fun createRoute(mealType: String): String {
                return "$route/$mealType"
            }
        }

        object CreateFood : Root(route = "create_food", routeWithArgs = "create_food/{$FOOD_NAME}?$BARCODE={$BARCODE}") {
            /** [barcode] links a new food to a scanned barcode that wasn't found. */
            fun createRoute(foodName: String, barcode: String? = null): String {
                return if (barcode == null) "$route/$foodName" else "$route/$foodName?$BARCODE=$barcode"
            }
        }

        object ScanBarcode : Root(route = "scan_barcode", routeWithArgs = "scan_barcode/{$MEAL_TYPE}") {
            fun createRoute(mealType: String): String {
                return "$route/$mealType"
            }
        }
        object CreateRecipe :
            Root(route = "create_recipe", routeWithArgs = "create_recipe/{$RECIPE_NAME}") {
            fun createRoute(recipeName: String): String {
                return "$route/$recipeName"
            }
        }

        object MainScreen : Root(route = "main")

        object MeasurementsLog : Root(route = "measurements_log")

        object LogWeight : Root(route = "log_weight")

        object Calendar : Root(route = "calendar")

        object PastGoal : Root(route = "past_goal")

        object MovingAverage : Root(route = "moving_average")

        object Paywall : Root(route = "paywall", routeWithArgs = "paywall?$PRO_FEATURE={$PRO_FEATURE}") {
            fun createRoute(feature: String?): String {
                return if (feature == null) route else "$route?$PRO_FEATURE=$feature"
            }
        }

        object MeasurementDetail :
            Root(route = "measurement_detail", routeWithArgs = "measurement_detail/{$MEASUREMENT_TYPE}") {
            fun createRoute(type: String): String {
                return "$route/$type"
            }
        }
    }

    sealed class Profile(val route: String) : AppDestinations() {
        object PersonalInfo : Profile(route = ProfileSectionEnum.PERSONAL_INFO.name)
        object NutritionGoals : Profile(route = ProfileSectionEnum.NUTRITION_GOALS.name)
        object WeightHistory : Profile(route = ProfileSectionEnum.WEIGHT_HISTORY.name)
        object BodyMeasurements : Profile(route = ProfileSectionEnum.BODY_MEASUREMENTS.name)
    }

    companion object {
        const val MEAL_TYPE = "mealType"
        const val RECIPE_NAME = "recipeName"
        const val FOOD_NAME = "foodName"
        const val BARCODE = "barcode"
        const val MEASUREMENT_TYPE = "measurementType"
        const val PRO_FEATURE = "proFeature"
    }
}
