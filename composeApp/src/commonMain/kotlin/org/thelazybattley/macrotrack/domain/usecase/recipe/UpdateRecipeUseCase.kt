package org.thelazybattley.macrotrack.domain.usecase.recipe

import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.repository.RecipeRepository

class UpdateRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.updateRecipe(recipe = recipe)
    }
}
