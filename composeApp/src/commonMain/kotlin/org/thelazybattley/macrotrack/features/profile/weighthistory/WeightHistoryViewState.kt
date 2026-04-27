package org.thelazybattley.macrotrack.features.profile.weighthistory

import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

data class WeightHistoryViewState(
    val weightList: List<Weight> = emptyList(),
    val averageWeight: Double = 0.0,
    val timeRange: WeightHistoryTimeRangeEnum = WeightHistoryTimeRangeEnum.ALL
)
