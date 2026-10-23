package org.thelazybattley.macrotrack.features.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.BurnProfile
import org.thelazybattley.macrotrack.domain.model.RouteSample
import org.thelazybattley.macrotrack.domain.model.samples
import org.thelazybattley.macrotrack.domain.usecase.activity.DeleteActivitySessionUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetActivitySessionUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetActivitySessionsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.RenameActivitySessionUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

enum class RouteChart { PACE, ELEVATION, CADENCE }

data class ActivityDetailViewState(
    val session: ActivitySession? = null,
    val samples: List<RouteSample> = emptyList(),
    val profile: BurnProfile? = null,
    val usedNames: List<String> = emptyList(),
    val chart: RouteChart = RouteChart.PACE,
    val selectedSample: Int? = null,
    /** The name being typed in the rename sheet. */
    val renameDraft: String? = null,
    val isConfirmingDelete: Boolean = false,
    val isDeleted: Boolean = false
)

interface ActivityDetailCallbacks {
    fun onChartSelected(chart: RouteChart)
    fun onSampleSelected(index: Int)
    fun onOpenRename()
    fun onRenameChanged(name: String)
    fun onCloseRename()
    fun onSaveRename()
    fun onDelete()
    fun onCancelDelete()
    fun onConfirmDelete()

    companion object {
        fun default() = object : ActivityDetailCallbacks {
            override fun onChartSelected(chart: RouteChart) = Unit
            override fun onSampleSelected(index: Int) = Unit
            override fun onOpenRename() = Unit
            override fun onRenameChanged(name: String) = Unit
            override fun onCloseRename() = Unit
            override fun onSaveRename() = Unit
            override fun onDelete() = Unit
            override fun onCancelDelete() = Unit
            override fun onConfirmDelete() = Unit
        }
    }
}

class ActivityDetailViewModel(
    savedStateHandle: SavedStateHandle,
    getActivitySessionUseCase: GetActivitySessionUseCase,
    getActivitySessionsUseCase: GetActivitySessionsUseCase,
    getBurnProfileUseCase: GetBurnProfileUseCase,
    private val renameActivitySessionUseCase: RenameActivitySessionUseCase,
    private val deleteActivitySessionUseCase: DeleteActivitySessionUseCase
) : ViewModel(), ActivityDetailCallbacks {

    private val id = savedStateHandle.get<String>(key = AppDestinations.ACTIVITY_ID)?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(value = ActivityDetailViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getActivitySessionUseCase(id = id),
                flow2 = getActivitySessionsUseCase(),
                flow3 = getBurnProfileUseCase()
            ) { session, sessions, profile ->
                _state.update { state ->
                    state.copy(
                        session = session,
                        samples = if (session?.route == state.session?.route) state.samples else session?.route?.samples().orEmpty(),
                        profile = profile,
                        usedNames = sessions.filter { it.type == session?.type }.mapNotNull { it.name }.distinct().take(n = USED_NAMES)
                    )
                }
            }.collect {}
        }
    }

    override fun onChartSelected(chart: RouteChart) {
        _state.update { it.copy(chart = chart, selectedSample = null) }
    }

    override fun onSampleSelected(index: Int) {
        _state.update { it.copy(selectedSample = if (it.selectedSample == index) null else index) }
    }

    override fun onOpenRename() {
        _state.update { it.copy(renameDraft = it.session?.name.orEmpty()) }
    }

    override fun onRenameChanged(name: String) {
        _state.update { it.copy(renameDraft = name) }
    }

    override fun onCloseRename() {
        _state.update { it.copy(renameDraft = null) }
    }

    override fun onSaveRename() {
        val state = _state.value
        val session = state.session ?: return
        val name = state.renameDraft ?: return
        _state.update { it.copy(renameDraft = null) }
        viewModelScope.launch { renameActivitySessionUseCase(id = session.id, name = name, type = session.type) }
    }

    override fun onDelete() {
        _state.update { it.copy(isConfirmingDelete = true) }
    }

    override fun onCancelDelete() {
        _state.update { it.copy(isConfirmingDelete = false) }
    }

    override fun onConfirmDelete() {
        _state.update { it.copy(isConfirmingDelete = false) }
        viewModelScope.launch {
            deleteActivitySessionUseCase(id = id)
            _state.update { it.copy(isDeleted = true) }
        }
    }

    private companion object {
        const val USED_NAMES = 6
    }
}
