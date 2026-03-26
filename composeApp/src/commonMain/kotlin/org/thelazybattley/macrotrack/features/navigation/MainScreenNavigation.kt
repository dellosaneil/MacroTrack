package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.thelazybattley.macrotrack.features.home.ui.HomeTabScreen
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackMainDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun MainScreenNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var currentDestination by remember { mutableStateOf(MacroTrackMainDestination.HOME) }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentDestination = MacroTrackMainDestination.entries.find { macroDestination ->
            destination.route == macroDestination.route
        } ?: MacroTrackMainDestination.HOME
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.white,
        bottomBar = {
            MacroBottomNavBar(
                selectedDestination = currentDestination,
            ) { destination ->
                navController.navigate(route = destination.route)
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .padding(paddingValues = AppPadding),
            navController = navController,
            startDestination = MacroTrackMainDestination.HOME.route
        ) {
            composable(route = MacroTrackMainDestination.HOME.route) {
                HomeTabScreen()
            }
            composable(route = MacroTrackMainDestination.LOG.route) {
                Text(
                    text = "Log"
                )
            }
            composable(route = MacroTrackMainDestination.STEPS.route) {
                Text("steps")
            }
            composable(route = MacroTrackMainDestination.PROGRESS.route) {
                Text("progress")
            }
            composable(route = MacroTrackMainDestination.PROFILE.route) {
                Text("profile")
            }
        }
    }

}


@Preview(
    showBackground = true,
    backgroundColor = 0xffffffff
)
@Composable
private fun MainScreenNavigation() {
    MainScreenNavigation(modifier = Modifier)
}
