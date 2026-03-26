package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.protein
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun MacrosDetail(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        MacroCard(
            modifier = Modifier.weight(weight = 1f),
            value = "32",
            macro = Res.string.kcal_text,
            textColor = colors.black,
        )
        MacroCard(
            modifier = Modifier.weight(weight = 1f),
            value = "21g",
            macro = Res.string.protein,
            textColor = colors.deepBlue
        )
        MacroCard(
            modifier = Modifier.weight(weight = 1f),
            value = "11g",
            textColor = colors.green,
            macro = Res.string.carbs
        )
        MacroCard(
            modifier = Modifier.weight(weight = 1f),
            value = "1g",
            macro = Res.string.fat,
            textColor = colors.orange
        )
    }
}

@Composable
private fun MacroCard(
    modifier: Modifier = Modifier,
    value: String,
    macro: StringResource,
    textColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colors.offWhite
        ),
        shape = RoundedCornerShape(size = 12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = value,
                style = typography.bold15,
                color = textColor
            )
            Text(
                text = stringResource(resource = macro),
                color = colors.gray,
                style = typography.regular10,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

    }
}
