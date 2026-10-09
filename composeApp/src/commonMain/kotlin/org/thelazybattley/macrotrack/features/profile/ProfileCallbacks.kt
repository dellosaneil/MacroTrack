package org.thelazybattley.macrotrack.features.profile

import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

interface ProfileCallbacks {

    fun onSaveTargetWeight(targetWeight: String)

    fun onNavigate(sectionEnum: ProfileSectionEnum)

    fun onResetNavigation()

    companion object {
        fun default() = object : ProfileCallbacks {
            override fun onSaveTargetWeight(targetWeight: String) {
            }

            override fun onNavigate(sectionEnum: ProfileSectionEnum) {
            }

            override fun onResetNavigation() {
            }
        }
    }
}
