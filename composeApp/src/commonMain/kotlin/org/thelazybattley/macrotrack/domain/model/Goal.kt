package org.thelazybattley.macrotrack.domain.model

enum class Goal(val calorieAdjust: Int) {
    LOSE_WEIGHT(calorieAdjust = -500),
    MAINTAIN_WEIGHT(calorieAdjust = 0),
    GAIN_WEIGHT(calorieAdjust = 300)
}
