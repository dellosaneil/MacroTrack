package org.thelazybattley.macrotrack.features.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlannedFood

interface FoodLogCallbacks {

    fun onNavigate(mealType: MealType)
    fun resetNavigateMealTypeParameter()
    fun onDeleteFoodLog(id: Long)
    fun onDateFilterSelected(date: LocalDate)

    /** Shows the date picked on the calendar, or today. */
    fun onScreenShown()

    fun onOpenEntry(log: FoodLog)
    fun onEntryAmountChanged(amount: Double)
    fun onEntryTypingStarted()
    fun onEntryKey(key: Char)
    fun onEntryMealChanged(mealType: MealType)
    fun onCloseEntry()
    fun onSaveEntry()
    fun onDeleteEntry()

    /** The store rating dialog was requested, so it isn't asked for again until it's due. */
    fun onReviewAsked()

    /** Copies [mealType] from the selected past day into today's same meal, keeping what's there. */
    fun onCopyMealToToday(mealType: MealType)
    fun onUndoCopy()
    fun onDismissCopyToast()

    /** Logs food planned for today; Ate all logs every planned item still waiting in [mealType]. */
    fun onAtePlanned(planned: PlannedFood)
    fun onAteAllPlanned(mealType: MealType)
    fun onSkipPlanned(planned: PlannedFood)

    /** Puts eaten or skipped planned food back to waiting. */
    fun onUndoPlanned(planned: PlannedFood)

    /** Undo on the logged toast: takes the food just logged back out of the meal and into Planned. */
    fun onUndoPlannedToast()
    fun onDismissPlannedToast()

    companion object {
        fun default() = object : FoodLogCallbacks {
            override fun onNavigate(mealType: MealType) {
                TODO("Not yet implemented")
            }

            override fun resetNavigateMealTypeParameter() {
                TODO("Not yet implemented")
            }

            override fun onDeleteFoodLog(id: Long) {
                TODO("Not yet implemented")
            }

            override fun onDateFilterSelected(date: LocalDate) {
                TODO("Not yet implemented")
            }

            override fun onScreenShown() {
                TODO("Not yet implemented")
            }

            override fun onOpenEntry(log: FoodLog) = Unit
            override fun onEntryAmountChanged(amount: Double) = Unit
            override fun onEntryTypingStarted() = Unit
            override fun onEntryKey(key: Char) = Unit
            override fun onEntryMealChanged(mealType: MealType) = Unit
            override fun onCloseEntry() = Unit
            override fun onSaveEntry() = Unit
            override fun onDeleteEntry() = Unit
            override fun onReviewAsked() = Unit
            override fun onCopyMealToToday(mealType: MealType) = Unit
            override fun onUndoCopy() = Unit
            override fun onDismissCopyToast() = Unit
            override fun onAtePlanned(planned: PlannedFood) = Unit
            override fun onAteAllPlanned(mealType: MealType) = Unit
            override fun onSkipPlanned(planned: PlannedFood) = Unit
            override fun onUndoPlanned(planned: PlannedFood) = Unit
            override fun onUndoPlannedToast() = Unit
            override fun onDismissPlannedToast() = Unit
        }
    }
}
