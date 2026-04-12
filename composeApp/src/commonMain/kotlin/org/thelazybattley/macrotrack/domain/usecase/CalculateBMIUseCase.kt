package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

class CalculateBMIUseCase(private val getUserDetailsUseCase: GetUserDetailsUseCase) {

    suspend operator fun invoke(): Double {
        val userDetails = getUserDetailsUseCase() ?: return 0.0
        val weight = userDetails.weight
        val heightInMeter = userDetails.height / 100.0
        return weight / (heightInMeter * heightInMeter)
    }


}
