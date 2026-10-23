package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.thelazybattley.macrotrack.core.telemetry.TrackScreenViews
import org.thelazybattley.macrotrack.features.activity.ui.RecordingBar
import org.thelazybattley.macrotrack.features.foodlog.ui.FoodLogTabScreen
import org.thelazybattley.macrotrack.features.insights.ui.InsightsScreen
import org.thelazybattley.macrotrack.features.profile.ui.ProfileScreen
import org.thelazybattley.macrotrack.features.progress.ui.ProgressScreen
import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.tourTarget
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

/** Asks the main screen to switch tabs. Each request is a new instance, so asking for the same tab twice still works. */
class TabRequest(val tab: AppDestinations.BottomNavigation)

@Composable
fun MainScreenNavigation(
    modifier: Modifier = Modifier,
    tabRequest: TabRequest? = null,
    onTabRequestHandled: () -> Unit = {},
    onReplayTour: () -> Unit = {},
    onNavigate: (String) -> Unit
) {
    val navController = rememberNavController()
    var currentDestination: AppDestinations.BottomNavigation by remember { mutableStateOf(value = AppDestinations.BottomNavigation.Log) }
    LaunchedEffect(key1 = Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = AppDestinations.BottomNavigation.entries.find { macroDestination ->
                destination.route == macroDestination.route
            } ?: AppDestinations.BottomNavigation.Log
        }
    }
    TrackScreenViews(navController = navController)
    // Clear the request once handled; otherwise it runs again whenever this screen comes back (say, after Back from
    // Nutrition goals) and jumps to that tab.
    LaunchedEffect(key1 = tabRequest) {
        val tab = tabRequest?.tab ?: return@LaunchedEffect
        // The tab NavHost sits in the Scaffold's content, which is composed after this effect can start. Navigating
        // before it sets its graph throws, so wait for its first screen.
        val currentRoute = navController.currentBackStackEntryFlow.first().destination.route
        if (currentRoute != tab.route) {
            navController.navigate(route = tab.route) {
                popUpTo(route = AppDestinations.BottomNavigation.Log.route)
                launchSingleTop = true
            }
        }
        // Last, since clearing the request cancels this effect.
        onTabRequestHandled()
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        bottomBar = {
            Column {
                // Shown on every tab while a walk or run records, just above the navigation.
                RecordingBar()
                MacroBottomNavBar(
                    modifier = Modifier.tourTarget(target = TourTarget.BOTTOM_NAVIGATION),
                    selectedDestination = currentDestination,
                ) { destination ->
                    if (currentDestination.route == destination.route) return@MacroBottomNavBar
                    navController.navigate(route = destination.route)
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .padding(paddingValues = AppPadding),
            navController = navController,
            startDestination = AppDestinations.BottomNavigation.Log.route
        ) {
            composable(route = AppDestinations.BottomNavigation.Log.route) {
                FoodLogTabScreen { route ->
                    onNavigate(route)
                }
            }
            composable(route = AppDestinations.BottomNavigation.Insights.route) {
                InsightsScreen(modifier = Modifier.fillMaxSize())
            }
            composable(route = AppDestinations.BottomNavigation.Progress.route) {
                ProgressScreen(modifier = Modifier.fillMaxSize()) { route ->
                    onNavigate(route)
                }
            }
            composable(route = AppDestinations.BottomNavigation.Profile.route) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigate = { route ->
                        onNavigate(route)
                    },
                    onReplayTour = onReplayTour
                )
            }
        }
    }
}
