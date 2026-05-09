package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.Month
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

data class WeightHistoryViewState(
    val completeWeightList: List<Weight> = emptyList(),
    val filteredWeightList: List<Weight> = emptyList(),
    val averageWeight: Double = 0.0,
    val timeRange: WeightHistoryTimeRangeEnum = WeightHistoryTimeRangeEnum.ALL,
    val monthlyWeightHistory: MonthlyWeightHistory = MonthlyWeightHistory(),
)

data class MonthlyWeightHistory(
    val monthSelected: Month = getCurrentDate().month,
    val weightsByMonth: Map<Month,List<Weight>> = emptyMap(),
)
