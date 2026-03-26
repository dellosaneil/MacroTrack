package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.ic_chevron_left
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CommonBackButton(
    modifier: Modifier = Modifier,
    onButtonClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable {
            onButtonClicked()
        }
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_left),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = colors.blue,
        )
        Text(
            text = stringResource(resource = Res.string.back),
            color = colors.deepBlue,
            style = typography.medium13
        )
    }
}

@Preview(backgroundColor = 0xffffffff, showBackground = true)
@Composable
private fun CommonBackButton() {
    MacroTrackTheme {
        CommonBackButton {

        }
    }
}
