package org.thelazybattley.macrotrack.features.scanbarcode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hands a food saved by the scanner back to Add meal when the user picks "Later", so Add meal can point it out.
 * Add meal's view model stays alive under the scanner and reads it as soon as it's set.
 */
class ScannedFoodRequest {
    private val _foodName = MutableStateFlow<String?>(value = null)
    val foodName: StateFlow<String?> = _foodName.asStateFlow()

    fun request(foodName: String) {
        _foodName.value = foodName
    }

    fun clear() {
        _foodName.value = null
    }
}
