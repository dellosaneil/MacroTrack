package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.UserGender
import kotlin.math.roundToInt

class CalculateTDEEUseCase {
    operator fun invoke(
        height: Double,
        weight: Double,
        age: Int,
        gender: UserGender,
        activityLevel: ActivityLevel
    ): Int {
        val bmr =
            (10 * weight) + (6.25 * height) - (5 * age) + (if (gender == UserGender.MALE) 5 else -161)
        return (bmr * activityLevel.multiplier).roundToInt()
    }
}
