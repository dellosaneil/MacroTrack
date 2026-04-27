package org.thelazybattley.macrotrack.features.profile

import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

interface ProfileCallbacks {

    fun onSaveWeight(weight: String)

    fun updateGoal(goal: Goal)

    fun onNavigate(sectionEnum: ProfileSectionEnum)

    fun onResetNavigation()

    fun clearGoalUpdated()

    companion object {
        fun default() = object : ProfileCallbacks {
            override fun onSaveWeight(weight: String) {
                TODO("Not yet implemented")
            }


            override fun updateGoal(goal: Goal) {
                TODO("Not yet implemented")
            }

            override fun onNavigate(sectionEnum: ProfileSectionEnum) {
                TODO("Not yet implemented")
            }

            override fun onResetNavigation() {
                TODO("Not yet implemented")
            }

            override fun clearGoalUpdated() {
                TODO("Not yet implemented")
            }
        }
    }
}
