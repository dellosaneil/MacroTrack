package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.all
import macrotrack.composeapp.generated.resources.month
import macrotrack.composeapp.generated.resources.three_months
import macrotrack.composeapp.generated.resources.week
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WeightHistoryTimeRange(
    modifier: Modifier = Modifier,
    selectedTimeRange: WeightHistoryTimeRangeEnum,
    onSelectTimeRange: (WeightHistoryTimeRangeEnum) -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = colors.paleBlue,
                shape = RoundedCornerShape(size = 8.dp)
            ),
    ) {
        Row(
            modifier = Modifier.padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            WeightHistoryTimeRangeEnum.entries.forEach { timeRange ->
                val textStyle = if (timeRange == selectedTimeRange) {
                    typography.bold11.copy(
                        color = colors.black
                    )
                } else {
                    typography.regular11.copy(
                        color = colors.mediumGray
                    )
                }
                val background = if (timeRange == selectedTimeRange) {
                    colors.white
                } else {
                    Color.Transparent
                }

                Text(
                    text = stringResource(resource = timeRange.stringResource),
                    style = textStyle,
                    modifier = Modifier
                        .clickable {
                            onSelectTimeRange(timeRange)
                        }
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .background(color = background, shape = RoundedCornerShape(size = 8.dp))
                        .padding(all = 8.dp)
                        .weight(weight = 1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewWeightHistoryTimeRange() {
    MacroTrackTheme {
        WeightHistoryTimeRange(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            selectedTimeRange = WeightHistoryTimeRangeEnum.WEEK
        ) {

        }
    }
}


enum class WeightHistoryTimeRangeEnum(val stringResource: StringResource) {
    ALL(stringResource = Res.string.all),
    WEEK(stringResource = Res.string.week),
    MONTH(stringResource = Res.string.month),
    THREE_MONTHS(stringResource = Res.string.three_months)
}
