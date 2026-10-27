package org.thelazybattley.macrotrack.features.reminders

import kotlinx.datetime.DayOfWeek
import org.thelazybattley.macrotrack.domain.model.MealType

interface RemindersCallbacks {

    fun onMasterToggled()

    fun onMealToggled(mealType: MealType)

    /** Setting a time turns that reminder on. */
    fun onMealTimePicked(mealType: MealType, minutes: Int)

    fun onWeightToggled()

    fun onWeightTimePicked(minutes: Int)

    fun onWeightDayToggled(day: DayOfWeek)

    fun onQuickActionsToggled()

    fun onEaseOffToggled()

    fun onQuietWeekendsToggled()

    fun onSendTest()

    companion object {
        fun default() = object : RemindersCallbacks {
            override fun onMasterToggled() {
            }

            override fun onMealToggled(mealType: MealType) {
            }

            override fun onMealTimePicked(mealType: MealType, minutes: Int) {
            }

            override fun onWeightToggled() {
            }

            override fun onWeightTimePicked(minutes: Int) {
            }

            override fun onWeightDayToggled(day: DayOfWeek) {
            }

            override fun onQuickActionsToggled() {
            }

            override fun onEaseOffToggled() {
            }

            override fun onQuietWeekendsToggled() {
            }

            override fun onSendTest() {
            }
        }
    }
}
