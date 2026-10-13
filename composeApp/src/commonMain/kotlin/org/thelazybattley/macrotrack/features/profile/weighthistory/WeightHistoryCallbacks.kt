package org.thelazybattley.macrotrack.features.profile.weighthistory

import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.MovingAverageWindow
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

interface WeightHistoryCallbacks {

    fun onTimePeriodSelect(timeRange: WeightHistoryTimeRangeEnum)

    fun onPreviousPeriod()

    fun onNextPeriod()

    fun onMovingAverageWindowSelect(window: MovingAverageWindow)

    companion object {
        fun default() = object : WeightHistoryCallbacks {
            override fun onTimePeriodSelect(timeRange: WeightHistoryTimeRangeEnum) {
            }

            override fun onPreviousPeriod() {
            }

            override fun onNextPeriod() {
            }

            override fun onMovingAverageWindowSelect(window: MovingAverageWindow) {
            }
        }
    }
}
