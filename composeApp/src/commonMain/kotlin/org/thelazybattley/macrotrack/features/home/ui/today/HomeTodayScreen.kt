package org.thelazybattley.macrotrack.features.home.ui.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.burned
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.eaten
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.goal
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.remaining
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.home.HomeTabCallbacks
import org.thelazybattley.macrotrack.features.home.HomeTabViewState
import org.thelazybattley.macrotrack.ui.AppPadding
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun HomeTodayScreen(
    modifier: Modifier = Modifier,
    viewState: HomeTabViewState,
    callbacks: HomeTabCallbacks
) {
    Column(
        modifier = modifier.padding(paddingValues = AppPadding),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        CaloriesCard()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            MacroCardDetails(
                macro = Res.string.protein,
                modifier = Modifier.weight(weight = 1f),
                currentValue = 67,
                goalValue = 130,
                macroColor = colors.deepBlue
            )
            MacroCardDetails(
                macro = Res.string.carbs,
                modifier = Modifier.weight(weight = 1f),
                currentValue = 23,
                goalValue = 60,
                macroColor = colors.green
            )
            MacroCardDetails(
                macro = Res.string.fat,
                modifier = Modifier.weight(weight = 1f),
                currentValue = 34,
                goalValue = 55,
                macroColor = colors.orange
            )
        }
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
            var progress by remember { mutableStateOf(0f) }
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(
                    durationMillis = 1500
                )
            )
            LaunchedEffect(key1 = Unit) {
                progress = 0.5f
            }

            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 90.dp),
                    color = colors.deepBlue,
                    strokeWidth = 10.dp,
                    trackColor = colors.lightGray,
                    gapSize = 0.dp,
                    progress = { animatedProgress }
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
private fun MacroCardDetails(
    modifier: Modifier = Modifier,
    macro: StringResource,
    currentValue: Int,
    goalValue: Int,
    macroColor: Color
) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1500
        )
    )
    LaunchedEffect(key1 = Unit) {
        progress = currentValue.toFloat() / goalValue.toFloat()
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.white
        ),
        border = BorderStroke(width = 1.dp, color = colors.lightGray)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = stringResource(resource = macro),
                color = colors.gray,
                style = typography.regular10
            )
            Text(
                text = "${currentValue}g",
                style = typography.bold16,
                color = macroColor
            )
            Text(
                text = "/ ${goalValue}g",
                style = typography.regular10,
                color = colors.mediumGray
            )
            Spacer(modifier = Modifier.height(height = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.lightGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(color = macroColor, shape = RoundedCornerShape(8.dp))
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
            modifier = Modifier,
            viewState = HomeTabViewState(),
            callbacks = HomeTabCallbacks.default()
        )
    }
}
