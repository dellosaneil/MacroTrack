package org.thelazybattley.macrotrack.features.profile.personalinformation

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserSex

interface PersonalInformationCallbacks {

    fun onAgeChanged(age: String)
    fun onHeightChanged(height: String)
    fun onHeightFeetChanged(feet: String)
    fun onHeightInchesChanged(inches: String)
    fun onUnitSystemSelected(unitSystem: UnitSystem)
    fun onSexSelected(sex: UserSex)
    fun onActivityLevelSelected(activityLevel: ActivityLevel)
    fun onSave()

    companion object {
        fun default() = object : PersonalInformationCallbacks {
            override fun onAgeChanged(age: String) {
            }

            override fun onHeightChanged(height: String) {
            }

            override fun onHeightFeetChanged(feet: String) {
            }

            override fun onHeightInchesChanged(inches: String) {
            }

            override fun onUnitSystemSelected(unitSystem: UnitSystem) {
            }

            override fun onSexSelected(sex: UserSex) {
            }

            override fun onActivityLevelSelected(activityLevel: ActivityLevel) {
            }

            override fun onSave() {
            }
        }
    }
}
