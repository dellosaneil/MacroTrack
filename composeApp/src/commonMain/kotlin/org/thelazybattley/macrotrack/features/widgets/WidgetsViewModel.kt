package org.thelazybattley.macrotrack.features.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.platform.HomeWidgetRefresher
import org.thelazybattley.macrotrack.core.platform.WidgetPlatform
import org.thelazybattley.macrotrack.domain.model.WidgetSnapshot
import org.thelazybattley.macrotrack.domain.usecase.widget.SetWidgetLockNumbersShownUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.GetWidgetSnapshotUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.SetWidgetWeightShownUseCase

/** [snapshot] is today as the widgets show it, for the preview; null until it loads. */
data class WidgetsViewState(
    val platform: WidgetPlatform = WidgetPlatform.ANDROID,
    val snapshot: WidgetSnapshot? = null
)

interface WidgetsCallbacks {

    fun onWeightShownToggled()

    fun onLockNumbersToggled()

    companion object {
        fun default() = object : WidgetsCallbacks {
            override fun onWeightShownToggled() = Unit
            override fun onLockNumbersToggled() = Unit
        }
    }
}

class WidgetsViewModel(
    getWidgetSnapshotUseCase: GetWidgetSnapshotUseCase,
    refresher: HomeWidgetRefresher,
    private val setWidgetWeightShownUseCase: SetWidgetWeightShownUseCase,
    private val setWidgetLockNumbersShownUseCase: SetWidgetLockNumbersShownUseCase
) : ViewModel(), WidgetsCallbacks {

    private val _state = MutableStateFlow(value = WidgetsViewState(platform = refresher.platform))
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

    override fun onLockNumbersToggled() {
        val isShown = _state.value.snapshot?.isLockNumbersShown ?: return
        viewModelScope.launch { setWidgetLockNumbersShownUseCase(isShown = !isShown) }
    }
}
