package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        MacroType.entries.forEach { macroType ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 8.dp)
                        .background(color = macroType.stripeColor(), shape = RoundedCornerShape(size = 2.dp))
                )
                Text(
                    text = stringResource(resource = macroType.text()),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
fun MacroType.stripeColor(): Color = when (this) {
    MacroType.PROTEIN -> colors.protein
    MacroType.CARBS -> colors.carbs
    MacroType.FAT -> colors.fat
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealLegend() {
    MacroTrackTheme {
        AddMealLegend(modifier = Modifier.padding(all = 16.dp))
    }
}
