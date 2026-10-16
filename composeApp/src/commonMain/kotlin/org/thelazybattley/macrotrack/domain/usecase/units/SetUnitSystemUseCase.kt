package org.thelazybattley.macrotrack.domain.usecase.units

import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class SetUnitSystemUseCase(private val repository: AppFlagRepository) {

    suspend operator fun invoke(unitSystem: UnitSystem) = repository.setUnitSystem(unitSystem = unitSystem)
}
