package org.thelazybattley.macrotrack.features.addingredient

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddIngredientViewModel: ViewModel() {

    private val _state = MutableStateFlow(value = AddIngredientViewState())
    val state = _state.asStateFlow()


}
