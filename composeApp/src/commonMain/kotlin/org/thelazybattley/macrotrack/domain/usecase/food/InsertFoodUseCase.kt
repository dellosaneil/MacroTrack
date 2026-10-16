package org.thelazybattley.macrotrack.domain.usecase.food

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class InsertFoodUseCase(private val repository: FoodRepository, private val telemetry: Telemetry) {
    suspend operator fun invoke(food: Food) {
        repository.insertFood(food = food)
        telemetry.logEvent(name = TelemetryEvent.FOOD_CREATED)
    }
}
