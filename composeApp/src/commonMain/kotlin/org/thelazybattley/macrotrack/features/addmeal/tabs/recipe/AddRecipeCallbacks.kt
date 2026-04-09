package org.thelazybattley.macrotrack.features.addmeal.tabs.recipe

interface AddRecipeCallbacks {

    fun insertRecipe(name: String, percentage: Double)

    fun onRecipeSelected(name: String)


    companion object {
        fun default() = object : AddRecipeCallbacks {
            override fun insertRecipe(name: String, percentage: Double) {
                TODO("Not yet implemented")
            }

            override fun onRecipeSelected(name: String) {
                TODO("Not yet implemented")
            }

        }
    }
}
