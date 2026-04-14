package org.thelazybattley.macrotrack.features.profile

import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.features.profile.ui.BMI

data class ProfileViewState(
    val profileBMI: ProfileBMI = ProfileBMI(),
    val weightInput: String = "0.0",
    val userDetails: UserDetails? = null,
)

data class ProfileBMI(
    val value: Double = 0.0,
    val category: BMI = BMI.NORMAL,
    val progress: Double = 0.0
)
