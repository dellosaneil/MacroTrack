package org.thelazybattley.macrotrack.features.profile.nutritiongoals

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroBasis

interface NutritionGoalsCallbacks {

    fun onGoalSelected(goal: Goal)
    fun onGoalPaceChanged(kgPerMonth: Double)
    fun onCustomSelected(isCustom: Boolean)
    fun onMacroBasisSelected(basis: MacroBasis)

    /** A body fat typed in also switches protein and fat to lean mass. */
    fun onBodyFatChanged(percent: Double)
    fun onCustomMacroGoalsChanged(macroGoals: MacroGoals)
    fun onSave()

    companion object {
        fun default() = object : NutritionGoalsCallbacks {
            override fun onGoalSelected(goal: Goal) {
            }

            override fun onGoalPaceChanged(kgPerMonth: Double) {
            }

            override fun onCustomSelected(isCustom: Boolean) {
            }

            override fun onMacroBasisSelected(basis: MacroBasis) {
            }

            override fun onBodyFatChanged(percent: Double) {
            }

            override fun onCustomMacroGoalsChanged(macroGoals: MacroGoals) {
            }

            override fun onSave() {
            }
        }
    }
}
