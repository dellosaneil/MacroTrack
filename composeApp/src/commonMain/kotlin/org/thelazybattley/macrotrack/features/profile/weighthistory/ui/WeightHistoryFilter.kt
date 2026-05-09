package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_left
import org.jetbrains.compose.resources.painterResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryFilters
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WeightHistoryFilter(
    modifier: Modifier = Modifier,
    weightHistoryFilters: WeightHistoryFilters,
    onNextButtonClicked: () -> Unit,
    onBackButtonClicked: () -> Unit
) {
    if (weightHistoryFilters == WeightHistoryFilters.All) return
    Row(
        modifier = modifier
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackButtonClicked,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = colors.lightGray
            ),
            shape = RoundedCornerShape(size = 8.dp)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_left),
                contentDescription = null
            )
        }
        val text = when(weightHistoryFilters) {
            is WeightHistoryFilters.Monthly -> {
                weightHistoryFilters.month.name
            }
            is WeightHistoryFilters.Weekly -> {
                "weekly"
            }
        }
        Text(
            text = text,
            style = typography.bold15,
            color = colors.black
        )

        IconButton(
            onClick = onNextButtonClicked,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = colors.lightGray
            ),
            shape = RoundedCornerShape(size = 8.dp)
        ) {
            Icon(
                modifier = Modifier.rotate(degrees = 180f),
                painter = painterResource(resource = Res.drawable.ic_chevron_left),
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryFilter() {
    MacroTrackTheme {
        WeightHistoryFilter(
            modifier = Modifier.fillMaxWidth(),
            weightHistoryFilters = WeightHistoryFilters.Monthly(
                month = getCurrentDate().month
            ),
            onNextButtonClicked = {},
            onBackButtonClicked = {}
        )
    }
}
