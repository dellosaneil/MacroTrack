package org.thelazybattley.macrotrack.features.checkin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.check_in_card_summary_less
import macrotrack.composeapp.generated.resources.check_in_card_summary_more
import macrotrack.composeapp.generated.resources.check_in_close_to_estimate
import macrotrack.composeapp.generated.resources.check_in_keep_title
import macrotrack.composeapp.generated.resources.check_in_lower_by
import macrotrack.composeapp.generated.resources.check_in_pace_faster
import macrotrack.composeapp.generated.resources.check_in_pace_not_steady
import macrotrack.composeapp.generated.resources.check_in_pace_on
import macrotrack.composeapp.generated.resources.check_in_pace_slower
import macrotrack.composeapp.generated.resources.check_in_raise_by
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.CHECK_IN_NO_CHANGE_KCAL
import org.thelazybattley.macrotrack.domain.model.CheckInPace
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

/** The pace tag: green when weight moves as planned, warm otherwise. */
@Composable
internal fun PaceTag(modifier: Modifier = Modifier, pace: CheckInPace) {
    val isOnPace = pace == CheckInPace.ON_PACE
    Text(
        text = stringResource(
            resource = when (pace) {
                CheckInPace.ON_PACE -> Res.string.check_in_pace_on
                CheckInPace.SLOWER -> Res.string.check_in_pace_slower
                CheckInPace.FASTER -> Res.string.check_in_pace_faster
                CheckInPace.NOT_STEADY -> Res.string.check_in_pace_not_steady
            }
        ),
        style = typography.bold11,
        color = if (isOnPace) colors.accentInk else colors.warmInk,
        modifier = modifier
            .background(color = if (isOnPace) colors.accentSoft else colors.warmSoft, shape = RoundedCornerShape(size = 8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** "Lower your target by 120 kcal", or "Keep 2,150 kcal a day" when the change is too small to bother with. */
@Composable
internal fun WeeklyCheckIn.Result.suggestionTitle(): String = when {
    isNoChange -> stringResource(resource = Res.string.check_in_keep_title, currentTarget.withThousands())
    targetChange < 0 -> stringResource(resource = Res.string.check_in_lower_by, abs(targetChange).withThousands())
    else -> stringResource(resource = Res.string.check_in_raise_by, targetChange.withThousands())
}

/** The measured burn against the profile's estimate, in one line. */
@Composable
internal fun WeeklyCheckIn.Result.burnSummary(): String {
    val difference = measuredBurn - estimatedBurn
    return when {
        abs(difference) <= CHECK_IN_NO_CHANGE_KCAL -> stringResource(resource = Res.string.check_in_close_to_estimate)
        difference < 0 -> stringResource(
            resource = Res.string.check_in_card_summary_less,
            measuredBurn.withThousands(),
            abs(difference).withThousands()
        )
        else -> stringResource(
            resource = Res.string.check_in_card_summary_more,
            measuredBurn.withThousands(),
            difference.withThousands()
        )
    }
}

/** "Thu, Oct 8". */
internal fun LocalDate.shortDate(): String =
    dayOfWeek.name.take(n = 3).lowercase().replaceFirstChar { it.uppercase() } + ", " + toAbbreviatedMonthDay()
