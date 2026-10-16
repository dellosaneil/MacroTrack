package org.thelazybattley.macrotrack.core.telemetry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import org.koin.compose.koinInject

/** Logs a screen view each time [navController] lands on a new destination, skipping the routes in [ignore]. */
@Composable
fun TrackScreenViews(navController: NavHostController, ignore: Set<String> = emptySet()) {
    val telemetry = koinInject<Telemetry>()
    LaunchedEffect(key1 = navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            val route = entry.destination.route ?: return@collect
            if (route in ignore) return@collect
            telemetry.logScreen(name = screenName(route = route))
        }
    }
}

/** "add_meal/{mealType}" becomes "add_meal", so screens group by where they are and never carry argument values. */
internal fun screenName(route: String): String = route.substringBefore(delimiter = '/').substringBefore(delimiter = '?').lowercase()
