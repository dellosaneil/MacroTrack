package org.thelazybattley.macrotrack.features.createrecipe

import org.thelazybattley.macrotrack.domain.model.Food

interface CreateRecipeCallbacks {

    fun onSearchKeyword(keyword: String)

    fun onAddIngredient(food: Food)

    companion object {
        fun default() = object : CreateRecipeCallbacks {
            override fun onSearchKeyword(keyword: String) {
                TODO("Not yet implemented")
            }

            override fun onAddIngredient(food: Food) {
                TODO("Not yet implemented")
            }
        }
    }
}
