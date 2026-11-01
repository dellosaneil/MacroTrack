package org.thelazybattley.macrotrack.domain.usecase.widget

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.HomeWidgetRefresher
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WidgetSnapshot
import org.thelazybattley.macrotrack.domain.model.widgetSnapshot
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.GetWaterSummaryUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase

/** What the widgets show today, against today's goal. The day is the one when it's first collected. */
class GetWidgetSnapshotUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val getWaterSummaryUseCase: GetWaterSummaryUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val appFlagRepository: AppFlagRepository
) {
    operator fun invoke(): Flow<WidgetSnapshot> = flow {
        val today = getCurrentDate()
        emitAll(
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getUserDetailsUseCase(),
                flow3 = getWaterSummaryUseCase(),
                flow4 = getAllWeightUseCase(),
                flow5 = combine(
                    flow = appFlagRepository.getUnitSystem(),
                    flow2 = appFlagRepository.isWidgetWeightShown(),
                    flow3 = appFlagRepository.isWidgetLockNumbersShown(),
                    transform = ::WidgetFlags
                )
            ) { foodLogs, userDetails, water, weights, flags ->
                widgetSnapshot(
                    today = today,
                    foodLogs = foodLogs,
                    macroGoals = userDetails?.let { calculateMacrosGoalUseCase(userDetails = it) },
                    water = water,
                    weights = weights,
                    unitSystem = flags.unitSystem,
                    isWeightShown = flags.isWeightShown,
                    isLockNumbersShown = flags.isLockNumbersShown
                )
            }
        )
    }
}

private data class WidgetFlags(val unitSystem: UnitSystem, val isWeightShown: Boolean, val isLockNumbersShown: Boolean)

/** Redraws the widgets as food, water, weight or goals change while the app is open. */
class SyncHomeWidgetsUseCase(
    private val getWidgetSnapshotUseCase: GetWidgetSnapshotUseCase,
    private val refresher: HomeWidgetRefresher
) {
    @OptIn(FlowPreview::class)
    suspend operator fun invoke() {
        if (!refresher.isSupported) return
        // Once on open too, so widgets placed before a new day or an update catch up.
        refresher.refresh()
        getWidgetSnapshotUseCase()
            .distinctUntilChanged()
            .drop(count = 1)
            .debounce(timeoutMillis = 500)
            .collectLatest { refresher.refresh() }
    }
}

class IsWidgetWeightShownUseCase(private val repository: AppFlagRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isWidgetWeightShown()
}

class SetWidgetWeightShownUseCase(private val repository: AppFlagRepository) {
    suspend operator fun invoke(isShown: Boolean) {
        repository.setWidgetWeightShown(isShown = isShown)
    }
}

class SetWidgetLockNumbersShownUseCase(private val repository: AppFlagRepository) {
    suspend operator fun invoke(isShown: Boolean) {
        repository.setWidgetLockNumbersShown(isShown = isShown)
    }
}
