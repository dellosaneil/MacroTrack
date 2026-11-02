package org.thelazybattley.macrotrack.features.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetChecklistUseCase

/** The periods history can show, in days. */
val CHECKLIST_HISTORY_PERIODS = listOf(7, 14, 30)

data class ChecklistHistoryViewState(val checklist: Checklist? = null, val days: Int = 14)

interface ChecklistHistoryCallbacks {
    fun onPeriod(days: Int)

    companion object {
        fun default() = object : ChecklistHistoryCallbacks {
            override fun onPeriod(days: Int) {}
        }
    }
}

class ChecklistHistoryViewModel(getChecklistUseCase: GetChecklistUseCase) : ViewModel(), ChecklistHistoryCallbacks {

    private val _state = MutableStateFlow(value = ChecklistHistoryViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getChecklistUseCase().collect { checklist -> _state.update { it.copy(checklist = checklist) } }
        }
    }

    override fun onPeriod(days: Int) {
        _state.update { it.copy(days = days) }
    }
}
