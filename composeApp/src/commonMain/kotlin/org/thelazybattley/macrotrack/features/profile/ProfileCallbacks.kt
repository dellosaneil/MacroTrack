package org.thelazybattley.macrotrack.features.profile

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

interface ProfileCallbacks {

    fun onSaveTargetWeight(targetWeight: String)

    fun onStartDateSelected(date: LocalDate)

    fun onNavigate(sectionEnum: ProfileSectionEnum)

    fun onResetNavigation()

    companion object {
        fun default() = object : ProfileCallbacks {
            override fun onSaveTargetWeight(targetWeight: String) {
            }

            override fun onStartDateSelected(date: LocalDate) {
            }

            override fun onNavigate(sectionEnum: ProfileSectionEnum) {
            }

            override fun onResetNavigation() {
            }
        }
    }
}
