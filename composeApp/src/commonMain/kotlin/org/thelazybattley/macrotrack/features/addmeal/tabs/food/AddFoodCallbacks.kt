package org.thelazybattley.macrotrack.features.addmeal.tabs.food

import org.thelazybattley.macrotrack.domain.model.Food

interface AddFoodCallbacks {

    fun insertFood(food: Food)

    fun customizeFoodWeight(name: String)

    fun closeCustomizeFoodWeight()

    fun updateWeight(portionSize: Double)

    fun addCustomizedFood()

    companion object {
        fun default() = object : AddFoodCallbacks {
            override fun insertFood(food: Food) {
                TODO("Not yet implemented")
            }

            override fun customizeFoodWeight(name: String) {
                TODO("Not yet implemented")
            }

            override fun closeCustomizeFoodWeight() {
                TODO("Not yet implemented")
            }

            override fun updateWeight(portionSize: Double) {
                TODO("Not yet implemented")
            }

            override fun addCustomizedFood() {
                TODO("Not yet implemented")
            }

        }
    }
}
