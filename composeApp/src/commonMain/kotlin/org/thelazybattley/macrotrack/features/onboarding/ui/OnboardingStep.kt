package org.thelazybattley.macrotrack.features.onboarding.ui

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.choose_your_goal
import macrotrack.composeapp.generated.resources.how_active_title
import macrotrack.composeapp.generated.resources.personalized_plan
import macrotrack.composeapp.generated.resources.set_up_your_profile
import macrotrack.composeapp.generated.resources.typical_week_sets_target
import macrotrack.composeapp.generated.resources.youre_all_set
import org.jetbrains.compose.resources.StringResource

enum class OnboardingStep(
    val titleRes: StringResource,
    val descriptionRes: StringResource
) {
    GOAL_AND_STATS(
        titleRes = Res.string.set_up_your_profile,
        descriptionRes = Res.string.choose_your_goal
    ),
    ACTIVITY_LEVEL(
        titleRes = Res.string.how_active_title,
        descriptionRes = Res.string.typical_week_sets_target
    ),
    SUMMARY(
        titleRes = Res.string.youre_all_set,
        descriptionRes = Res.string.personalized_plan
    )
}
