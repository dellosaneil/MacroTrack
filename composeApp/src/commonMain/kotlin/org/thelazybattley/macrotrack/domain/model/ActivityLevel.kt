package org.thelazybattley.macrotrack.domain.model

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.extremely_active
import macrotrack.composeapp.generated.resources.hard_training_or_sport
import macrotrack.composeapp.generated.resources.ic_extremely_active
import macrotrack.composeapp.generated.resources.ic_lightly_active
import macrotrack.composeapp.generated.resources.ic_moderately_active
import macrotrack.composeapp.generated.resources.ic_sedentary
import macrotrack.composeapp.generated.resources.ic_very_active
import macrotrack.composeapp.generated.resources.lightly_active
import macrotrack.composeapp.generated.resources.little_or_no_exercise
import macrotrack.composeapp.generated.resources.moderately_active
import macrotrack.composeapp.generated.resources.one_to_three_days_per_week
import macrotrack.composeapp.generated.resources.sedentary
import macrotrack.composeapp.generated.resources.six_to_seven_days_a_week
import macrotrack.composeapp.generated.resources.three_to_five_days_a_week
import macrotrack.composeapp.generated.resources.very_active
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(multiplier = 1.2),
    LIGHTLY_ACTIVE(multiplier = 1.375),
    MODERATELY_ACTIVE(multiplier = 1.55),
    VERY_ACTIVE(multiplier = 1.725),
    EXTREMELY_ACTIVE(multiplier = 1.9)
}


fun ActivityLevel.descriptionRes(): StringResource {
    return when (this) {
        ActivityLevel.SEDENTARY -> Res.string.little_or_no_exercise
        ActivityLevel.LIGHTLY_ACTIVE -> Res.string.one_to_three_days_per_week
        ActivityLevel.MODERATELY_ACTIVE -> Res.string.three_to_five_days_a_week
        ActivityLevel.VERY_ACTIVE -> Res.string.six_to_seven_days_a_week
        ActivityLevel.EXTREMELY_ACTIVE -> Res.string.hard_training_or_sport
    }
}

fun ActivityLevel.iconRes(): DrawableResource {
    return when (this) {
        ActivityLevel.SEDENTARY -> Res.drawable.ic_sedentary
        ActivityLevel.LIGHTLY_ACTIVE -> Res.drawable.ic_lightly_active
        ActivityLevel.MODERATELY_ACTIVE -> Res.drawable.ic_moderately_active
        ActivityLevel.VERY_ACTIVE -> Res.drawable.ic_very_active
        ActivityLevel.EXTREMELY_ACTIVE -> Res.drawable.ic_extremely_active
    }
}

fun ActivityLevel.titleRes(): StringResource {
    return when (this) {
        ActivityLevel.SEDENTARY -> Res.string.sedentary
        ActivityLevel.LIGHTLY_ACTIVE -> Res.string.lightly_active
        ActivityLevel.MODERATELY_ACTIVE -> Res.string.moderately_active
        ActivityLevel.VERY_ACTIVE -> Res.string.very_active
        ActivityLevel.EXTREMELY_ACTIVE -> Res.string.extremely_active
    }
}
