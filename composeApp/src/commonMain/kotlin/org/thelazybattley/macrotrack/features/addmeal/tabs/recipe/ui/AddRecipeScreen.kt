package org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun AddRecipeScreen(modifier: Modifier = Modifier) {

}

@Preview(showBackground = true)
@Composable
private fun PreviewAddRecipeScreen() {
    MacroTrackTheme {
        AddRecipeScreen(modifier = Modifier.fillMaxWidth().padding(all = 12.dp))
    }
}
