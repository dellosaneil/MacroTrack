package org.thelazybattley.macrotrack.features.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.WidgetSnapshot
import org.thelazybattley.macrotrack.domain.usecase.widget.GetWidgetSnapshotUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.SetWidgetWeightShownUseCase

/** [snapshot] is today as the widgets show it, for the preview; null until it loads. */
data class WidgetsViewState(
    val snapshot: WidgetSnapshot? = null
)

interface WidgetsCallbacks {

    fun onWeightShownToggled()

    companion object {
        fun default() = object : WidgetsCallbacks {
            override fun onWeightShownToggled() = Unit
        }
    }
}

class WidgetsViewModel(
    getWidgetSnapshotUseCase: GetWidgetSnapshotUseCase,
    private val setWidgetWeightShownUseCase: SetWidgetWeightShownUseCase
) : ViewModel(), WidgetsCallbacks {

    private val _state = MutableStateFlow(value = WidgetsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getWidgetSnapshotUseCase().collect { snapshot -> _state.update { it.copy(snapshot = snapshot) } }
        }
    }

    override fun onWeightShownToggled() {
        val isShown = _state.value.snapshot?.isWeightShown ?: return
        viewModelScope.launch { setWidgetWeightShownUseCase(isShown = !isShown) }
    }
}
