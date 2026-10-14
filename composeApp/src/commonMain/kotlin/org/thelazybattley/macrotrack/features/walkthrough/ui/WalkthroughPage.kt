package org.thelazybattley.macrotrack.features.walkthrough.ui

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.walkthrough_insights_description
import macrotrack.composeapp.generated.resources.walkthrough_insights_title
import macrotrack.composeapp.generated.resources.walkthrough_log_description
import macrotrack.composeapp.generated.resources.walkthrough_log_title
import macrotrack.composeapp.generated.resources.walkthrough_progress_description
import macrotrack.composeapp.generated.resources.walkthrough_progress_title
import macrotrack.composeapp.generated.resources.walkthrough_welcome_description
import macrotrack.composeapp.generated.resources.walkthrough_welcome_title
import org.jetbrains.compose.resources.StringResource

enum class WalkthroughPage(
    val titleRes: StringResource,
    val descriptionRes: StringResource
) {
    WELCOME(
        titleRes = Res.string.walkthrough_welcome_title,
        descriptionRes = Res.string.walkthrough_welcome_description
    ),
    LOG(
        titleRes = Res.string.walkthrough_log_title,
        descriptionRes = Res.string.walkthrough_log_description
    ),
    INSIGHTS(
        titleRes = Res.string.walkthrough_insights_title,
        descriptionRes = Res.string.walkthrough_insights_description
    ),
    PROGRESS(
        titleRes = Res.string.walkthrough_progress_title,
        descriptionRes = Res.string.walkthrough_progress_description
    )
}
