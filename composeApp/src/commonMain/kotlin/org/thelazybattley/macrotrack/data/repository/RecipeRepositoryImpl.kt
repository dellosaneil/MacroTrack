package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.RecipeDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.model.toEntity
import org.thelazybattley.macrotrack.domain.repository.RecipeRepository

class RecipeRepositoryImpl(private val dao: RecipeDao) : RecipeRepository {
    override suspend fun insertRecipe(recipe: Recipe) {
        dao.insertRecipe(recipe = recipe.toEntity())
    }

    override suspend fun getAllRecipe(): Flow<List<Recipe>> {
        return dao.getAllRecipes().map { flow ->
            flow.map { recipeEntity ->
                recipeEntity.toDomain()
            }
        }
    }

    override suspend fun getRecipeByName(name: String): Flow<List<Recipe>> {
        return dao.getRecipeByName(name = name).map { flow ->
            flow.map { entity ->
                entity.toDomain()
            }
        }
    }
}
