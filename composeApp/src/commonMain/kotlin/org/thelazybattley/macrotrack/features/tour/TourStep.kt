package org.thelazybattley.macrotrack.features.tour

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.tour_albums_body
import macrotrack.composeapp.generated.resources.tour_albums_title
import macrotrack.composeapp.generated.resources.tour_check_in_body
import macrotrack.composeapp.generated.resources.tour_check_in_title
import macrotrack.composeapp.generated.resources.tour_compare_body
import macrotrack.composeapp.generated.resources.tour_compare_title
import macrotrack.composeapp.generated.resources.tour_confirm_body
import macrotrack.composeapp.generated.resources.tour_confirm_title
import macrotrack.composeapp.generated.resources.tour_copy_body
import macrotrack.composeapp.generated.resources.tour_copy_title
import macrotrack.composeapp.generated.resources.tour_create_body
import macrotrack.composeapp.generated.resources.tour_create_title
import macrotrack.composeapp.generated.resources.tour_dates_body
import macrotrack.composeapp.generated.resources.tour_dates_title
import macrotrack.composeapp.generated.resources.tour_done_body
import macrotrack.composeapp.generated.resources.tour_done_title
import macrotrack.composeapp.generated.resources.tour_entry_body
import macrotrack.composeapp.generated.resources.tour_entry_title
import macrotrack.composeapp.generated.resources.tour_filters_body
import macrotrack.composeapp.generated.resources.tour_filters_title
import macrotrack.composeapp.generated.resources.tour_first_latest_body
import macrotrack.composeapp.generated.resources.tour_first_latest_title
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
import macrotrack.composeapp.generated.resources.tour_measurements_body
import macrotrack.composeapp.generated.resources.tour_measurements_title
import macrotrack.composeapp.generated.resources.tour_navigation_body
import macrotrack.composeapp.generated.resources.tour_navigation_title
import macrotrack.composeapp.generated.resources.tour_photos_body
import macrotrack.composeapp.generated.resources.tour_photos_title
import macrotrack.composeapp.generated.resources.tour_pick_body
import macrotrack.composeapp.generated.resources.tour_pick_title
import macrotrack.composeapp.generated.resources.tour_remaining_body
import macrotrack.composeapp.generated.resources.tour_remaining_title
import macrotrack.composeapp.generated.resources.tour_screen_add_meal
import macrotrack.composeapp.generated.resources.tour_screen_all_set
import macrotrack.composeapp.generated.resources.tour_screen_before_after
import macrotrack.composeapp.generated.resources.tour_screen_check_in
import macrotrack.composeapp.generated.resources.tour_screen_insights
import macrotrack.composeapp.generated.resources.tour_screen_measurements
import macrotrack.composeapp.generated.resources.tour_screen_moving_average
import macrotrack.composeapp.generated.resources.tour_screen_photos
import macrotrack.composeapp.generated.resources.tour_screen_profile
import macrotrack.composeapp.generated.resources.tour_screen_progress
import macrotrack.composeapp.generated.resources.tour_screen_today
import macrotrack.composeapp.generated.resources.tour_screen_weight_history
import macrotrack.composeapp.generated.resources.tour_screen_welcome
import macrotrack.composeapp.generated.resources.tour_search_body
import macrotrack.composeapp.generated.resources.tour_search_title
import macrotrack.composeapp.generated.resources.tour_steps_body
import macrotrack.composeapp.generated.resources.tour_steps_title
import macrotrack.composeapp.generated.resources.tour_streak_body
import macrotrack.composeapp.generated.resources.tour_streak_title
import macrotrack.composeapp.generated.resources.tour_trend_body
import macrotrack.composeapp.generated.resources.tour_trend_title
import macrotrack.composeapp.generated.resources.tour_week_body
import macrotrack.composeapp.generated.resources.tour_week_title
import macrotrack.composeapp.generated.resources.tour_weight_history_body
import macrotrack.composeapp.generated.resources.tour_weight_history_title
import macrotrack.composeapp.generated.resources.tour_welcome_body
import macrotrack.composeapp.generated.resources.tour_welcome_title
import macrotrack.composeapp.generated.resources.tour_your_data_body
import macrotrack.composeapp.generated.resources.tour_your_data_title
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.core.isActivityEnabled
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** The screen a tour step needs on display. Tabs of the main screen carry the tab to select. */
enum class TourScreen(val tab: AppDestinations.BottomNavigation?) {
    LOG(tab = AppDestinations.BottomNavigation.Log),
    ADD_MEAL(tab = null),
    INSIGHTS(tab = AppDestinations.BottomNavigation.Insights),
    PROGRESS(tab = AppDestinations.BottomNavigation.Progress),
    CHECK_IN(tab = null),
    WEIGHT_HISTORY(tab = null),
    MOVING_AVERAGE(tab = null),
    MEASUREMENTS(tab = null),
    PHOTOS(tab = null),
    /** Before & after in the album with the newest photos; the photos screen when none has two. */
    PHOTO_COMPARE(tab = null),
    PROFILE(tab = AppDestinations.BottomNavigation.Profile)
}

/** A control the tour can spotlight; mark it with [tourTarget]. */
enum class TourTarget(val cornerRadius: Dp) {
    TODAY_HEADER(cornerRadius = 16.dp),
    TODAY_STREAK(cornerRadius = 20.dp),
    TODAY_DATES(cornerRadius = 18.dp),
    TODAY_REMAINING(cornerRadius = 26.dp),
    TODAY_STEPS(cornerRadius = 18.dp),
    TODAY_COPY(cornerRadius = 10.dp),
    TODAY_ADD_MEAL(cornerRadius = 16.dp),
    /** The first food logged today. */
    TODAY_ENTRY(cornerRadius = 0.dp),
    TODAY_WEEK(cornerRadius = 18.dp),
    CHECK_IN_WEEK(cornerRadius = 24.dp),
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
    PROGRESS_PHOTOS(cornerRadius = 24.dp),
    WEIGHT_HISTORY_CHART(cornerRadius = 24.dp),
    WEIGHT_HISTORY_RANGE(cornerRadius = 14.dp),
    MOVING_AVERAGE_CHART(cornerRadius = 28.dp),
    MOVING_AVERAGE_RANGE(cornerRadius = 20.dp),
    MEASUREMENTS_SUMMARY(cornerRadius = 24.dp),
    PHOTOS_HERO(cornerRadius = 24.dp),
    PHOTOS_ALBUMS(cornerRadius = 20.dp),
    PHOTO_COMPARE(cornerRadius = 20.dp),
    PROFILE_NUTRITION_GOALS(cornerRadius = 12.dp),
    PROFILE_YOUR_DATA(cornerRadius = 26.dp)
}

/**
 * The first-run tour, in order; [shown] leaves out the debug-only steps on release builds. A step spotlights the
 * first of its [targets] that is on the screen, so an empty list or chart falls back to the next one; with none (or
 * no [targets]) the card shows in the middle of the screen.
 */
enum class TourStep(
    val screen: TourScreen,
    val targets: List<TourTarget>,
    val screenLabel: StringResource,
    val title: StringResource,
    val body: StringResource,
    val isDebugOnly: Boolean = false
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
    TODAY_DATES(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_DATES),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_dates_title,
        body = Res.string.tour_dates_body
    ),
    TODAY_REMAINING(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_REMAINING),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_remaining_title,
        body = Res.string.tour_remaining_body
    ),
    TODAY_STEPS(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_STEPS),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_steps_title,
        body = Res.string.tour_steps_body,
        isDebugOnly = true
    ),
    TODAY_COPY(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_COPY),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_copy_title,
        body = Res.string.tour_copy_body
    ),
    TODAY_LOG_MEAL(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_ADD_MEAL),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_log_meal_title,
        body = Res.string.tour_log_meal_body
    ),
    TODAY_ENTRY(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_ENTRY),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_entry_title,
        body = Res.string.tour_entry_body
    ),
    TODAY_WEEK(
        screen = TourScreen.LOG,
        targets = listOf(TourTarget.TODAY_WEEK),
        screenLabel = Res.string.tour_screen_today,
        title = Res.string.tour_week_title,
        body = Res.string.tour_week_body
    ),
    CHECK_IN(
        screen = TourScreen.CHECK_IN,
        targets = listOf(TourTarget.CHECK_IN_WEEK),
        screenLabel = Res.string.tour_screen_check_in,
        title = Res.string.tour_check_in_title,
        body = Res.string.tour_check_in_body
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
    WEIGHT_HISTORY(
        screen = TourScreen.WEIGHT_HISTORY,
        targets = listOf(TourTarget.WEIGHT_HISTORY_CHART, TourTarget.WEIGHT_HISTORY_RANGE),
        screenLabel = Res.string.tour_screen_weight_history,
        title = Res.string.tour_weight_history_title,
        body = Res.string.tour_weight_history_body
    ),
    MOVING_AVERAGE(
        screen = TourScreen.MOVING_AVERAGE,
        targets = listOf(TourTarget.MOVING_AVERAGE_CHART, TourTarget.MOVING_AVERAGE_RANGE),
        screenLabel = Res.string.tour_screen_moving_average,
        title = Res.string.tour_trend_title,
        body = Res.string.tour_trend_body
    ),
    MEASUREMENTS(
        screen = TourScreen.MEASUREMENTS,
        targets = listOf(TourTarget.MEASUREMENTS_SUMMARY),
        screenLabel = Res.string.tour_screen_measurements,
        title = Res.string.tour_measurements_title,
        body = Res.string.tour_measurements_body
    ),
    PROGRESS_PHOTOS(
        screen = TourScreen.PROGRESS,
        targets = listOf(TourTarget.PROGRESS_PHOTOS),
        screenLabel = Res.string.tour_screen_progress,
        title = Res.string.tour_photos_title,
        body = Res.string.tour_photos_body
    ),
    PHOTOS_FIRST_LATEST(
        screen = TourScreen.PHOTOS,
        targets = listOf(TourTarget.PHOTOS_HERO, TourTarget.PHOTOS_ALBUMS),
        screenLabel = Res.string.tour_screen_photos,
        title = Res.string.tour_first_latest_title,
        body = Res.string.tour_first_latest_body
    ),
    PHOTOS_COMPARE(
        screen = TourScreen.PHOTO_COMPARE,
        targets = listOf(TourTarget.PHOTO_COMPARE),
        screenLabel = Res.string.tour_screen_before_after,
        title = Res.string.tour_compare_title,
        body = Res.string.tour_compare_body
    ),
    PHOTOS_ALBUMS(
        screen = TourScreen.PHOTOS,
        targets = listOf(TourTarget.PHOTOS_ALBUMS),
        screenLabel = Res.string.tour_screen_photos,
        title = Res.string.tour_albums_title,
        body = Res.string.tour_albums_body
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

    /** Where this step sits among the [shown] steps. */
    val index: Int get() = shown.indexOf(element = this)

    val isFirst: Boolean get() = this == shown.first()

    val isLast: Boolean get() = this == shown.last()

    companion object {
        /** The steps this build shows: Steps, walks and runs ship in debug builds only for now. */
        val shown: List<TourStep> = entries.filter { step -> !step.isDebugOnly || isActivityEnabled }
    }
}
