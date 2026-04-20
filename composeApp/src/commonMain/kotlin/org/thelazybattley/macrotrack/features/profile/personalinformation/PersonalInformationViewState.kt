package org.thelazybattley.macrotrack.features.profile.personalinformation

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.UserDetails

data class PersonalInformationViewState(
    val userDetails: UserDetails? = null,
    val updatedActivityLevel: ActivityLevel? = null
)
