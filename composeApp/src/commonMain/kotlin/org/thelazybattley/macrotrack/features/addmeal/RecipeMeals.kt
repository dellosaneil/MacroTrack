package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import kotlin.math.roundToInt

/** A recipe portion is logged as a percentage of one full serving. */
const val FULL_RECIPE_PERCENT = 100.0

/** Each recipe as a food whose macros are one full serving, the sum of its ingredients. */
fun List<Recipe>.toRecipeMeals(
    foods: List<Food>,
    calculateAdjustMacros: CalculateAdjustMacrosUseCase
): List<RecipeMeal> = map { recipe ->
    val ingredients = recipe.ingredients.mapNotNull { ingredient ->
        val ingredientAsFood = foods.find { it.name == ingredient.name } ?: return@mapNotNull null
        calculateAdjustMacros(portionSize = ingredient.weight, originalFood = ingredientAsFood)
    }
    RecipeMeal(
        food = Food(
            macros = FoodMacros(
                protein = ingredients.sumOf { it.protein },
                carbs = ingredients.sumOf { it.carbs },
                fat = ingredients.sumOf { it.fat },
                calories = ingredients.sumOf { it.calories }
            ),
            name = recipe.name,
            weight = 0.0,
            dominantMacro = recipe.dominantMacro,
            unit = "serving"
        ),
        ingredients = recipe.ingredients,
        percentage = 1.0,
        name = recipe.name
    )
}

/**
 * Recipe logs don't store the percentage eaten (their weight is 0.0), so it is worked out from the
 * calories logged against a full serving.
 */
fun recipePercentEaten(loggedCalories: Int, fullCalories: Int): Double =
    if (fullCalories > 0) (loggedCalories * FULL_RECIPE_PERCENT / fullCalories).roundToInt().toDouble() else FULL_RECIPE_PERCENT
