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

enum class MacroTrackDestination(
    val route: String
) {
    SPLASH_SCREEN(route = "splash_screen"),
    ONBOARDING(route = "onboarding"),
    MAIN(route = "main"),
    ADD_MEAL(route = "add_meal")
}

enum class MacroTrackMainDestination(
    val route: String,
    val icon: DrawableResource,
    val label: StringResource
) {
    HOME(
        route = "home", label = Res.string.home,
        icon = Res.drawable.ic_home
    ),
    LOG(
        route = "log", label = Res.string.log,
        icon = Res.drawable.ic_log
    ),
    STEPS(
        route = "steps",
        label = Res.string.steps,
        icon = Res.drawable.ic_steps
    ),
    PROGRESS(
        route = "progress", label = Res.string.progress,
        icon = Res.drawable.ic_progress
    ),
    PROFILE(
        route = "profile", label = Res.string.profile,
        icon = Res.drawable.ic_profile
    ),
}
