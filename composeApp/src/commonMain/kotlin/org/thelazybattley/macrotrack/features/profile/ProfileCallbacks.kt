package org.thelazybattley.macrotrack.features.profile

import org.thelazybattley.macrotrack.domain.model.Goal

interface ProfileCallbacks {

    fun onWeightInput(weight: String)

    fun onSaveWeight()

    fun updateGoal(goal: Goal)

    companion object {
        fun default() = object : ProfileCallbacks {
            override fun onWeightInput(weight: String) {
                TODO("Not yet implemented")
            }

            override fun onSaveWeight() {
                TODO("Not yet implemented")
            }

            override fun updateGoal(goal: Goal) {
                TODO("Not yet implemented")
            }
        }
    }
}
