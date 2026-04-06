package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_dinner
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CommonTopBar(
    modifier: Modifier = Modifier
        .padding(paddingValues = AppPadding)
        .fillMaxWidth()
        .statusBarsPadding(),
    stringResource: StringResource,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
    ) {
        CommonBackButton(modifier = Modifier.align(alignment = Alignment.CenterStart)) {
            onClick()
        }
        Text(
            text = stringResource(resource = stringResource),
            style = typography.bold16,
            color = colors.black,
            modifier = Modifier.align(alignment = Alignment.Center)
        )
    }
}

@Preview
@Composable
private fun PreviewCommonTopBar() {
    MacroTrackTheme {
        CommonTopBar(
            modifier = Modifier.fillMaxWidth(),
            stringResource = Res.string.add_dinner
        ) {

        }
    }
}
