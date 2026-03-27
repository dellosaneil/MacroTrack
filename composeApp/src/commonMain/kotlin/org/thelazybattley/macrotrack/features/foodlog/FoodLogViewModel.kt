package org.thelazybattley.macrotrack.features.foodlog

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FoodLogViewModel(
) : ViewModel() {

    private val _state = MutableStateFlow(value = FoodLogViewState())

    val state = _state.asStateFlow()


}
