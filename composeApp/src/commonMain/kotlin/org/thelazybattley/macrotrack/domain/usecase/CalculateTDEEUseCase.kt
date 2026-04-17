package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.UserSex
import kotlin.math.roundToInt

class CalculateTDEEUseCase {
    operator fun invoke(
        height: Double,
        weight: Double,
        age: Int,
        gender: UserSex,
        activityLevel: ActivityLevel
    ): Int {
        val bmr =
            (10 * weight) + (6.25 * height) - (5 * age) + (if (gender == UserSex.MALE) 5 else -161)
        return (bmr * activityLevel.multiplier).roundToInt()
    }
}
