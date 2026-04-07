package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.model.MacroType

class CalculateMacroPercentageUseCase {
    operator fun invoke(
        totalCalories: Int,
        macroValue: Double,
        macroType: MacroType
    ): Double {
        if(totalCalories == 0 ) return 0.0
        return (macroType.calories * macroValue) / totalCalories
    }
}
