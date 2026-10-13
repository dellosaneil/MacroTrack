package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

enum class MovingAverageWindow(val days: Int) {
    THREE(days = 3),
    FIVE(days = 5),
    SEVEN(days = 7),
    TEN(days = 10)
}
