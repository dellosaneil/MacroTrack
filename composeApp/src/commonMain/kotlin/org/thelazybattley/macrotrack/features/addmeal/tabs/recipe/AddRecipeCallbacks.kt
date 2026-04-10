package org.thelazybattley.macrotrack.features.addmeal.tabs.recipe

interface AddRecipeCallbacks {

    fun insertRecipe(name: String, percentage: Double)

    fun onRecipeSelected(name: String)

    fun closeSelectedRecipe()

    fun addCustomizedFood()

    fun onPercentageEatenValue(value: Double)

    fun insertSelectedRecipe()

    fun updateRecipe(name: String)

    companion object {
        fun default() = object : AddRecipeCallbacks {
            override fun insertRecipe(name: String, percentage: Double) {
                TODO("Not yet implemented")
            }

            override fun onRecipeSelected(name: String) {
                TODO("Not yet implemented")
            }

            override fun closeSelectedRecipe() {
                TODO("Not yet implemented")
            }

            override fun addCustomizedFood() {
                TODO("Not yet implemented")
            }

            override fun onPercentageEatenValue(value: Double) {
                TODO("Not yet implemented")
            }

            override fun insertSelectedRecipe() {
                TODO("Not yet implemented")
            }

            override fun updateRecipe(name: String) {
                TODO("Not yet implemented")
            }

        }
    }
}
