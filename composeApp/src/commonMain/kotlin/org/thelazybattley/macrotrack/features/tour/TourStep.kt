package org.thelazybattley.macrotrack.features.tour

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.tour_confirm_body
import macrotrack.composeapp.generated.resources.tour_confirm_title
import macrotrack.composeapp.generated.resources.tour_create_body
import macrotrack.composeapp.generated.resources.tour_create_title
import macrotrack.composeapp.generated.resources.tour_done_body
import macrotrack.composeapp.generated.resources.tour_done_title
import macrotrack.composeapp.generated.resources.tour_filters_body
import macrotrack.composeapp.generated.resources.tour_filters_title
import macrotrack.composeapp.generated.resources.tour_goal_body
import macrotrack.composeapp.generated.resources.tour_goal_title
import macrotrack.composeapp.generated.resources.tour_goals_body
import macrotrack.composeapp.generated.resources.tour_goals_title
import macrotrack.composeapp.generated.resources.tour_insights_body
import macrotrack.composeapp.generated.resources.tour_insights_title
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
import macrotrack.composeapp.generated.resources.tour_screen_insights
import macrotrack.composeapp.generated.resources.tour_screen_moving_average
import macrotrack.composeapp.generated.resources.tour_screen_profile
import macrotrack.composeapp.generated.resources.tour_screen_progress
import macrotrack.composeapp.generated.resources.tour_screen_today
import macrotrack.composeapp.generated.resources.tour_screen_welcome
import macrotrack.composeapp.generated.resources.tour_search_body
import macrotrack.composeapp.generated.resources.tour_search_title
import macrotrack.composeapp.generated.resources.tour_streak_body
import macrotrack.composeapp.generated.resources.tour_streak_title
import macrotrack.composeapp.generated.resources.tour_trend_body
import macrotrack.composeapp.generated.resources.tour_trend_title
import macrotrack.composeapp.generated.resources.tour_welcome_body
import macrotrack.composeapp.generated.resources.tour_welcome_title
import macrotrack.composeapp.generated.resources.tour_your_data_body
import macrotrack.composeapp.generated.resources.tour_your_data_title
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** The screen a tour step needs on display. Tabs of the main screen carry the tab to select. */
enum class TourScreen(val tab: AppDestinations.BottomNavigation?) {
    LOG(tab = AppDestinations.BottomNavigation.Log),
    ADD_MEAL(tab = null),
    INSIGHTS(tab = AppDestinations.BottomNavigation.Insights),
    PROGRESS(tab = AppDestinations.BottomNavigation.Progress),
    MOVING_AVERAGE(tab = null),
    PROFILE(tab = AppDestinations.BottomNavigation.Profile)
}

/** A control the tour can spotlight; mark it with [tourTarget]. */
enum class TourTarget(val cornerRadius: Dp) {
    TODAY_HEADER(cornerRadius = 16.dp),
    TODAY_STREAK(cornerRadius = 20.dp),
    TODAY_REMAINING(cornerRadius = 26.dp),
    TODAY_ADD_MEAL(cornerRadius = 16.dp),
    BOTTOM_NAVIGATION(cornerRadius = 0.dp),
    ADD_MEAL_SEARCH(cornerRadius = 20.dp),
    ADD_MEAL_CREATE(cornerRadius = 20.dp),
    ADD_MEAL_FILTERS(cornerRadius = 20.dp),
    ADD_MEAL_FIRST_ITEM(cornerRadius = 20.dp),
    /** What the list shows when it has nothing to pick yet. */
    ADD_MEAL_EMPTY_LIST(cornerRadius = 26.dp),
    ADD_MEAL_CONFIRM(cornerRadius = 18.dp),
    INSIGHTS_SUMMARY(cornerRadius = 26.dp),
    INSIGHTS_RANGE(cornerRadius = 20.dp),
    PROGRESS_QUICK_ACTIONS(cornerRadius = 20.dp),
    MOVING_AVERAGE_CHART(cornerRadius = 28.dp),
    MOVING_AVERAGE_RANGE(cornerRadius = 20.dp),
    PROFILE_NUTRITION_GOALS(cornerRadius = 12.dp),
    PROFILE_YOUR_DATA(cornerRadius = 26.dp)
}

/**
 * The first-run tour, in order. A step spotlights the first of its [targets] that is on the screen, so an empty list
 * or chart falls back to the next one; with none (or no [targets]) the card shows in the middle of the screen.
 */
enum class TourStep(
    val screen: TourScreen,
    val targets: List<TourTarget>,
    val screenLabel: StringResource,
    val title: StringResource,
    val body: StringResource
) {
    WELCOME(
        screen = TourScreen.LOG,
        targets = emptyList(),
        screenLabel = Res.string.tour_screen_welcome,
        title = Res.string.tour_welcome_title,
        body = Res.string.tour_welcome_body
    ),
    TODAY_GOAL(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_HEADER),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_goal_title,
        body = Res.string.tour_goal_body
    ),
    TODAY_STREAK(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_STREAK),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_streak_title,
        body = Res.string.tour_streak_body
    ),
    TODAY_REMAINING(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_REMAINING),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_remaining_title,
        body = Res.string.tour_remaining_body
    ),
    TODAY_LOG_MEAL(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_ADD_MEAL),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_log_meal_title,
        body = Res.string.tour_log_meal_body
    ),
    NAVIGATION(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.BOTTOM_NAVIGATION),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_navigation_title,
        body = Res.string.tour_navigation_body
    ),
    ADD_MEAL_SEARCH(
        screen = TourScreen.ADD_MEAL,
        targets = listOf(TourTarget.ADD_MEAL_SEARCH),
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_search_title,
        body = Res.string.tour_search_body
    ),
    ADD_MEAL_CREATE(
        screen = TourScreen.ADD_MEAL,
        targets = listOf(TourTarget.ADD_MEAL_CREATE),
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_create_title,
        body = Res.string.tour_create_body
    ),
    ADD_MEAL_FILTERS(
        screen = TourScreen.ADD_MEAL,
        targets = listOf(TourTarget.ADD_MEAL_FILTERS),
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_filters_title,
        body = Res.string.tour_filters_body
    ),
    ADD_MEAL_PICK(
        screen = TourScreen.ADD_MEAL,
        targets = listOf(TourTarget.ADD_MEAL_FIRST_ITEM, TourTarget.ADD_MEAL_EMPTY_LIST),
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_pick_title,
        body = Res.string.tour_pick_body
    ),
    ADD_MEAL_CONFIRM(
        screen = TourScreen.ADD_MEAL,
        targets = listOf(TourTarget.ADD_MEAL_CONFIRM),
        screenLabel = Res.string.tour_screen_add_meal,
        title = Res.string.tour_confirm_title,
        body = Res.string.tour_confirm_body
    ),
    INSIGHTS(
        screen = TourScreen.INSIGHTS,
        targets = listOf(TourTarget.INSIGHTS_SUMMARY, TourTarget.INSIGHTS_RANGE),
        screenLabel = Res.string.tour_screen_insights,
        title = Res.string.tour_insights_title,
        body = Res.string.tour_insights_body
    ),
    PROGRESS_LOG_WEIGHT(
        screen = TourScreen.PROGRESS,
        targets = listOf(TourTarget.PROGRESS_QUICK_ACTIONS),
        screenLabel = Res.string.tour_screen_progress,
        title = Res.string.tour_log_weight_title,
        body = Res.string.tour_log_weight_body
    ),
    MOVING_AVERAGE(
        screen = TourScreen.MOVING_AVERAGE,
        targets = listOf(TourTarget.MOVING_AVERAGE_CHART, TourTarget.MOVING_AVERAGE_RANGE),
        screenLabel = Res.string.tour_screen_moving_average,
        title = Res.string.tour_trend_title,
        body = Res.string.tour_trend_body
    ),
    PROFILE_GOALS(
        screen = TourScreen.PROFILE,
        targets = listOf(TourTarget.PROFILE_NUTRITION_GOALS),
        screenLabel = Res.string.tour_screen_profile,
        title = Res.string.tour_goals_title,
        body = Res.string.tour_goals_body
    ),
    PROFILE_YOUR_DATA(
        screen = TourScreen.PROFILE,
        targets = listOf(TourTarget.PROFILE_YOUR_DATA),
        screenLabel = Res.string.tour_screen_profile,
        title = Res.string.tour_your_data_title,
        body = Res.string.tour_your_data_body
    ),
    ALL_SET(
        screen = TourScreen.LOG,
        targets = emptyList(),
        screenLabel = Res.string.tour_screen_all_set,
        title = Res.string.tour_done_title,
        body = Res.string.tour_done_body
    );

    val isFirst: Boolean get() = this == entries.first()

    val isLast: Boolean get() = this == entries.last()
}
