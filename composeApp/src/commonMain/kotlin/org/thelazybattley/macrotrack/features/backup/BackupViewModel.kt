package org.thelazybattley.macrotrack.features.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.date
import macrotrack.composeapp.generated.resources.export_item_food_log
import macrotrack.composeapp.generated.resources.export_item_goals
import macrotrack.composeapp.generated.resources.export_item_measurements
import macrotrack.composeapp.generated.resources.export_item_weigh_ins
import macrotrack.composeapp.generated.resources.export_range_30
import macrotrack.composeapp.generated.resources.export_range_90
import macrotrack.composeapp.generated.resources.export_range_all
import macrotrack.composeapp.generated.resources.export_range_year
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.goal
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.meal
import macrotrack.composeapp.generated.resources.pdf_amount
import macrotrack.composeapp.generated.resources.pdf_average_calories
import macrotrack.composeapp.generated.resources.pdf_average_macros
import macrotrack.composeapp.generated.resources.pdf_average_weight
import macrotrack.composeapp.generated.resources.pdf_days
import macrotrack.composeapp.generated.resources.pdf_days_logged
import macrotrack.composeapp.generated.resources.pdf_food
import macrotrack.composeapp.generated.resources.pdf_from
import macrotrack.composeapp.generated.resources.pdf_measurement
import macrotrack.composeapp.generated.resources.pdf_no_data
import macrotrack.composeapp.generated.resources.pdf_note
import macrotrack.composeapp.generated.resources.pdf_subtitle
import macrotrack.composeapp.generated.resources.pdf_summary
import macrotrack.composeapp.generated.resources.pdf_summary_entry
import macrotrack.composeapp.generated.resources.pdf_title
import macrotrack.composeapp.generated.resources.pdf_value
import macrotrack.composeapp.generated.resources.pdf_week_of
import macrotrack.composeapp.generated.resources.pdf_weekly_averages
import macrotrack.composeapp.generated.resources.pdf_weight_change
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.quick_add
import macrotrack.composeapp.generated.resources.recipe
import macrotrack.composeapp.generated.resources.weight
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.SharedFile
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.BackupException
import org.thelazybattley.macrotrack.domain.model.BackupInfo
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.withCurrentGoal
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.CreateBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.GetLastBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.ReadBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.RecordBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.RestoreBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.backup.export.ExportData
import org.thelazybattley.macrotrack.features.backup.export.ExportFormat
import org.thelazybattley.macrotrack.features.backup.export.ExportItem
import org.thelazybattley.macrotrack.features.backup.export.ExportRange
import org.thelazybattley.macrotrack.features.backup.export.PdfLabels
import org.thelazybattley.macrotrack.features.backup.export.buildExport

class BackupViewModel(
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getAllRecipeUseCase: GetAllRecipeUseCase,
    getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    getFoodLibraryUseCase: GetFoodLibraryUseCase,
    getUnitSystemUseCase: GetUnitSystemUseCase,
    getLastBackupUseCase: GetLastBackupUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val createBackupUseCase: CreateBackupUseCase,
    private val readBackupUseCase: ReadBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val recordBackupUseCase: RecordBackupUseCase
) : ViewModel(), BackupCallbacks {

    private val _state = MutableStateFlow(value = BackupViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val records = combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getAllWeightUseCase(),
                flow3 = getAllMeasurementsUseCase(),
                flow4 = flow { emitAll(getAllFoodUseCase()) },
                flow5 = flow { emitAll(getAllRecipeUseCase()) }
            ) { logs, weights, measurements, foods, recipes ->
                ExportData(logs = logs, weights = weights, measurements = measurements, foods = foods, recipes = recipes)
            }
            combine(
                flow = records,
                flow2 = getAllDailyGoalsUseCase(),
                flow3 = flow { emitAll(getUserDetailsUseCase()) },
                flow4 = getFoodLibraryUseCase(),
                flow5 = combine(flow = getUnitSystemUseCase(), flow2 = getLastBackupUseCase()) { units, last -> units to last }
            ) { data, goals, userDetails, libraryFoods, (unitSystem, lastBackup) ->
                val today = getCurrentDate()
                // Today's goal comes from the user's details, as on the Nutrition goals screen.
                val current = userDetails?.let {
                    DailyGoal(
                        date = today,
                        goal = it.goal,
                        macroGoals = calculateMacrosGoalUseCase(
                            height = it.height,
                            weight = it.weight,
                            age = it.age,
                            gender = it.sex,
                            activityLevel = it.activityLevel,
                            goal = it.goal
                        )
                    )
                }
                _state.update { currentState ->
                    currentState.copy(
                        data = data.copy(
                            goals = withCurrentGoal(records = goals, today = today, current = current),
                            // Logs keep only a name; a saved food wins over a library food with the same name.
                            foodUnits = libraryFoods.associate { it.food.name to it.food.unit } + data.foods.associate { it.name to it.unit }
                        ),
                        unitSystem = unitSystem,
                        lastBackup = lastBackup
                    )
                }
            }.collect()
        }
    }

    override fun onBackUpNow() {
        if (_state.value.isBackingUp) return
        _state.update { it.copy(isBackingUp = true) }
        viewModelScope.launch {
            val file = backupFile()
            _state.update { it.copy(saveRequest = SaveRequest(file = file, reason = SaveReason.BACKUP)) }
        }
    }

    override fun onSaveStarted() {
        _state.update { it.copy(saveRequest = null, isChoosingPlace = it.isChoosingPlace || it.saveRequest?.reason == SaveReason.BACKUP) }
    }

    override fun onSaveFinished(reason: SaveReason, isSaved: Boolean, sizeBytes: Int) {
        when (reason) {
            SaveReason.BACKUP -> {
                _state.update { it.copy(isBackingUp = false, isChoosingPlace = false) }
                if (isSaved) viewModelScope.launch { recordBackupUseCase(sizeBytes = sizeBytes) }
            }
            SaveReason.BEFORE_RESTORE -> {
                val dialog = _state.value.restoreDialog as? RestoreDialog.Working ?: return
                val confirm = pendingRestore ?: return
                if (!isSaved) {
                    // Without the safety copy, go back and let the user decide again.
                    _state.update { it.copy(restoreDialog = confirm) }
                    return
                }
                viewModelScope.launch { recordBackupUseCase(sizeBytes = sizeBytes) }
                restore(confirm = confirm, info = dialog.info, savedFirst = true)
            }
            SaveReason.EXPORT -> _state.update { currentState ->
                val sheet = currentState.exportSheet as? ExportSheet.Ready ?: return@update currentState
                currentState.copy(exportSheet = sheet.copy(isSaved = isSaved || sheet.isSaved))
            }
        }
    }

    override fun onRestoreClick() {
        _state.update { it.copy(isPickRequested = true) }
    }

    override fun onPickStarted() {
        _state.update { it.copy(isPickRequested = false) }
    }

    override fun onBackupPicked(bytes: ByteArray?) {
        bytes ?: return
        viewModelScope.launch {
            val dialog = try {
                RestoreDialog.Confirm(info = withContext(context = Dispatchers.Default) { readBackupUseCase(bytes = bytes) }, bytes = bytes)
            } catch (exception: BackupException) {
                RestoreDialog.Failed(problem = exception.problem)
            }
            _state.update { it.copy(restoreDialog = dialog) }
        }
    }

    override fun onBackUpFirstToggled() {
        _state.update { currentState ->
            val dialog = currentState.restoreDialog as? RestoreDialog.Confirm ?: return@update currentState
            currentState.copy(restoreDialog = dialog.copy(backUpFirst = !dialog.backUpFirst))
        }
    }

    /** The confirmed backup, kept while the safety copy is being saved. */
    private var pendingRestore: RestoreDialog.Confirm? = null

    override fun onConfirmRestore() {
        val confirm = _state.value.restoreDialog as? RestoreDialog.Confirm ?: return
        pendingRestore = confirm
        if (!confirm.backUpFirst) {
            restore(confirm = confirm, info = confirm.info, savedFirst = false)
            return
        }
        _state.update { it.copy(restoreDialog = RestoreDialog.Working(info = confirm.info, isBackingUp = true)) }
        viewModelScope.launch {
            val file = backupFile()
            _state.update { it.copy(saveRequest = SaveRequest(file = file, reason = SaveReason.BEFORE_RESTORE)) }
        }
    }

    private fun restore(confirm: RestoreDialog.Confirm, info: BackupInfo, savedFirst: Boolean) {
        _state.update { it.copy(restoreDialog = RestoreDialog.Working(info = info, isBackingUp = false)) }
        viewModelScope.launch {
            val dialog = try {
                restoreBackupUseCase(bytes = confirm.bytes)
                RestoreDialog.Done(info = info, savedFirst = savedFirst)
            } catch (exception: BackupException) {
                RestoreDialog.Failed(problem = exception.problem)
            }
            pendingRestore = null
            _state.update { it.copy(restoreDialog = dialog) }
        }
    }

    override fun onCloseRestore() {
        pendingRestore = null
        _state.update { it.copy(restoreDialog = null) }
    }

    override fun onGoToToday() {
        _state.update { it.copy(restoreDialog = null, goToToday = true) }
    }

    override fun onItemToggled(item: ExportItem) {
        _state.update { currentState ->
            val selected = currentState.selectedItems
            currentState.copy(selectedItems = if (item in selected) selected - item else selected + item)
        }
    }

    override fun onToggleAll() {
        _state.update { it.copy(selectedItems = if (it.isAllSelected) emptySet() else ExportItem.entries.toSet()) }
    }

    override fun onRangeSelected(range: ExportRange) {
        _state.update { it.copy(range = range) }
    }

    override fun onFormatSelected(format: ExportFormat) {
        _state.update { it.copy(format = format) }
    }

    override fun onExport() {
        val currentState = _state.value
        if (!currentState.canExport || currentState.exportSheet != null) return
        _state.update { it.copy(exportSheet = ExportSheet.Preparing(item = ExportItem.entries.firstOrNull { item -> item in it.selectedItems })) }
        viewModelScope.launch {
            val today = getCurrentDate()
            val labels = if (currentState.format == ExportFormat.PDF) pdfLabels(state = currentState, today = today) else PdfLabels()
            val file = withContext(context = Dispatchers.Default) {
                buildExport(
                    data = currentState.data,
                    items = currentState.selectedItems,
                    range = currentState.range,
                    format = currentState.format,
                    unitSystem = currentState.unitSystem,
                    today = today,
                    pdfLabels = labels
                )
            }
            // The sheet may have been closed while the file was being made.
            _state.update { if (it.exportSheet is ExportSheet.Preparing) it.copy(exportSheet = ExportSheet.Ready(file = file)) else it }
        }
    }

    override fun onSaveExport() {
        val sheet = _state.value.exportSheet as? ExportSheet.Ready ?: return
        _state.update {
            it.copy(saveRequest = SaveRequest(file = SharedFile(name = sheet.file.fileName, mimeType = sheet.file.mimeType, bytes = sheet.file.bytes), reason = SaveReason.EXPORT))
        }
    }

    override fun onCloseExport() {
        _state.update { it.copy(exportSheet = null) }
    }

    private suspend fun backupFile(): SharedFile {
        val bytes = withContext(context = Dispatchers.Default) { createBackupUseCase() }
        return SharedFile(name = "macrotrack-backup-${getCurrentDate()}.json", mimeType = "application/json", bytes = bytes)
    }

    private suspend fun pdfLabels(state: BackupViewState, today: LocalDate): PdfLabels {
        val imperial = state.unitSystem == UnitSystem.IMPERIAL
        val start = state.range.startDate(today = today)
            ?: listOfNotNull(
                state.data.logs.minOfOrNull { it.date },
                state.data.weights.minOfOrNull { it.date },
                state.data.measurements.minOfOrNull { it.date }
            ).minOrNull()
            ?: today
        val rangeName = getString(
            resource = when (state.range) {
                ExportRange.LAST_30_DAYS -> Res.string.export_range_30
                ExportRange.LAST_90_DAYS -> Res.string.export_range_90
                ExportRange.THIS_YEAR -> Res.string.export_range_year
                ExportRange.ALL_TIME -> Res.string.export_range_all
            }
        )
        return PdfLabels(
            title = getString(resource = Res.string.pdf_title),
            subtitle = getString(
                Res.string.pdf_subtitle,
                rangeName,
                start.longDate(),
                today.longDate(),
                if (imperial) "lb" else "kg",
                if (imperial) "oz" else "g"
            ),
            summary = getString(resource = Res.string.pdf_summary),
            summaryEntry = getString(resource = Res.string.pdf_summary_entry),
            daysLogged = getString(resource = Res.string.pdf_days_logged),
            averageCalories = getString(resource = Res.string.pdf_average_calories),
            averageMacros = getString(resource = Res.string.pdf_average_macros),
            weightChange = getString(resource = Res.string.pdf_weight_change),
            weight = getString(resource = Res.string.weight),
            weeklyAverages = getString(resource = Res.string.pdf_weekly_averages),
            weekOf = getString(resource = Res.string.pdf_week_of),
            days = getString(resource = Res.string.pdf_days),
            kcal = getString(resource = Res.string.kcal_text),
            protein = getString(resource = Res.string.protein),
            carbs = getString(resource = Res.string.carbs),
            fat = getString(resource = Res.string.fat),
            averageWeight = getString(resource = Res.string.pdf_average_weight),
            foodLog = getString(resource = Res.string.export_item_food_log),
            date = getString(resource = Res.string.date),
            meal = getString(resource = Res.string.meal),
            food = getString(resource = Res.string.pdf_food),
            amount = getString(resource = Res.string.pdf_amount),
            weighIns = getString(resource = Res.string.export_item_weigh_ins),
            measurements = getString(resource = Res.string.export_item_measurements),
            measurement = getString(resource = Res.string.pdf_measurement),
            value = getString(resource = Res.string.pdf_value),
            note = getString(resource = Res.string.pdf_note),
            goalHistory = getString(resource = Res.string.export_item_goals),
            from = getString(resource = Res.string.pdf_from),
            goal = getString(resource = Res.string.goal),
            recipe = getString(resource = Res.string.recipe),
            quickAdd = getString(resource = Res.string.quick_add),
            noData = getString(resource = Res.string.pdf_no_data),
            meals = MealType.entries.associateWith { getString(resource = it.title) },
            measurementTypes = MeasurementType.entries.associateWith { getString(resource = it.title) },
            goals = Goal.entries.associateWith { getString(resource = it.title) },
            formatDate = { it.longDate() }
        )
    }
}

/** "Oct 14, 2026" */
internal fun LocalDate.longDate(): String = "${toAbbreviatedMonthDay()}, $year"
