package org.thelazybattley.macrotrack.features.reminders

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.LogReminderAction
import org.thelazybattley.macrotrack.domain.model.LogReminderKind
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/**
 * The screen a reminder or one of its buttons opens: Add meal or Quick add for the meal, Log weight, or the
 * Reminders screen. Null for the buttons that don't open the app. A reminder from an earlier day logs to that day.
 */
fun logReminderRoute(kind: LogReminderKind, date: LocalDate, action: LogReminderAction = LogReminderAction.LOG): String? {
    val logDate = date.takeIf { it != getCurrentDate() }
    val mealType = kind.mealType
    return when (action) {
        LogReminderAction.LOG -> when {
            mealType != null -> AppDestinations.Root.AddMeal.createRoute(mealType = mealType.name, logDate = logDate)
            kind == LogReminderKind.WEIGHT -> AppDestinations.Root.LogWeight.createRoute(logDate = logDate)
            else -> AppDestinations.Root.Reminders.route
        }
        LogReminderAction.QUICK_ADD -> mealType?.let { AppDestinations.Root.QuickAdd.createRoute(mealType = it.name, logDate = logDate) }
        LogReminderAction.CHANGE_TIMES -> AppDestinations.Root.Reminders.route
        LogReminderAction.SAME_AS_YESTERDAY, LogReminderAction.LATER, LogReminderAction.SKIP, LogReminderAction.KEEP_ON -> null
    }
}

/** A screen a tapped notification asks for, opened over the main screen once the app is past the splash. */
class NotificationRouteRequest {
    private val _route = MutableStateFlow<String?>(value = null)
    val route: StateFlow<String?> = _route.asStateFlow()

    fun request(route: String) {
        _route.value = route
    }

    fun consume() {
        _route.value = null
    }
}
