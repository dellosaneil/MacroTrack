package org.thelazybattley.macrotrack.features.addingredient

import org.thelazybattley.macrotrack.features.addingredient.ui.AddIngredientTextFieldType

interface AddIngredientCallbacks {

    fun onSaveIngredient()

    fun onTextFieldUpdated(value: String, type: AddIngredientTextFieldType)

    companion object {
        fun default() = object : AddIngredientCallbacks {
            override fun onSaveIngredient() {
                TODO("Not yet implemented")
            }

            override fun onTextFieldUpdated(
                value: String,
                type: AddIngredientTextFieldType
            ) {
                TODO("Not yet implemented")
            }
        }
    }
}
