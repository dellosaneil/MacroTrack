package org.thelazybattley.macrotrack.features.checklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetChecklistUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.RefillChecklistItemUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** One item's history: [month] is the first of the month on the calendar, this month until another is picked. */
data class ChecklistItemViewState(
    val itemId: Long,
    val checklist: Checklist? = null,
    val pickedMonth: LocalDate? = null,
    val isRefilling: Boolean = false
) {
    val item: ChecklistItem? get() = checklist?.item(id = itemId)
    val month: LocalDate? get() = pickedMonth ?: checklist?.today?.let { LocalDate(year = it.year, month = it.month, day = 1) }
    val canGoBack: Boolean
        get() {
            val createdOn = item?.createdOn ?: return false
            return (month ?: return false) > createdOn
        }
    val canGoForward: Boolean
        get() {
            val today = checklist?.today ?: return false
            return (month ?: return false).plus(period = DatePeriod(months = 1)) <= today
        }
}

interface ChecklistItemCallbacks {
    fun onPreviousMonth()
    fun onNextMonth()
    fun onOpenRefill(isOpen: Boolean)
    fun onRefill(added: Int, warnAt: Int)

    companion object {
        fun default() = object : ChecklistItemCallbacks {
            override fun onPreviousMonth() {}
            override fun onNextMonth() {}
            override fun onOpenRefill(isOpen: Boolean) {}
            override fun onRefill(added: Int, warnAt: Int) {}
        }
    }
}

class ChecklistItemViewModel(
    savedStateHandle: SavedStateHandle,
    getChecklistUseCase: GetChecklistUseCase,
    private val refillChecklistItemUseCase: RefillChecklistItemUseCase
) : ViewModel(), ChecklistItemCallbacks {

    private val _state = MutableStateFlow(
        value = ChecklistItemViewState(itemId = savedStateHandle.get<String>(key = AppDestinations.CHECKLIST_ITEM_ID)?.toLongOrNull() ?: 0L)
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getChecklistUseCase().collect { checklist -> _state.update { it.copy(checklist = checklist) } }
        }
    }

    override fun onPreviousMonth() {
        val currentState = _state.value
        val month = currentState.month ?: return
        if (currentState.canGoBack) _state.update { it.copy(pickedMonth = month.minus(period = DatePeriod(months = 1))) }
    }

    override fun onNextMonth() {
        val currentState = _state.value
        val month = currentState.month ?: return
        if (currentState.canGoForward) _state.update { it.copy(pickedMonth = month.plus(period = DatePeriod(months = 1))) }
    }

    override fun onOpenRefill(isOpen: Boolean) {
        _state.update { it.copy(isRefilling = isOpen) }
    }

    override fun onRefill(added: Int, warnAt: Int) {
        _state.update { it.copy(isRefilling = false) }
        viewModelScope.launch { refillChecklistItemUseCase(id = _state.value.itemId, added = added, warnAt = warnAt) }
    }
}
