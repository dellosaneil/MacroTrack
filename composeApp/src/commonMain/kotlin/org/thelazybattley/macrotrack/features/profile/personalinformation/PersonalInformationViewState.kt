package org.thelazybattley.macrotrack.features.profile.personalinformation

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.Weight

data class PersonalInformationViewState(
    val userDetails: UserDetails? = null,
    val latestWeight: Weight? = null,
    val age: String = "",
    val height: String = "",
    val sex: UserSex? = null,
    val activityLevel: ActivityLevel? = null,
    val dailyCalories: Int? = null,
    val isSaved: Boolean = false
) {
    val parsedAge: Int? get() = age.toIntOrNull()?.takeIf { it in MIN_AGE..MAX_AGE }
    val parsedHeight: Double? get() = height.toDoubleOrNull()?.takeIf { it in MIN_HEIGHT..MAX_HEIGHT }

    val currentWeight: Double? get() = latestWeight?.weight ?: userDetails?.weight

    val hasChanges: Boolean
        get() = userDetails != null && (
            parsedAge != userDetails.age ||
                parsedHeight != userDetails.height ||
                sex != userDetails.sex ||
                activityLevel != userDetails.activityLevel
            )

    val canSave: Boolean get() = parsedAge != null && parsedHeight != null && hasChanges

    companion object {
        const val MIN_AGE = 10
        const val MAX_AGE = 120
        const val MIN_HEIGHT = 50.0
        const val MAX_HEIGHT = 272.0
    }
}
