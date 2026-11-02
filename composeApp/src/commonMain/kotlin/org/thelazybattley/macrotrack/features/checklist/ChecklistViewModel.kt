package org.thelazybattley.macrotrack.features.checklist

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
import org.thelazybattley.macrotrack.domain.usecase.checklist.ChecklistAction
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetChecklistUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.RefillChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SnoozeChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.UpdateChecklistDayUseCase

/**
 * The checklist for [selectedDate], today unless another day is picked; null [selectedDate] follows today as it
 * changes. [menuItemId] has its ⋮ menu open, [refillItemId] its refill sheet.
 */
data class ChecklistViewState(
    val checklist: Checklist? = null,
    val pickedDate: LocalDate? = null,
    val menuItemId: Long? = null,
    val refillItemId: Long? = null
) {
    val selectedDate: LocalDate? get() = pickedDate ?: checklist?.today
    val isToday: Boolean get() = checklist != null && selectedDate == checklist.today
    val menuItem: ChecklistItem? get() = menuItemId?.let { checklist?.item(id = it) }
    val refillItem: ChecklistItem? get() = refillItemId?.let { checklist?.item(id = it) }
}

interface ChecklistCallbacks {
    fun onPreviousDay()
    fun onNextDay()
    fun onTap(item: ChecklistItem)
    fun onOpenMenu(item: ChecklistItem)
    fun onCloseMenu()
    fun onMenuAction(item: ChecklistItem, action: ChecklistAction)
    fun onSnooze(item: ChecklistItem)
    fun onOpenRefill(item: ChecklistItem)
    fun onCloseRefill()
    fun onRefill(item: ChecklistItem, added: Int, warnAt: Int)

    companion object {
        fun default() = object : ChecklistCallbacks {
            override fun onPreviousDay() {}
            override fun onNextDay() {}
            override fun onTap(item: ChecklistItem) {}
            override fun onOpenMenu(item: ChecklistItem) {}
            override fun onCloseMenu() {}
            override fun onMenuAction(item: ChecklistItem, action: ChecklistAction) {}
            override fun onSnooze(item: ChecklistItem) {}
            override fun onOpenRefill(item: ChecklistItem) {}
            override fun onCloseRefill() {}
            override fun onRefill(item: ChecklistItem, added: Int, warnAt: Int) {}
        }
    }
}

/** The checklist screen and the Checklist row on Today. */
class ChecklistViewModel(
    getChecklistUseCase: GetChecklistUseCase,
    private val updateChecklistDayUseCase: UpdateChecklistDayUseCase,
    private val snoozeChecklistItemUseCase: SnoozeChecklistItemUseCase,
    private val refillChecklistItemUseCase: RefillChecklistItemUseCase
) : ViewModel(), ChecklistCallbacks {

    private val _state = MutableStateFlow(value = ChecklistViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getChecklistUseCase().collect { checklist -> _state.update { it.copy(checklist = checklist) } }
        }
    }

    override fun onPreviousDay() {
        val date = _state.value.selectedDate ?: return
        _state.update { it.copy(pickedDate = date.minus(period = DatePeriod(days = 1))) }
    }

    override fun onNextDay() {
        val currentState = _state.value
        val date = currentState.selectedDate ?: return
        val today = currentState.checklist?.today ?: return
        val next = date.plus(period = DatePeriod(days = 1))
        if (next > today) return
        _state.update { it.copy(pickedDate = next.takeIf { day -> day < today }) }
    }

    override fun onTap(item: ChecklistItem) {
        val date = _state.value.selectedDate ?: return
        viewModelScope.launch { updateChecklistDayUseCase(itemId = item.id, date = date, action = ChecklistAction.TAP) }
    }

    override fun onOpenMenu(item: ChecklistItem) {
        _state.update { it.copy(menuItemId = item.id) }
    }

    override fun onCloseMenu() {
        _state.update { it.copy(menuItemId = null) }
    }

    override fun onMenuAction(item: ChecklistItem, action: ChecklistAction) {
        val date = _state.value.selectedDate ?: return
        _state.update { it.copy(menuItemId = null) }
        viewModelScope.launch { updateChecklistDayUseCase(itemId = item.id, date = date, action = action) }
    }

    override fun onSnooze(item: ChecklistItem) {
        _state.update { it.copy(menuItemId = null) }
        viewModelScope.launch { snoozeChecklistItemUseCase(itemId = item.id) }
    }

    override fun onOpenRefill(item: ChecklistItem) {
        _state.update { it.copy(refillItemId = item.id, menuItemId = null) }
    }

    override fun onCloseRefill() {
        _state.update { it.copy(refillItemId = null) }
    }

    override fun onRefill(item: ChecklistItem, added: Int, warnAt: Int) {
        _state.update { it.copy(refillItemId = null) }
        viewModelScope.launch { refillChecklistItemUseCase(id = item.id, added = added, warnAt = warnAt) }
    }
}
