package org.thelazybattley.macrotrack.features.createrecipe

import org.thelazybattley.macrotrack.domain.model.Food

interface CreateRecipeCallbacks {

    fun onSearchKeyword(keyword: String)

    fun onAddIngredient(food: Food)

    fun customizeIngredientWeight(name: String)

    fun closeCustomWeight()

    fun addCustomizedIngredient()

    fun updateWeight(portionSize: Double)

    fun onSaveRecipe()

    fun inputRecipeName(name: String)

    fun removeIngredient(food: Food)

    companion object {
        fun default() = object : CreateRecipeCallbacks {
            override fun onSearchKeyword(keyword: String) {
                TODO("Not yet implemented")
            }

            override fun onAddIngredient(food: Food) {
                TODO("Not yet implemented")
            }

            override fun customizeIngredientWeight(name: String) {
                TODO("Not yet implemented")
            }

            override fun closeCustomWeight() {
                TODO("Not yet implemented")
            }

            override fun addCustomizedIngredient() {
                TODO("Not yet implemented")
            }

            override fun updateWeight(portionSize: Double) {
                TODO("Not yet implemented")
            }

            override fun onSaveRecipe() {
                TODO("Not yet implemented")
            }

            override fun inputRecipeName(name: String) {
                TODO("Not yet implemented")
            }

            override fun removeIngredient(food: Food) {
                TODO("Not yet implemented")
            }
        }
    }
}
