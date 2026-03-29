package org.thelazybattley.macrotrack.features.foodlog

import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.UserDetails

data class FoodLogViewState(
    val breakfastFood : List<FoodLog> = emptyList(),
    val lunchFood : List<FoodLog> = emptyList(),
    val dinnerFood : List<FoodLog> = emptyList(),
    val snackFood : List<FoodLog> = emptyList(),
    val allFood : List<FoodLog> = emptyList(),
    val userDetails: UserDetails? = null
)
