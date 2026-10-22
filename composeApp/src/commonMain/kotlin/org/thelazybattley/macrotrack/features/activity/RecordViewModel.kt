package org.thelazybattley.macrotrack.features.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.BurnProfile
import org.thelazybattley.macrotrack.domain.model.DayActivity
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.model.routeClimb
import org.thelazybattley.macrotrack.domain.model.routeDistance
import org.thelazybattley.macrotrack.domain.usecase.activity.GetActivitySessionsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetDayActivityUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SaveActivitySessionUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Runs take longer strides than walks; used only to estimate steps on phones without a step counter. */
private const val RUN_STRIDE_FACTOR = 1.35

/** Names offered as chips for each type, most recent first. */
private const val USED_NAME_COUNT = 6

/** What's typed in the save sheet: a name, blank for the type and date, and whether it was a walk or run. */
data class SaveDraft(val name: String, val type: ActivityType)

data class RecordViewState(
    val type: ActivityType = ActivityType.WALK,
    val recording: Recording? = null,
    val today: DayActivity = DayActivity(totalSteps = 0, sessions = emptyList()),
    val goal: Int = StepGoal.DEFAULT,
    val profile: BurnProfile? = null,
    val usedNames: Map<ActivityType, List<String>> = emptyMap(),
    val saveDraft: SaveDraft? = null,
    /** Set once saved, to open its summary. */
    val savedId: Long? = null,
    val isDiscarded: Boolean = false
)

interface RecordCallbacks {
    /** Starts recording once location is allowed, unless something is already being recorded. */
    fun onStart()
    fun onPauseToggled()
    fun onFinish()
    fun onKeepRecording()
    fun onNameChanged(name: String)
    fun onTypeChanged(type: ActivityType)
    fun onSave()
    fun onDiscard()

    companion object {
        fun default() = object : RecordCallbacks {
            override fun onStart() = Unit
            override fun onPauseToggled() = Unit
            override fun onFinish() = Unit
            override fun onKeepRecording() = Unit
            override fun onNameChanged(name: String) = Unit
            override fun onTypeChanged(type: ActivityType) = Unit
            override fun onSave() = Unit
            override fun onDiscard() = Unit
        }
    }
}

class RecordViewModel(
    savedStateHandle: SavedStateHandle,
    private val recorder: ActivityRecorder,
    getDayActivityUseCase: GetDayActivityUseCase,
    getStepGoalUseCase: GetStepGoalUseCase,
    getBurnProfileUseCase: GetBurnProfileUseCase,
    getActivitySessionsUseCase: GetActivitySessionsUseCase,
    private val saveActivitySessionUseCase: SaveActivitySessionUseCase
) : ViewModel(), RecordCallbacks {

    private val type = savedStateHandle.get<String>(key = AppDestinations.ACTIVITY_TYPE)
        ?.let { name -> ActivityType.entries.find { it.name == name } }
        ?: ActivityType.WALK

    private val _state = MutableStateFlow(value = RecordViewState(type = type))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            recorder.state.collect { recording -> _state.update { it.copy(recording = recording) } }
        }
        viewModelScope.launch {
            combine(
                flow = getDayActivityUseCase(date = getCurrentDate()),
                flow2 = getStepGoalUseCase(),
                flow3 = getBurnProfileUseCase(),
                flow4 = getActivitySessionsUseCase()
            ) { today, goal, profile, sessions ->
                _state.update {
                    it.copy(
                        today = today,
                        goal = goal,
                        profile = profile,
                        usedNames = ActivityType.entries.associateWith { type ->
                            sessions.filter { session -> session.type == type }.mapNotNull { session -> session.name }.distinct().take(n = USED_NAME_COUNT)
                        }
                    )
                }
            }.collect {}
        }
    }

    override fun onStart() {
        recorder.start(type = type)
    }

    override fun onPauseToggled() {
        if (_state.value.recording?.isPaused == true) recorder.resume() else recorder.pause()
    }

    override fun onFinish() {
        val recording = _state.value.recording ?: return
        recorder.pause()
        _state.update { it.copy(saveDraft = SaveDraft(name = "", type = recording.type)) }
    }

    override fun onKeepRecording() {
        _state.update { it.copy(saveDraft = null) }
        recorder.resume()
    }

    override fun onNameChanged(name: String) {
        _state.update { state -> state.copy(saveDraft = state.saveDraft?.copy(name = name)) }
    }

    override fun onTypeChanged(type: ActivityType) {
        _state.update { state -> state.copy(saveDraft = state.saveDraft?.copy(type = type)) }
    }

    @OptIn(ExperimentalTime::class)
    override fun onSave() {
        val draft = _state.value.saveDraft ?: return
        val profile = _state.value.profile
        val recording = recorder.finish() ?: return
        val now = Clock.System.now().toEpochMilliseconds()
        val distance = recording.points.routeDistance()
        val estimatedSteps = profile?.let {
            val stride = it.stepLengthMeters * if (draft.type == ActivityType.RUN) RUN_STRIDE_FACTOR else 1.0
            (distance / stride).roundToInt()
        } ?: 0
        val session = ActivitySession(
            type = draft.type,
            name = draft.name.trim().takeIf { it.isNotEmpty() },
            startMillis = recording.startMillis,
            endMillis = now,
            movingSeconds = (recording.movingMillis(now = now) / 1000).toInt(),
            distanceMeters = distance,
            // Phones without a step counter get an estimate from the distance.
            steps = if (recording.steps > 0) recording.steps else estimatedSteps,
            climbMeters = recording.points.routeClimb(),
            route = recording.points
        )
        _state.update { it.copy(saveDraft = null) }
        viewModelScope.launch {
            val id = saveActivitySessionUseCase(session = session)
            _state.update { it.copy(savedId = id) }
        }
    }

    override fun onDiscard() {
        recorder.discard()
        _state.update { it.copy(saveDraft = null, isDiscarded = true) }
    }
}
