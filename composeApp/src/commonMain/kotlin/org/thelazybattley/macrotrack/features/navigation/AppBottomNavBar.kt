package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    selectedDestination: MacroTrackMainDestination?,
    onDestinationClicked: (MacroTrackMainDestination) -> Unit
) {
    Column {
        HorizontalDivider(
            thickness = 1.dp,
            color = colors.lightGray
        )
        NavigationBar(
            modifier = modifier,
            containerColor = colors.white
        ) {
            MacroTrackMainDestination.entries.forEachIndexed { index, destination ->
                val isSelected = selectedDestination?.ordinal == index
                val color = if (isSelected) {
                    colors.deepBlue
                } else {
                    colors.mediumGray
                }
                NavigationBarItem(
                    selected = isSelected,
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
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMacroBottomNavBar() {
    MacroBottomNavBar(
        modifier = Modifier.fillMaxWidth(),
        selectedDestination = MacroTrackMainDestination.HOME
    ) {

    }

}
