package org.thelazybattley.macrotrack.features.profile

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

interface ProfileCallbacks {

    fun onSaveTargetWeight(targetKg: Double)

    fun onUnitSystemSelected(unitSystem: UnitSystem)

    fun onTelemetryToggled(isEnabled: Boolean)

    fun onStartDateSelected(date: LocalDate)

    fun onNavigate(sectionEnum: ProfileSectionEnum)

    fun onResetNavigation()

    companion object {
        fun default() = object : ProfileCallbacks {
            override fun onUnitSystemSelected(unitSystem: UnitSystem) {
            }

            override fun onTelemetryToggled(isEnabled: Boolean) {
            }

            override fun onSaveTargetWeight(targetKg: Double) {
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
