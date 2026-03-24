package org.thelazybattley.macrotrack.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.month
import macrotrack.composeapp.generated.resources.today
import macrotrack.composeapp.generated.resources.week
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.home.HomeTabCallbacks
import org.thelazybattley.macrotrack.features.home.HomeTabViewModel
import org.thelazybattley.macrotrack.features.home.HomeTabViewState
import org.thelazybattley.macrotrack.features.home.ui.today.HomeTodayScreen
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun HomeTabScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<HomeTabViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    HomeTabScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel
    )
}

@Composable
fun HomeTabScreen(
    modifier: Modifier = Modifier,
    viewState: HomeTabViewState,
    callbacks: HomeTabCallbacks
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        var selectedTimePeriod by remember { mutableStateOf(value = TimePeriod.TODAY) }
        PrimaryTabRow(
            modifier = Modifier.clip(shape = RoundedCornerShape(size = 14.dp)),
            selectedTabIndex = selectedTimePeriod.ordinal,
            containerColor = colors.offWhite,
            contentColor = colors.black,
            indicator = {

            },
            divider = {

            }
        ) {
            HomeTabChoice(
                text = Res.string.today,
                isSelected = selectedTimePeriod == TimePeriod.TODAY
            ) {
                selectedTimePeriod = TimePeriod.TODAY
            }
            HomeTabChoice(
                text = Res.string.week,
                isSelected = selectedTimePeriod == TimePeriod.WEEK
            ) {
                selectedTimePeriod = TimePeriod.WEEK
            }
            HomeTabChoice(
                text = Res.string.month,
                isSelected = selectedTimePeriod == TimePeriod.MONTH
            ) {
                selectedTimePeriod = TimePeriod.MONTH
            }
        }

        HomeTodayScreen(
            viewState = viewState,
            callbacks = callbacks
        )
    }

}

@Composable
private fun HomeTabChoice(
    modifier: Modifier = Modifier,
    text: StringResource,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textStyle = if (isSelected) {
        typography.bold13.copy(
            color = colors.black
        )
    } else {
        typography.regular13.copy(
            color = colors.mediumGray
        )
    }
    Tab(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
    ) {
        Text(
            text = stringResource(resource = text),
            style = textStyle,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewHomeTabScreen() {
    MacroTrackTheme {
        HomeTabScreen(
            viewState = HomeTabViewState(),
            callbacks = HomeTabCallbacks.default(),
        )
    }
}

private enum class TimePeriod {
    TODAY, WEEK, MONTH
}
