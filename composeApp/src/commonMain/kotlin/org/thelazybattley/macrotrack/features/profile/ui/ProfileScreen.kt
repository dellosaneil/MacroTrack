package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {

}

@Preview(showBackground = true, backgroundColor = 0xfffffff)
@Composable
private fun PreviewProfileScreen() {
    MacroTrackTheme {
        ProfileScreen()
    }
}
