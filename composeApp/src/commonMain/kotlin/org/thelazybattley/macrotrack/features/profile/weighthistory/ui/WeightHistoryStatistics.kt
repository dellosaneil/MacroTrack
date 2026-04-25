package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.average_weight
import macrotrack.composeapp.generated.resources.change
import macrotrack.composeapp.generated.resources.latest
import macrotrack.composeapp.generated.resources.start
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.to2Decimal
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WeightHistoryStatistics(
    modifier: Modifier = Modifier,
    viewState: WeightHistoryViewState,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeightStatisticCard(
                modifier = Modifier.weight(weight = 1f),
                value = viewState.weightList.first().weight.to2Decimal().toString(),
                textResource = Res.string.start,
                backgroundColor = colors.paleBlue,
                border = null,
                valueTextColor = colors.black,
                labelTextColor = colors.mediumGray
            )

            WeightStatisticCard(
                modifier = Modifier.weight(weight = 1f),
                value = viewState.weightList.last().weight.to2Decimal().toString(),
                textResource = Res.string.latest,
                backgroundColor = colors.paleBlue,
                border = null,
                valueTextColor = colors.black,
                labelTextColor = colors.mediumGray
            )
            val difference =
                viewState.weightList.last().weight - viewState.weightList.first().weight
            val differenceText = when {
                difference == 0.0 -> difference.toString()
                difference >= 0 -> "+$difference"
                else -> "-$difference"
            }


            val borderColor = if (difference <= 0) {
                colors.mossGreen
            } else {
                colors.crimsonRed
            }
            val backgroundColor = if (difference <= 0) {
                colors.seafoamGreen
            } else {
                colors.lightRed
            }

            WeightStatisticCard(
                modifier = Modifier.weight(weight = 1f),
                value = differenceText,
                textResource = Res.string.change,
                backgroundColor = backgroundColor,
                border = BorderStroke(width = 1.dp, color = borderColor),
                valueTextColor = borderColor,
                labelTextColor = borderColor
            )
        }
        WeightAverage(
            modifier = Modifier.fillMaxWidth(),
            average = viewState.averageWeight
        )
    }
}

@Composable
private fun WeightAverage(
    modifier: Modifier = Modifier,
    average: Double
) {
    Row(
        modifier = modifier
            .background(
                color = colors.paleBlue,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(resource = Res.string.average_weight),
            color = colors.mediumGray,
            style = typography.regular11
        )
        Text(
            text = average.to2Decimal().toString(),
            color = colors.deepBlue,
            style = typography.bold14
        )
    }
}

@Composable
private fun WeightStatisticCard(
    modifier: Modifier = Modifier,
    value: String,
    textResource: StringResource,
    backgroundColor: Color,
    border: BorderStroke?,
    valueTextColor: Color,
    labelTextColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = typography.bold16,
                color = valueTextColor
            )
            Text(
                text = stringResource(resource = textResource),
                color = labelTextColor,
                style = typography.regular8
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryStatistics() {
    MacroTrackTheme {
        WeightHistoryStatistics(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            viewState = WeightHistoryViewState(
                weightList = listOf(
                    Weight(
                        date = getCurrentDate(),
                        weight = 64.5,
                    ),
                    Weight(
                        date = getCurrentDate(),
                        weight = 62.7,
                    ),
                    Weight(
                        date = getCurrentDate(),
                        weight = 67.2,
                    ),
                    Weight(
                        date = getCurrentDate(),
                        weight = 65.5,
                    ),
                ),
                averageWeight = 56.3

            )
        )
    }
}
