package org.thelazybattley.macrotrack.features.shortcuts

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.thelazybattley.macrotrack.core.platform.AppShortcut
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** The screen a shortcut on the app icon opens. Food goes to the meal of the time it's tapped. */
fun appShortcutRoute(shortcut: AppShortcut, now: LocalTime = currentTime()): String {
    val mealType = mealTypeAt(time = now).name
    return when (shortcut) {
        AppShortcut.SCAN_BARCODE -> AppDestinations.Root.ScanBarcode.createRoute(mealType = mealType)
        AppShortcut.LOG_FOOD -> AppDestinations.Root.AddMeal.createRoute(mealType = mealType)
        AppShortcut.QUICK_ADD -> AppDestinations.Root.QuickAdd.createRoute(mealType = mealType)
        AppShortcut.ADD_WATER -> AppDestinations.Root.Water.createRoute(isAddingGlass = true)
        AppShortcut.LOG_WEIGHT -> AppDestinations.Root.LogWeight.createRoute()
    }
}

/** The meal usually eaten around [time]: breakfast until 10:30, lunch until 3, a snack until 5, dinner until 9:30. */
fun mealTypeAt(time: LocalTime): MealType = when {
    time < LocalTime(hour = 10, minute = 30) -> MealType.BREAKFAST
    time < LocalTime(hour = 15, minute = 0) -> MealType.LUNCH
    time < LocalTime(hour = 17, minute = 0) -> MealType.SNACK
    time < LocalTime(hour = 21, minute = 30) -> MealType.DINNER
    else -> MealType.SNACK
}

@OptIn(ExperimentalTime::class)
private fun currentTime(): LocalTime = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault()).time
