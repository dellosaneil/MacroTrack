package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.moving_average_window_10
import macrotrack.composeapp.generated.resources.moving_average_window_3
import macrotrack.composeapp.generated.resources.moving_average_window_5
import macrotrack.composeapp.generated.resources.moving_average_window_7
import org.jetbrains.compose.resources.StringResource

enum class MovingAverageWindow(val days: Int, val description: StringResource) {
    THREE(days = 3, description = Res.string.moving_average_window_3),
    FIVE(days = 5, description = Res.string.moving_average_window_5),
    SEVEN(days = 7, description = Res.string.moving_average_window_7),
    TEN(days = 10, description = Res.string.moving_average_window_10)
}
