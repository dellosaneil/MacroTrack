package org.thelazybattley.macrotrack.widget

import kotlinx.datetime.DayOfWeek
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.StreakDayState
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WidgetAmount
import org.thelazybattley.macrotrack.domain.model.WidgetMeal
import org.thelazybattley.macrotrack.domain.model.WidgetSnapshot
import org.thelazybattley.macrotrack.domain.model.WidgetStreak
import org.thelazybattley.macrotrack.domain.model.WidgetStreakDay
import org.thelazybattley.macrotrack.domain.model.WidgetWater
import org.thelazybattley.macrotrack.domain.model.WidgetWeight

/** A made-up day for the widget list's previews: halfway through the day's calories, dinner still to log. */
internal fun sampleSnapshot(): WidgetSnapshot {
    val streak = listOf(StreakDayState.LOGGED, StreakDayState.LOGGED, StreakDayState.LOGGED, StreakDayState.OPEN) +
        List(size = 3) { StreakDayState.LATER }
    return WidgetSnapshot(
        date = getCurrentDate(),
        isSetUp = true,
        calories = WidgetAmount(eaten = 1_100, goal = 2_150),
        protein = WidgetAmount(eaten = 74, goal = 160),
        carbs = WidgetAmount(eaten = 140, goal = 220),
        fat = WidgetAmount(eaten = 30, goal = 70),
        meals = listOf(
            WidgetMeal(mealType = MealType.BREAKFAST, foodNames = listOf("Greek yogurt", "Oats", "Berries"), calories = 460),
            WidgetMeal(mealType = MealType.LUNCH, foodNames = listOf("Chicken rice bowl", "Iced latte"), calories = 640),
            WidgetMeal(mealType = MealType.DINNER, foodNames = emptyList(), calories = 0),
            WidgetMeal(mealType = MealType.SNACK, foodNames = emptyList(), calories = 0)
        ),
        water = WidgetWater(totalMl = 1_250, goalMl = 2_600, glassMl = 250),
        weight = WidgetWeight(trendKg = 74.2, weekChangeKg = -0.7, line = listOf(74.9, 74.8, 74.6, 74.6, 74.4, 74.3, 74.2)),
        streak = WidgetStreak(
            days = 3,
            isTodayLogged = false,
            week = DayOfWeek.entries.zip(streak) { day, state -> WidgetStreakDay(dayOfWeek = day, state = state) }
        ),
        unitSystem = UnitSystem.METRIC,
        isWeightShown = true
    )
}
