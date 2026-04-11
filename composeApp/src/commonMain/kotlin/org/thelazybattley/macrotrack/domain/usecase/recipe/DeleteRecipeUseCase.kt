package org.thelazybattley.macrotrack.domain.usecase.recipe

import org.thelazybattley.macrotrack.domain.repository.RecipeRepository

class DeleteRecipeUseCase(private val repository: RecipeRepository) {

    suspend operator fun invoke(name: String) = repository.deleteRecipe(name = name)

}
