package org.thelazybattley.macrotrack.features.profile.personalinformation

import org.thelazybattley.macrotrack.domain.model.ActivityLevel

interface PersonalInformationCallbacks {

    fun onActivityLevelSelected(activityLevel: ActivityLevel)

    companion object {
        fun default() = object : PersonalInformationCallbacks {
            override fun onActivityLevelSelected(activityLevel: ActivityLevel) {
                TODO("Not yet implemented")
            }
        }
    }
}
