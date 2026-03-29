package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun FoodLogTotalMacros(
    modifier: Modifier = Modifier,
    viewState: FoodLogViewState
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        ),
        shape = RoundedCornerShape(size = 16.dp)
    ) {

    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewFoodLogTotalMacros() {
    MacroTrackTheme {
        FoodLogTotalMacros(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            viewState = FoodLogViewState()
        )
    }
}
