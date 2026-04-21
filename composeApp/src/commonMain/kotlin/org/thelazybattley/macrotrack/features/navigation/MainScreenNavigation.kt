package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.thelazybattley.macrotrack.features.foodlog.ui.FoodLogTabScreen
import org.thelazybattley.macrotrack.features.profile.ui.ProfileScreen
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun MainScreenNavigation(
    modifier: Modifier = Modifier,
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

    Scaffold(
        modifier = modifier,
        containerColor = colors.white,
        bottomBar = {
            MacroBottomNavBar(
                selectedDestination = currentDestination,
            ) { destination ->
                if (currentDestination.route == destination.route) return@MacroBottomNavBar
                navController.navigate(route = destination.route)
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
            composable(route = AppDestinations.BottomNavigation.Steps.route) {
                Text("steps")
            }
            composable(route = AppDestinations.BottomNavigation.Progress.route) {
                Text("progress")
            }
            composable(route = AppDestinations.BottomNavigation.Profile.route) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigate = { route ->
                        onNavigate(route)
                    }
                )
            }
        }
    }
}
