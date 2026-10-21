package org.thelazybattley.macrotrack.features.foodlog

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.thelazybattley.macrotrack.domain.model.FoodLogCopy

/** The last copy made from the copy screens, for the food log to show with Undo once the user is back on it. */
class FoodLogCopyEvents {
    private val _latest = MutableStateFlow<FoodLogCopy?>(value = null)
    val latest: StateFlow<FoodLogCopy?> = _latest.asStateFlow()

    fun post(copy: FoodLogCopy) {
        _latest.value = copy
    }

    fun clear() {
        _latest.value = null
    }
}
