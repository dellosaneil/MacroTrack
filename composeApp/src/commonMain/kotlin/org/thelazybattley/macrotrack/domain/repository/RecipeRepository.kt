package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.Recipe

interface RecipeRepository {

    suspend fun insertRecipe(recipe: Recipe)

    suspend fun getAllRecipe(): Flow<List<Recipe>>

    suspend fun getRecipeByName(name: String): Flow<List<Recipe>>

    suspend fun updateRecipe(recipe: Recipe)

    suspend fun deleteRecipe(name: String)


}
