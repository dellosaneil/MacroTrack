package org.thelazybattley.macrotrack.features.profile.nutritiongoals

import org.thelazybattley.macrotrack.domain.model.Goal

interface NutritionGoalsCallbacks {

    fun onGoalSelected(goal: Goal)
    fun onGoalPaceChanged(kgPerMonth: Double)
    fun onSave()

    companion object {
        fun default() = object : NutritionGoalsCallbacks {
            override fun onGoalSelected(goal: Goal) {
            }

            override fun onGoalPaceChanged(kgPerMonth: Double) {
            }

            override fun onSave() {
            }
        }
    }
}
