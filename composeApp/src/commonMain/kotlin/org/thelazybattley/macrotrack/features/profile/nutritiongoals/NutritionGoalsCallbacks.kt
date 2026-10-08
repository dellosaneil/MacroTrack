package org.thelazybattley.macrotrack.features.profile.nutritiongoals

import org.thelazybattley.macrotrack.domain.model.Goal

interface NutritionGoalsCallbacks {

    fun onGoalSelected(goal: Goal)
    fun onSave()

    companion object {
        fun default() = object : NutritionGoalsCallbacks {
            override fun onGoalSelected(goal: Goal) {
            }

            override fun onSave() {
            }
        }
    }
}
