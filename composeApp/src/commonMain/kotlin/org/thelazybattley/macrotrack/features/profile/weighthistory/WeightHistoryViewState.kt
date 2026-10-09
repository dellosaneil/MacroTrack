package org.thelazybattley.macrotrack.features.profile.weighthistory

import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

data class WeightHistoryViewState(
    val userDetails: UserDetails? = null,
    val allWeights: List<Weight> = emptyList(),
    val timeRange: WeightHistoryTimeRangeEnum = WeightHistoryTimeRangeEnum.MONTH,
    val period: WeightPeriod? = null,
    val periodWeights: List<Weight> = emptyList(),
    val previousPeriod: WeightPeriod? = null,
    val previousWeights: List<Weight> = emptyList(),
    val canGoBack: Boolean = false,
    val canGoNext: Boolean = false
) {
    val average: Double? get() = periodWeights.takeIf { it.isNotEmpty() }?.map { it.weight }?.average()
    val previousAverage: Double? get() = previousWeights.takeIf { it.isNotEmpty() }?.map { it.weight }?.average()
}
