package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.Month
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

data class WeightHistoryViewState(
    val completeWeightList: List<Weight> = emptyList(),
    val filteredWeightList: List<Weight> = emptyList(),
    val averageWeight: Double = 0.0,
    val timeRange: WeightHistoryTimeRangeEnum = WeightHistoryTimeRangeEnum.ALL,
    val weightHistoryFilter: WeightHistoryFilters = WeightHistoryFilters.All
)

sealed class WeightHistoryFilters {
    object All : WeightHistoryFilters()
    data class Monthly(
        val month: Month
    ) : WeightHistoryFilters()

    data class Weekly(
        val index: Int
    ) : WeightHistoryFilters()
}
