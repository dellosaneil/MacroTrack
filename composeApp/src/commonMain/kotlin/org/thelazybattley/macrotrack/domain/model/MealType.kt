package org.thelazybattley.macrotrack.domain.model

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.breakfast
import macrotrack.composeapp.generated.resources.dinner
import macrotrack.composeapp.generated.resources.lunch
import macrotrack.composeapp.generated.resources.snack
import org.jetbrains.compose.resources.StringResource

enum class MealType(
    val title: StringResource
) {
    BREAKFAST(
        title = Res.string.breakfast
    ),
    LUNCH(
        title = Res.string.lunch
    ),
    DINNER(
        title = Res.string.dinner
    ),
    SNACK(
        title = Res.string.snack
    )
}
