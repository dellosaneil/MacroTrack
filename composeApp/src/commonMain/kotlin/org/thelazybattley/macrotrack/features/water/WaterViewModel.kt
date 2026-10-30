package org.thelazybattley.macrotrack.features.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.WATER_AMOUNT_MAX_ML
import org.thelazybattley.macrotrack.domain.model.WATER_AMOUNT_MIN_ML
import org.thelazybattley.macrotrack.domain.model.WaterEntry
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WaterSummary
import org.thelazybattley.macrotrack.domain.usecase.water.AddWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.DeleteWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.GetWaterSummaryUseCase

/** [lastAdded] is the drink just added, shown with Undo; [isTyping] when the keypad is open for another amount. */
data class WaterViewState(
    val summary: WaterSummary? = null,
    val lastAdded: WaterEntry? = null,
    val isTyping: Boolean = false
)

interface WaterCallbacks {
    fun onAdd(kind: WaterKind)
    fun onOpenKeypad()
    fun onCloseKeypad()
    fun onAddAmount(ml: Int)
    fun onRemove(entry: WaterEntry)
    fun onUndo()
    fun onDismissNotice()

    companion object {
        fun default() = object : WaterCallbacks {
            override fun onAdd(kind: WaterKind) {}
            override fun onOpenKeypad() {}
            override fun onCloseKeypad() {}
            override fun onAddAmount(ml: Int) {}
            override fun onRemove(entry: WaterEntry) {}
            override fun onUndo() {}
            override fun onDismissNotice() {}
        }
    }
}

/** Today's water, on the Water screen and the card on Today. */
class WaterViewModel(
    getWaterSummaryUseCase: GetWaterSummaryUseCase,
    private val addWaterUseCase: AddWaterUseCase,
    private val deleteWaterUseCase: DeleteWaterUseCase
) : ViewModel(), WaterCallbacks {

    private val _state = MutableStateFlow(value = WaterViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getWaterSummaryUseCase().collect { summary -> _state.update { it.copy(summary = summary) } }
        }
    }

    override fun onAdd(kind: WaterKind) {
        val amount = _state.value.summary?.settings?.sizeOf(kind = kind) ?: return
        add(ml = amount, kind = kind)
    }

    override fun onOpenKeypad() {
        _state.update { it.copy(isTyping = true) }
    }

    override fun onCloseKeypad() {
        _state.update { it.copy(isTyping = false) }
    }

    override fun onAddAmount(ml: Int) {
        _state.update { it.copy(isTyping = false) }
        if (ml in WATER_AMOUNT_MIN_ML..WATER_AMOUNT_MAX_ML) add(ml = ml, kind = WaterKind.OTHER)
    }

    override fun onRemove(entry: WaterEntry) {
        _state.update { currentState -> currentState.copy(lastAdded = currentState.lastAdded?.takeIf { it.id != entry.id }) }
        viewModelScope.launch { deleteWaterUseCase(id = entry.id) }
    }

    override fun onUndo() {
        val entry = _state.value.lastAdded ?: return
        _state.update { it.copy(lastAdded = null) }
        viewModelScope.launch { deleteWaterUseCase(id = entry.id) }
    }

    override fun onDismissNotice() {
        _state.update { it.copy(lastAdded = null) }
    }

    private fun add(ml: Int, kind: WaterKind) {
        viewModelScope.launch {
            val entry = addWaterUseCase(amountMl = ml, kind = kind)
            _state.update { it.copy(lastAdded = entry) }
        }
    }
}
