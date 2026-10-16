package org.thelazybattley.macrotrack.features.quickadd

import org.thelazybattley.macrotrack.domain.model.MealType

interface QuickAddCallbacks {

    fun onModeSelected(mode: QuickAddMode)

    fun onFieldSelected(field: QuickAddField)

    fun onKey(key: Char)

    fun onLabelChanged(label: String)

    fun onMealSelected(mealType: MealType)

    /** Logs the entry; [defaultName] is used when no label was typed. */
    fun onAdd(defaultName: String)

    companion object {
        fun default() = object : QuickAddCallbacks {
            override fun onModeSelected(mode: QuickAddMode) {
            }

            override fun onFieldSelected(field: QuickAddField) {
            }

            override fun onKey(key: Char) {
            }

            override fun onLabelChanged(label: String) {
            }

            override fun onMealSelected(mealType: MealType) {
            }

            override fun onAdd(defaultName: String) {
            }
        }
    }
}
