package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.model.MacroType

class CalculateMacroPercentageUseCase {
    operator fun invoke(
        totalCalories: Int,
        macroValue: Double,
        macroType: MacroType
    ): Double {
        return (macroType.calories * macroValue) / totalCalories
    }
}
