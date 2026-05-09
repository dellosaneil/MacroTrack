package org.thelazybattley.macrotrack.features.profile.weighthistory

import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

interface WeightHistoryCallbacks {

    fun onTimePeriodSelect(timeRange: WeightHistoryTimeRangeEnum)

    fun onFilterBackButtonClicked()

    fun onFilterNextButtonClicked()

    companion object {
        fun default() = object : WeightHistoryCallbacks {
            override fun onTimePeriodSelect(timeRange: WeightHistoryTimeRangeEnum) {
                TODO("Not yet implemented")
            }

            override fun onFilterBackButtonClicked() {
                TODO("Not yet implemented")
            }

            override fun onFilterNextButtonClicked() {
                TODO("Not yet implemented")
            }
        }
    }
}
