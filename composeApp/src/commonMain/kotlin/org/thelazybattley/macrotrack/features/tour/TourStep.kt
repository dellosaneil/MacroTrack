package org.thelazybattley.macrotrack.features.tour

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.tour_confirm_body
import macrotrack.composeapp.generated.resources.tour_confirm_title
import macrotrack.composeapp.generated.resources.tour_done_body
import macrotrack.composeapp.generated.resources.tour_done_title
import macrotrack.composeapp.generated.resources.tour_goal_body
import macrotrack.composeapp.generated.resources.tour_goal_title
import macrotrack.composeapp.generated.resources.tour_goals_body
import macrotrack.composeapp.generated.resources.tour_goals_title
import macrotrack.composeapp.generated.resources.tour_log_meal_body
import macrotrack.composeapp.generated.resources.tour_log_meal_title
import macrotrack.composeapp.generated.resources.tour_log_weight_body
import macrotrack.composeapp.generated.resources.tour_log_weight_title
import macrotrack.composeapp.generated.resources.tour_navigation_body
import macrotrack.composeapp.generated.resources.tour_navigation_title
import macrotrack.composeapp.generated.resources.tour_pick_body
import macrotrack.composeapp.generated.resources.tour_pick_title
import macrotrack.composeapp.generated.resources.tour_remaining_body
import macrotrack.composeapp.generated.resources.tour_remaining_title
import macrotrack.composeapp.generated.resources.tour_screen_add_meal
import macrotrack.composeapp.generated.resources.tour_screen_all_set
import macrotrack.composeapp.generated.resources.tour_screen_moving_average
import macrotrack.composeapp.generated.resources.tour_screen_profile
import macrotrack.composeapp.generated.resources.tour_screen_progress
import macrotrack.composeapp.generated.resources.tour_screen_today
import macrotrack.composeapp.generated.resources.tour_screen_welcome
import macrotrack.composeapp.generated.resources.tour_search_body
import macrotrack.composeapp.generated.resources.tour_search_title
import macrotrack.composeapp.generated.resources.tour_trend_body
import macrotrack.composeapp.generated.resources.tour_trend_title
import macrotrack.composeapp.generated.resources.tour_welcome_body
import macrotrack.composeapp.generated.resources.tour_welcome_title
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** The screen a tour step needs on display. Tabs of the main screen carry the tab to select. */
enum class TourScreen(val tab: AppDestinations.BottomNavigation?) {
    LOG(tab = AppDestinations.BottomNavigation.Log),
    ADD_MEAL(tab = null),
    PROGRESS(tab = AppDestinations.BottomNavigation.Progress),
    MOVING_AVERAGE(tab = null),
    PROFILE(tab = AppDestinations.BottomNavigation.Profile)
}

/** A control the tour can spotlight; mark it with [tourTarget]. */
enum class TourTarget(val cornerRadius: Dp) {
    TODAY_HEADER(cornerRadius = 16.dp),
    TODAY_REMAINING(cornerRadius = 26.dp),
    TODAY_ADD_MEAL(cornerRadius = 16.dp),
    BOTTOM_NAVIGATION(cornerRadius = 0.dp),
    ADD_MEAL_SEARCH(cornerRadius = 20.dp),
    ADD_MEAL_FIRST_ITEM(cornerRadius = 20.dp),
    ADD_MEAL_CONFIRM(cornerRadius = 18.dp),
    PROGRESS_QUICK_ACTIONS(cornerRadius = 20.dp),
    MOVING_AVERAGE_CHART(cornerRadius = 28.dp),
    PROFILE_NUTRITION_GOALS(cornerRadius = 12.dp)
}

/**
 * The first-run tour, in order. A step without a [target] shows its card in the middle of the
 * screen; so does a step whose target is not on screen (an empty Recent list, no weigh-ins yet).
 */
enum class TourStep(
    val screen: TourScreen,
    val target: TourTarget?,
    val screenLabel: StringResource,
    val title: StringResource,
    val body: StringResource
) {
    WELCOME(
        screen = TourScreen.LOG,
        target = null,
        screenLabel = Res.string.tour_screen_welcome,
        title = Res.string.tour_welcome_title,
        body = Res.string.tour_welcome_body
    ),
    TODAY_GOAL(
        screen = TourScreen.LOG,
        target = TourTarget.TODAY_HEADER,
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_goal_title,
        body = Res.string.tour_goal_body
    ),
    TODAY_REMAINING(
        screen = TourScreen.LOG,
        target = TourTarget.TODAY_REMAINING,
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_remaining_title,
        body = Res.string.tour_remaining_body
    ),
    TODAY_LOG_MEAL(
        screen = TourScreen.LOG,
        target = TourTarget.TODAY_ADD_MEAL,
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_log_meal_title,
        body = Res.string.tour_log_meal_body
    ),
    NAVIGATION(
        screen = TourScreen.LOG,
        target = TourTarget.BOTTOM_NAVIGATION,
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_navigation_title,
        body = Res.string.tour_navigation_body
    ),
    ADD_MEAL_SEARCH(
        screen = TourScreen.ADD_MEAL,
        target = TourTarget.ADD_MEAL_SEARCH,
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_search_title,
        body = Res.string.tour_search_body
    ),
    ADD_MEAL_PICK(
        screen = TourScreen.ADD_MEAL,
        target = TourTarget.ADD_MEAL_FIRST_ITEM,
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_pick_title,
        body = Res.string.tour_pick_body
    ),
    ADD_MEAL_CONFIRM(
        screen = TourScreen.ADD_MEAL,
        target = TourTarget.ADD_MEAL_CONFIRM,
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_confirm_title,
        body = Res.string.tour_confirm_body
    ),
    PROGRESS_LOG_WEIGHT(
        screen = TourScreen.PROGRESS,
        target = TourTarget.PROGRESS_QUICK_ACTIONS,
        screenLabel = Res.string.tour_screen_progress,
        title = Res.string.tour_log_weight_title,
        body = Res.string.tour_log_weight_body
    ),
    MOVING_AVERAGE(
        screen = TourScreen.MOVING_AVERAGE,
        target = TourTarget.MOVING_AVERAGE_CHART,
        screenLabel = Res.string.tour_screen_moving_average,
        title = Res.string.tour_trend_title,
        body = Res.string.tour_trend_body
    ),
    PROFILE_GOALS(
        screen = TourScreen.PROFILE,
        target = TourTarget.PROFILE_NUTRITION_GOALS,
        screenLabel = Res.string.tour_screen_profile,
        title = Res.string.tour_goals_title,
        body = Res.string.tour_goals_body
    ),
    ALL_SET(
        screen = TourScreen.LOG,
        target = null,
        screenLabel = Res.string.tour_screen_all_set,
        title = Res.string.tour_done_title,
        body = Res.string.tour_done_body
    );

    val isFirst: Boolean get() = this == entries.first()

    val isLast: Boolean get() = this == entries.last()
}
