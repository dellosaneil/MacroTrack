package org.thelazybattley.macrotrack.features.addfood

import org.thelazybattley.macrotrack.features.addfood.ui.AddFoodTextFieldType

interface AddFoodCallbacks {

    fun onSaveFood()

    fun onTextFieldUpdated(value: String, type: AddFoodTextFieldType)

    companion object {
        fun default() = object : AddFoodCallbacks {
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
