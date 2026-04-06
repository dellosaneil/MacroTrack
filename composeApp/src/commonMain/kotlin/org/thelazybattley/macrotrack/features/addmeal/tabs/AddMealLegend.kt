package org.thelazybattley.macrotrack.features.addmeal.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.stripe_dominant_macro
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography


@Composable
fun AddMealLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        ),
        border = BorderStroke(width = 1.dp, color = colors.lightGray)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.stripe_dominant_macro),
                style = typography.bold14,
                color = colors.gray
            )
            Row(
                modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DominantMacroItem(modifier = Modifier, macroType = MacroType.PROTEIN)
                DominantMacroItem(modifier = Modifier, macroType = MacroType.CARBS)
                DominantMacroItem(modifier = Modifier, macroType = MacroType.FAT)
            }
        }

    }
}

@Composable
private fun DominantMacroItem(modifier: Modifier = Modifier, macroType: MacroType) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 3.dp))
                .size(size = 10.dp)
                .background(color = macroType.toColor())
        )
        Text(
            text = stringResource(resource = macroType.text()),
            style = typography.regular13,
            color = macroType.toColor()
        )
    }
}
