package org.thelazybattley.macrotrack.widget

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The water button just tapped: [ml] was added as [entryId], or taken back again when [isUndone]. A widget can't
 * vibrate or pop up anything, so every widget showing water answers the tap itself while this is set. [token] tells
 * one tap from the next, so a tap's timer doesn't clear a later one.
 */
internal data class WaterTap(val entryId: Long, val ml: Int, val isUndone: Boolean, val token: Long) {
    val isAdded: Boolean get() = !isUndone
}

/** Kept in memory only: it lasts a few seconds, and the button's action runs in the app's process. */
internal object WaterFeedback {

    private val _tap = MutableStateFlow<WaterTap?>(value = null)
    val tap: StateFlow<WaterTap?> = _tap.asStateFlow()

    private var lastToken = 0L

    fun show(entryId: Long, ml: Int, isUndone: Boolean): Long {
        val token = ++lastToken
        _tap.value = WaterTap(entryId = entryId, ml = ml, isUndone = isUndone, token = token)
        return token
    }

    /** Clears the tap [token] showed, unless another tap has replaced it. True when it was cleared. */
    fun clear(token: Long): Boolean {
        var isCleared = false
        _tap.update { current ->
            if (current?.token == token) {
                isCleared = true
                null
            } else {
                current
            }
        }
        return isCleared
    }
}
