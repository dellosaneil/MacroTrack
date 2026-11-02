package org.thelazybattley.macrotrack.features.checklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_quick_creatine
import macrotrack.composeapp.generated.resources.checklist_quick_fish_oil
import macrotrack.composeapp.generated.resources.checklist_quick_magnesium
import macrotrack.composeapp.generated.resources.checklist_quick_multivitamin
import macrotrack.composeapp.generated.resources.checklist_quick_read
import macrotrack.composeapp.generated.resources.checklist_quick_stretch
import macrotrack.composeapp.generated.resources.checklist_quick_vitamin_d
import macrotrack.composeapp.generated.resources.checklist_quick_walk
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_DEFAULT_WARN_AT
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_NAME_MAX
import org.thelazybattley.macrotrack.domain.model.ChecklistDays
import org.thelazybattley.macrotrack.domain.model.ChecklistIcon
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.domain.model.HabitGoal
import org.thelazybattley.macrotrack.domain.model.SupplementUnit
import org.thelazybattley.macrotrack.domain.usecase.checklist.DeleteChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SaveChecklistItemUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** The quick start chips on the first-time screen: each only fills in the form, for the user to check. */
enum class ChecklistPreset(val label: StringResource, val draft: ChecklistDraft) {
    CREATINE(
        label = Res.string.checklist_quick_creatine,
        draft = ChecklistDraft(icon = ChecklistIcon.PILL, amount = 5.0, unit = SupplementUnit.G, time = ChecklistTime.MORNING)
    ),
    VITAMIN_D(
        label = Res.string.checklist_quick_vitamin_d,
        draft = ChecklistDraft(icon = ChecklistIcon.SUN, amount = 1_000.0, unit = SupplementUnit.IU, time = ChecklistTime.MORNING)
    ),
    FISH_OIL(
        label = Res.string.checklist_quick_fish_oil,
        draft = ChecklistDraft(icon = ChecklistIcon.FISH, amount = 2.0, unit = SupplementUnit.CAPSULES, time = ChecklistTime.DINNER)
    ),
    MAGNESIUM(
        label = Res.string.checklist_quick_magnesium,
        draft = ChecklistDraft(icon = ChecklistIcon.LEAF, amount = 200.0, unit = SupplementUnit.MG, time = ChecklistTime.BEDTIME)
    ),
    MULTIVITAMIN(
        label = Res.string.checklist_quick_multivitamin,
        draft = ChecklistDraft(icon = ChecklistIcon.PILL, amount = 1.0, unit = SupplementUnit.TABLETS, time = ChecklistTime.BREAKFAST)
    ),
    WALK(
        label = Res.string.checklist_quick_walk,
        draft = ChecklistDraft(
            kind = ChecklistKind.HABIT,
            icon = ChecklistIcon.RUN,
            goal = HabitGoal.MINUTES,
            minutes = 10,
            time = ChecklistTime.LUNCH,
            days = ChecklistDays.WEEKDAYS.days!!
        )
    ),
    STRETCH(
        label = Res.string.checklist_quick_stretch,
        draft = ChecklistDraft(kind = ChecklistKind.HABIT, icon = ChecklistIcon.MOON, time = ChecklistTime.BEDTIME)
    ),
    READ(
        label = Res.string.checklist_quick_read,
        draft = ChecklistDraft(kind = ChecklistKind.HABIT, icon = ChecklistIcon.BOOK, goal = HabitGoal.MINUTES, minutes = 20, time = ChecklistTime.BEDTIME)
    )
}

/** The add and edit form as it's being filled in. */
data class ChecklistDraft(
    val id: Long = 0,
    val kind: ChecklistKind = ChecklistKind.SUPPLEMENT,
    val name: String = "",
    val icon: ChecklistIcon = if (kind == ChecklistKind.SUPPLEMENT) ChecklistIcon.PILL else ChecklistIcon.RUN,
    val amount: Double = 1.0,
    val unit: SupplementUnit = SupplementUnit.CAPSULES,
    val goal: HabitGoal = HabitGoal.TICK,
    val minutes: Int = 10,
    val times: Int = 3,
    val time: ChecklistTime = ChecklistTime.MORNING,
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    /** Whether the days were picked one by one, so Custom stays chosen even when they match a preset. */
    val isCustomDays: Boolean = false,
    val reminderMinutes: Int = time.defaultReminderMinutes,
    val isReminderOn: Boolean = true,
    val stock: Int = 60,
    val stockWarnAt: Int = CHECKLIST_DEFAULT_WARN_AT,
    val isStockOn: Boolean = false,
    /** What editing keeps as it was: when it started, its order, pause and refills. */
    val original: ChecklistItem? = null
) {
    val dayChoice: ChecklistDays get() = if (isCustomDays) ChecklistDays.CUSTOM else ChecklistDays.of(days = days)

    val canSave: Boolean get() = name.isNotBlank() && days.isNotEmpty() && (kind == ChecklistKind.HABIT || amount > 0)

    fun toItem(): ChecklistItem {
        val isSupplement = kind == ChecklistKind.SUPPLEMENT
        return ChecklistItem(
            id = id,
            kind = kind,
            name = name.trim(),
            icon = icon,
            amount = if (isSupplement) amount else 1.0,
            unit = unit,
            goal = if (isSupplement) HabitGoal.TICK else goal,
            goalValue = when {
                isSupplement -> 0
                goal == HabitGoal.MINUTES -> minutes
                goal == HabitGoal.TIMES -> times
                else -> 0
            },
            time = time,
            days = days,
            reminderMinutes = reminderMinutes.takeIf { isReminderOn },
            stock = stock.takeIf { isSupplement && isStockOn },
            stockWarnAt = stockWarnAt,
            refills = original?.refills ?: 0,
            sortOrder = original?.sortOrder ?: 0,
            pausedOn = original?.pausedOn,
            createdOn = original?.createdOn ?: getCurrentDate()
        )
    }

    companion object {
        fun of(item: ChecklistItem) = ChecklistDraft(
            id = item.id,
            kind = item.kind,
            name = item.name,
            icon = item.icon,
            amount = item.amount,
            unit = item.unit,
            goal = item.goal,
            minutes = if (item.goal == HabitGoal.MINUTES) item.goalValue else 10,
            times = if (item.goal == HabitGoal.TIMES) item.goalValue else 3,
            time = item.time,
            days = item.days,
            isCustomDays = ChecklistDays.of(days = item.days) == ChecklistDays.CUSTOM,
            reminderMinutes = item.reminderMinutes ?: item.time.defaultReminderMinutes,
            isReminderOn = item.reminderMinutes != null,
            stock = item.stock ?: 60,
            stockWarnAt = item.stockWarnAt,
            isStockOn = item.stock != null,
            original = item
        )
    }
}

/** The number fields typed on the keypad. */
enum class ChecklistField { AMOUNT, MINUTES, TIMES, STOCK, WARN_AT }

/**
 * [draft] is null until an item being edited has loaded. [openField] has the keypad open, [isPickingTime] the
 * reminder clock, [isPickingIcon] the icons and [isConfirmingDelete] the delete question. [isClosed] once saved or
 * deleted.
 */
data class ChecklistFormViewState(
    val draft: ChecklistDraft? = null,
    val openField: ChecklistField? = null,
    val isPickingTime: Boolean = false,
    val isPickingIcon: Boolean = false,
    val isConfirmingDelete: Boolean = false,
    val isClosed: Boolean = false
) {
    val isEditing: Boolean get() = (draft?.id ?: 0L) != 0L
}

interface ChecklistFormCallbacks {
    fun onKind(kind: ChecklistKind)
    fun onName(name: String)
    fun onIcon(icon: ChecklistIcon)
    fun onPickIcon(isOpen: Boolean)
    fun onUnit(unit: SupplementUnit)
    fun onGoal(goal: HabitGoal)
    fun onTime(time: ChecklistTime)
    fun onDayChoice(choice: ChecklistDays)
    fun onDayToggled(day: DayOfWeek)
    fun onReminderToggled()
    fun onPickTime(isOpen: Boolean)
    fun onReminderTime(minutes: Int)
    fun onStockToggled()
    fun onOpenField(field: ChecklistField?)
    fun onFieldValue(field: ChecklistField, value: Double)
    fun onSave()
    fun onConfirmDelete(isOpen: Boolean)
    fun onDelete()

    companion object {
        fun default() = object : ChecklistFormCallbacks {
            override fun onKind(kind: ChecklistKind) {}
            override fun onName(name: String) {}
            override fun onIcon(icon: ChecklistIcon) {}
            override fun onPickIcon(isOpen: Boolean) {}
            override fun onUnit(unit: SupplementUnit) {}
            override fun onGoal(goal: HabitGoal) {}
            override fun onTime(time: ChecklistTime) {}
            override fun onDayChoice(choice: ChecklistDays) {}
            override fun onDayToggled(day: DayOfWeek) {}
            override fun onReminderToggled() {}
            override fun onPickTime(isOpen: Boolean) {}
            override fun onReminderTime(minutes: Int) {}
            override fun onStockToggled() {}
            override fun onOpenField(field: ChecklistField?) {}
            override fun onFieldValue(field: ChecklistField, value: Double) {}
            override fun onSave() {}
            override fun onConfirmDelete(isOpen: Boolean) {}
            override fun onDelete() {}
        }
    }
}

/** Adding a supplement or habit, from scratch or a quick start, or editing one. */
class ChecklistFormViewModel(
    savedStateHandle: SavedStateHandle,
    getChecklistItemUseCase: GetChecklistItemUseCase,
    private val saveChecklistItemUseCase: SaveChecklistItemUseCase,
    private val deleteChecklistItemUseCase: DeleteChecklistItemUseCase
) : ViewModel(), ChecklistFormCallbacks {

    private val _state = MutableStateFlow(value = ChecklistFormViewState())
    val state = _state.asStateFlow()

    init {
        val itemId = savedStateHandle.get<String>(key = AppDestinations.CHECKLIST_ITEM_ID)?.toLongOrNull()
        val kind = savedStateHandle.get<String>(key = AppDestinations.CHECKLIST_KIND)?.let { name -> ChecklistKind.entries.find { it.name == name } }
        val preset = savedStateHandle.get<String>(key = AppDestinations.CHECKLIST_PRESET)?.let { name -> ChecklistPreset.entries.find { it.name == name } }
        viewModelScope.launch {
            val draft = when {
                itemId != null -> getChecklistItemUseCase(id = itemId)?.let { ChecklistDraft.of(item = it) }
                preset != null -> preset.draft.copy(name = getString(resource = preset.label))
                else -> null
            } ?: ChecklistDraft(kind = kind ?: ChecklistKind.SUPPLEMENT)
            _state.update { it.copy(draft = draft) }
        }
    }

    private fun edit(change: (ChecklistDraft) -> ChecklistDraft) {
        _state.update { currentState -> currentState.copy(draft = currentState.draft?.let(change)) }
    }

    override fun onKind(kind: ChecklistKind) = edit { draft ->
        if (draft.kind == kind) draft else draft.copy(kind = kind, icon = ChecklistDraft(kind = kind).icon)
    }

    override fun onName(name: String) = edit { it.copy(name = name.take(n = CHECKLIST_NAME_MAX)) }

    override fun onIcon(icon: ChecklistIcon) {
        edit { it.copy(icon = icon) }
        _state.update { it.copy(isPickingIcon = false) }
    }

    override fun onPickIcon(isOpen: Boolean) {
        _state.update { it.copy(isPickingIcon = isOpen) }
    }

    override fun onUnit(unit: SupplementUnit) = edit { it.copy(unit = unit) }

    override fun onGoal(goal: HabitGoal) = edit { it.copy(goal = goal) }

    /** The reminder follows the time to its usual hour, unless one was picked on the clock. */
    override fun onTime(time: ChecklistTime) = edit { draft ->
        val isUsualTime = draft.reminderMinutes == draft.time.defaultReminderMinutes
        draft.copy(time = time, reminderMinutes = if (isUsualTime) time.defaultReminderMinutes else draft.reminderMinutes)
    }

    override fun onDayChoice(choice: ChecklistDays) = edit { draft ->
        draft.copy(days = choice.days ?: draft.days, isCustomDays = choice == ChecklistDays.CUSTOM)
    }

    override fun onDayToggled(day: DayOfWeek) = edit { draft ->
        draft.copy(days = if (day in draft.days) draft.days - day else draft.days + day, isCustomDays = true)
    }

    override fun onReminderToggled() = edit { it.copy(isReminderOn = !it.isReminderOn) }

    override fun onPickTime(isOpen: Boolean) {
        _state.update { it.copy(isPickingTime = isOpen) }
    }

    override fun onReminderTime(minutes: Int) {
        edit { it.copy(reminderMinutes = minutes, isReminderOn = true) }
        _state.update { it.copy(isPickingTime = false) }
    }

    override fun onStockToggled() = edit { it.copy(isStockOn = !it.isStockOn) }

    override fun onOpenField(field: ChecklistField?) {
        _state.update { it.copy(openField = field) }
    }

    override fun onFieldValue(field: ChecklistField, value: Double) {
        edit { draft ->
            when (field) {
                ChecklistField.AMOUNT -> draft.copy(amount = value)
                ChecklistField.MINUTES -> draft.copy(minutes = value.toInt())
                ChecklistField.TIMES -> draft.copy(times = value.toInt())
                ChecklistField.STOCK -> draft.copy(stock = value.toInt(), isStockOn = true)
                ChecklistField.WARN_AT -> draft.copy(stockWarnAt = value.toInt())
            }
        }
    }

    override fun onSave() {
        val draft = _state.value.draft?.takeIf { it.canSave } ?: return
        viewModelScope.launch {
            saveChecklistItemUseCase(item = draft.toItem())
            _state.update { it.copy(isClosed = true) }
        }
    }

    override fun onConfirmDelete(isOpen: Boolean) {
        _state.update { it.copy(isConfirmingDelete = isOpen) }
    }

    override fun onDelete() {
        val id = _state.value.draft?.id?.takeIf { it != 0L } ?: return
        _state.update { it.copy(isConfirmingDelete = false) }
        viewModelScope.launch {
            deleteChecklistItemUseCase(id = id)
            _state.update { it.copy(isClosed = true) }
        }
    }
}
