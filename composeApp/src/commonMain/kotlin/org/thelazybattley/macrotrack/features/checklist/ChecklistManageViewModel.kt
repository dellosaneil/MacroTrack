package org.thelazybattley.macrotrack.features.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetChecklistUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.ReorderChecklistUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SaveChecklistSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SetChecklistItemPausedUseCase

/** [isPickingDayStart] while the new-day times are open. */
data class ChecklistManageViewState(val checklist: Checklist? = null, val isPickingDayStart: Boolean = false)

interface ChecklistManageCallbacks {
    /** [ordered] is a group's items in their new order. */
    fun onReorder(ordered: List<ChecklistItem>)
    fun onPausedToggled(item: ChecklistItem)
    fun onShowOnTodayToggled()
    fun onPickDayStart(isOpen: Boolean)
    fun onDayStart(minutes: Int)

    companion object {
        fun default() = object : ChecklistManageCallbacks {
            override fun onReorder(ordered: List<ChecklistItem>) {}
            override fun onPausedToggled(item: ChecklistItem) {}
            override fun onShowOnTodayToggled() {}
            override fun onPickDayStart(isOpen: Boolean) {}
            override fun onDayStart(minutes: Int) {}
        }
    }
}

class ChecklistManageViewModel(
    getChecklistUseCase: GetChecklistUseCase,
    private val reorderChecklistUseCase: ReorderChecklistUseCase,
    private val setChecklistItemPausedUseCase: SetChecklistItemPausedUseCase,
    private val saveChecklistSettingsUseCase: SaveChecklistSettingsUseCase
) : ViewModel(), ChecklistManageCallbacks {

    private val _state = MutableStateFlow(value = ChecklistManageViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getChecklistUseCase().collect { checklist -> _state.update { it.copy(checklist = checklist) } }
        }
    }

    /** The group takes the places its items had, in its new order, so the other groups stay put. */
    override fun onReorder(ordered: List<ChecklistItem>) {
        val items = _state.value.checklist?.items?.sortedBy { it.sortOrder } ?: return
        val groupIds = ordered.map { it.id }.toSet()
        val queue = ordered.iterator()
        val merged = items.map { item -> if (item.id in groupIds && queue.hasNext()) queue.next() else item }
        viewModelScope.launch { reorderChecklistUseCase(orderedIds = merged.map { it.id }) }
    }

    override fun onPausedToggled(item: ChecklistItem) {
        viewModelScope.launch { setChecklistItemPausedUseCase(id = item.id, isPaused = !item.isPaused) }
    }

    override fun onShowOnTodayToggled() {
        viewModelScope.launch { saveChecklistSettingsUseCase { it.copy(isShownOnToday = !it.isShownOnToday) } }
    }

    override fun onPickDayStart(isOpen: Boolean) {
        _state.update { it.copy(isPickingDayStart = isOpen) }
    }

    override fun onDayStart(minutes: Int) {
        _state.update { it.copy(isPickingDayStart = false) }
        viewModelScope.launch { saveChecklistSettingsUseCase { it.copy(dayStartMinutes = minutes) } }
    }
}
