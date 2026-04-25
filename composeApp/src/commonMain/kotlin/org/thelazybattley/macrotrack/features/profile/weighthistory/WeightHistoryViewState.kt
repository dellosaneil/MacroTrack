package org.thelazybattley.macrotrack.features.profile.weighthistory

import org.thelazybattley.macrotrack.domain.model.Weight

data class WeightHistoryViewState(
    val weightList: List<Weight> = emptyList(),
    val averageWeight: Double = 0.0
)
