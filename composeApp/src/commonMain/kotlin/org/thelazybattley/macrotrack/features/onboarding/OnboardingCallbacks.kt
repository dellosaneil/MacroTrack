package org.thelazybattley.macrotrack.features.onboarding

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender

interface OnboardingCallbacks {

    fun onGoalSelected(goal: Goal)

    fun onActivityLevelSelected(activityLevel: ActivityLevel)

    fun onGenderSelected(gender: UserGender)

    fun onContinueClicked()

    fun onBackClicked()

    fun onAgeUpdated(age: Int)

    fun onHeightUpdated(height: Double)

    fun onWeightUpdated(weight: Double)


    companion object {
        fun default() = run {
            object : OnboardingCallbacks {
                override fun onGoalSelected(goal: Goal) {
                    TODO("Not yet implemented")
                }

                override fun onActivityLevelSelected(activityLevel: ActivityLevel) {
                    TODO("Not yet implemented")
                }

                override fun onGenderSelected(gender: UserGender) {
                    TODO("Not yet implemented")
                }

                override fun onContinueClicked() {
                    TODO("Not yet implemented")
                }

                override fun onBackClicked() {
                    TODO("Not yet implemented")
                }

                override fun onAgeUpdated(age: Int) {
                    TODO("Not yet implemented")
                }

                override fun onHeightUpdated(height: Double) {
                    TODO("Not yet implemented")
                }

                override fun onWeightUpdated(weight: Double) {
                    TODO("Not yet implemented")
                }

            }
        }
    }

}
