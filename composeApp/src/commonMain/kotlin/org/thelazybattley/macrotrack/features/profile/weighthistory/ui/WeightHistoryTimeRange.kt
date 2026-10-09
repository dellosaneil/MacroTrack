package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .padding(all = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        WeightHistoryTimeRangeEnum.entries.forEach { timeRange ->
            val isSelected = timeRange == selectedTimeRange
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable { onSelectTimeRange(timeRange) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = timeRange.stringResource),
                    style = typography.bold13,
                    color = if (isSelected) colors.ink else colors.muted
                )
            }
        }
    }
}

enum class WeightHistoryTimeRangeEnum(val stringResource: StringResource) {
    WEEK(stringResource = Res.string.week),
    MONTH(stringResource = Res.string.month),
    THREE_MONTHS(stringResource = Res.string.three_months),
    ALL(stringResource = Res.string.all)
}

@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryTimeRange() {
    MacroTrackTheme {
        WeightHistoryTimeRange(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            selectedTimeRange = WeightHistoryTimeRangeEnum.MONTH,
            onSelectTimeRange = {}
        )
    }
}
