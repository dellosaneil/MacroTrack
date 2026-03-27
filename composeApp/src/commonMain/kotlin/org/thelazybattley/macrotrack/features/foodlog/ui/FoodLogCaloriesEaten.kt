package org.thelazybattley.macrotrack.features.foodlog.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.calories_eaten
import macrotrack.composeapp.generated.resources.eaten_calories
import macrotrack.composeapp.generated.resources.remaining_calories
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.common.CommonLinearProgressBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun FoodLogCaloriesEaten(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        ),
        shape = RoundedCornerShape(size = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(resource = Res.string.calories_eaten),
                    color = colors.gray,
                    style = typography.regular11
                )
                Text(
                    text = "660",
                    color = colors.black,
                    style = typography.bold12
                )
            }
            CommonLinearProgressBar(
                modifier = Modifier.fillMaxWidth(),
                progress = 0.5f
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(resource = Res.string.eaten_calories, 660),
                    color = colors.mediumGray,
                    style = typography.regular10
                )
                Text(
                    text = stringResource(resource = Res.string.remaining_calories, 1344),
                    color = colors.mediumGray,
                    style = typography.regular10
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewFoodLogCaloriesEaten() {
    MacroTrackTheme {
        FoodLogCaloriesEaten(modifier = Modifier.fillMaxWidth().padding(all = 16.dp))
    }
}
