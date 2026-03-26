package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackMainDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun MacroBottomNavBar(
    modifier: Modifier = Modifier,
    onDestinationClicked: (MacroTrackMainDestination) -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = colors.white
    ) {
        MacroTrackMainDestination.entries.forEachIndexed { index, destination ->
            val color = if (true) {
                colors.deepBlue
            } else {
                colors.mediumGray
            }
            NavigationBarItem(
                selected = false,
                onClick = {
                    onDestinationClicked(destination)
                },
                icon = {
                    Icon(
                        painter = painterResource(resource = destination.icon),
                        contentDescription = null,
                        modifier = Modifier.size(size = 24.dp),
                        tint = color
                    )
                },
                label = {
                    Text(
                        text = stringResource(resource = destination.label),
                        color = color,
                        style = typography.regular10
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewMacroBottomNavBar() {
    MacroBottomNavBar(
        modifier = Modifier.fillMaxWidth(),
    ) {

    }

}
