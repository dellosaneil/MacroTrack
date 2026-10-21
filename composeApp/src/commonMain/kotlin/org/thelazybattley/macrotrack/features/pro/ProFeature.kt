package org.thelazybattley.macrotrack.features.pro

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.pro_feature_backup
import macrotrack.composeapp.generated.resources.pro_feature_backup_body
import macrotrack.composeapp.generated.resources.pro_feature_check_in
import macrotrack.composeapp.generated.resources.pro_feature_check_in_body
import macrotrack.composeapp.generated.resources.pro_feature_goal_phases
import macrotrack.composeapp.generated.resources.pro_feature_goal_phases_body
import macrotrack.composeapp.generated.resources.pro_feature_insights
import macrotrack.composeapp.generated.resources.pro_feature_insights_body
import macrotrack.composeapp.generated.resources.pro_feature_measurements
import macrotrack.composeapp.generated.resources.pro_feature_measurements_body
import macrotrack.composeapp.generated.resources.pro_feature_moving_average
import macrotrack.composeapp.generated.resources.pro_feature_moving_average_body
import macrotrack.composeapp.generated.resources.pro_feature_range_comparison
import macrotrack.composeapp.generated.resources.pro_feature_range_comparison_body
import macrotrack.composeapp.generated.resources.pro_feature_recipes
import macrotrack.composeapp.generated.resources.pro_feature_recipes_body
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.domain.model.FREE_RECIPE_LIMIT
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** What Pro adds, in the order the paywall lists it. */
enum class ProFeature(val title: StringResource, val body: StringResource) {
    MOVING_AVERAGE(title = Res.string.pro_feature_moving_average, body = Res.string.pro_feature_moving_average_body),
    GOAL_PHASES(title = Res.string.pro_feature_goal_phases, body = Res.string.pro_feature_goal_phases_body),
    RANGE_COMPARISON(title = Res.string.pro_feature_range_comparison, body = Res.string.pro_feature_range_comparison_body),
    BODY_MEASUREMENTS(title = Res.string.pro_feature_measurements, body = Res.string.pro_feature_measurements_body),
    INSIGHTS(title = Res.string.pro_feature_insights, body = Res.string.pro_feature_insights_body),
    RECIPES(title = Res.string.pro_feature_recipes, body = Res.string.pro_feature_recipes_body),
    BACKUP(title = Res.string.pro_feature_backup, body = Res.string.pro_feature_backup_body),
    CHECK_IN(title = Res.string.pro_feature_check_in, body = Res.string.pro_feature_check_in_body)
}

/**
 * The Pro feature a navigation [route] opens, or null when it is free. Logging new measurements is Pro, while
 * the ones already saved stay viewable; a new recipe is Pro only past [FREE_RECIPE_LIMIT], and editing a saved
 * one never is.
 */
fun proFeatureFor(route: String, recipeCount: Int): ProFeature? = when (route) {
    AppDestinations.Root.MovingAverage.route -> ProFeature.MOVING_AVERAGE
    AppDestinations.Root.MeasurementsLog.route -> ProFeature.BODY_MEASUREMENTS
    AppDestinations.Root.CreateRecipe.createRoute(recipeName = "") ->
        ProFeature.RECIPES.takeIf { recipeCount >= FREE_RECIPE_LIMIT }
    else -> null
}
