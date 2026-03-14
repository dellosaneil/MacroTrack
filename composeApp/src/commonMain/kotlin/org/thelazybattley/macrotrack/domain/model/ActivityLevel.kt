package org.thelazybattley.macrotrack.domain.model

enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(multiplier = 1.2),
    LIGHTLY_ACTIVE(multiplier = 1.375),
    MODERATELY_ACTIVE(multiplier = 1.55),
    VERY_ACTIVE(multiplier = 1.725),
    EXTREMELY_ACTIVE(multiplier = 1.9)
}
