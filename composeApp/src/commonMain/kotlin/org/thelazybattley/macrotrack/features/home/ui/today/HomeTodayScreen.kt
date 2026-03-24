package org.thelazybattley.macrotrack.features.home.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.burned
import macrotrack.composeapp.generated.resources.eaten
import macrotrack.composeapp.generated.resources.goal
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.remaining
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.home.HomeTabCallbacks
import org.thelazybattley.macrotrack.features.home.HomeTabViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun HomeTodayScreen(
    modifier: Modifier = Modifier,
    viewState: HomeTabViewState,
    callbacks: HomeTabCallbacks
) {
    Column(modifier = modifier) {
        CaloriesCard()
    }
}

@Composable
private fun CaloriesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 90.dp),
                    color = colors.deepBlue,
                    strokeWidth = 10.dp,
                    trackColor = colors.lightGray,
                    gapSize = 0.dp
                )
                Column {
                    Text(
                        text = "1,340",
                        color = colors.black,
                        style = typography.bold15
                    )
                    Text(
                        text = stringResource(resource = Res.string.remaining),
                        color = colors.mediumGray,
                        style = typography.regular8
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                CalorieCardDetails(
                    modifier = Modifier,
                    title = Res.string.goal,
                    kcal = 2000,
                )
                HorizontalDivider(color = colors.lightGray)
                CalorieCardDetails(
                    modifier = Modifier,
                    title = Res.string.eaten,
                    kcal = 2000,
                )
                HorizontalDivider(color = colors.lightGray)
                CalorieCardDetails(
                    modifier = Modifier,
                    title = Res.string.burned,
                    kcal = 2000,
                    color = colors.green
                )
            }
        }
    }
}

@Composable
private fun CalorieCardDetails(
    modifier: Modifier = Modifier,
    title: StringResource,
    kcal: Int,
    color: Color = colors.black,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(resource = title),
            color = colors.gray,
            style = typography.regular13
        )
        Text(
            text = stringResource(resource = Res.string.kcal, kcal),
            color = color,
            style = typography.bold13
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
private fun PreviewHomeTodayScreen() {
    MacroTrackTheme {
        HomeTodayScreen(
            modifier = Modifier.padding(all = 16.dp),
            viewState = HomeTabViewState(),
            callbacks = HomeTabCallbacks.default()
        )
    }
}
