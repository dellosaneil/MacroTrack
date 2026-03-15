package org.thelazybattley.macrotrack.domain.usecase.recipe

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.repository.RecipeRepository

class GetAllRecipeUseCase(private val repository: RecipeRepository) {

    suspend operator fun invoke(): Flow<List<Recipe>> = repository.getAllRecipe()

}
