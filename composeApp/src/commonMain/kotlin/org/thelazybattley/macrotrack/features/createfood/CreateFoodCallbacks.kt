package org.thelazybattley.macrotrack.features.createfood

import org.thelazybattley.macrotrack.features.createfood.ui.AddFoodTextFieldType

interface CreateFoodCallbacks {

    fun onSaveFood()

    fun onTextFieldUpdated(value: String, type: AddFoodTextFieldType)

    companion object {
        fun default() = object : CreateFoodCallbacks {
            override fun onSaveFood() {
                TODO("Not yet implemented")
            }

            override fun onTextFieldUpdated(
                value: String,
                type: AddFoodTextFieldType
            ) {
                TODO("Not yet implemented")
            }
        }
    }
}
