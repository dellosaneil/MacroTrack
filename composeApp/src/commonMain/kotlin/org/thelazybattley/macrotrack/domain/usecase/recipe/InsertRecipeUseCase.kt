package org.thelazybattley.macrotrack.domain.usecase.recipe

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.repository.RecipeRepository

class InsertRecipeUseCase(
    private val repository: RecipeRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.insertRecipe(recipe = recipe)
        telemetry.logEvent(name = TelemetryEvent.RECIPE_CREATED)
    }
}
