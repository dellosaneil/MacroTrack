package org.thelazybattley.macrotrack.domain.model

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.deficit
import macrotrack.composeapp.generated.resources.gain_weight
import macrotrack.composeapp.generated.resources.ic_gain_weight
import macrotrack.composeapp.generated.resources.ic_lose_weight
import macrotrack.composeapp.generated.resources.ic_maintain_weight
import macrotrack.composeapp.generated.resources.lose_weight
import macrotrack.composeapp.generated.resources.maintain
import macrotrack.composeapp.generated.resources.maintain_weight
import macrotrack.composeapp.generated.resources.surplus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** [direction] is -1 to lose, 1 to gain, 0 to maintain; the size of the change comes from [GoalPace]. */
enum class Goal(
    val direction: Int,
    val icon: DrawableResource,
    val title: StringResource,
    val description: StringResource
) {
    LOSE_WEIGHT(
        direction = -1,
        icon = Res.drawable.ic_lose_weight,
        title = Res.string.deficit,
        description = Res.string.lose_weight
    ),
    MAINTAIN_WEIGHT(
        direction = 0,
        icon = Res.drawable.ic_maintain_weight,
        title = Res.string.maintain,
        description = Res.string.maintain_weight
    ),
    GAIN_WEIGHT(
        direction = 1,
        icon = Res.drawable.ic_gain_weight,
        title = Res.string.surplus,
        description = Res.string.gain_weight
    )
}
