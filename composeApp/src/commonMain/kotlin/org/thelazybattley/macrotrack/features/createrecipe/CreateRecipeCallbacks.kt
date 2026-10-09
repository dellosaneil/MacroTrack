package org.thelazybattley.macrotrack.features.createrecipe

import org.thelazybattley.macrotrack.domain.model.Food

interface CreateRecipeCallbacks {

    fun onSearchKeyword(keyword: String)

    fun onAddIngredient(food: Food)

    fun onIngredientAmountChanged(name: String, amount: Double)

    fun onSaveRecipe()

    fun inputRecipeName(name: String)

    fun removeIngredient(food: Food)

    companion object {
        fun default() = object : CreateRecipeCallbacks {
            override fun onSearchKeyword(keyword: String) {}
            override fun onAddIngredient(food: Food) {}
            override fun onIngredientAmountChanged(name: String, amount: Double) {}
            override fun onSaveRecipe() {}
            override fun inputRecipeName(name: String) {}
            override fun removeIngredient(food: Food) {}
        }
    }
}
