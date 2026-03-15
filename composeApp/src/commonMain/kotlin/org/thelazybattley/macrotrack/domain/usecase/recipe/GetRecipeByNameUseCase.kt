package org.thelazybattley.macrotrack.domain.usecase.recipe

import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.repository.RecipeRepository

class GetRecipeByNameUseCase(private val repository: RecipeRepository) {

    suspend operator fun invoke(name: String): List<Recipe> =
        repository.getRecipeByName(name = name)

}
