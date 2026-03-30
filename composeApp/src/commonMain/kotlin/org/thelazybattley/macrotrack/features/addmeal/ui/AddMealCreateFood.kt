package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cant_find_it_add_custom_macros
import macrotrack.composeapp.generated.resources.create_new_food
import macrotrack.composeapp.generated.resources.ic_chevron_left
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealCreateFood(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = colors.paleBlue
        ),
        border = BorderStroke(width = 1.dp, color = colors.skyBlue),
        shape = RoundedCornerShape(size = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 38.dp)
                    .clip(
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .background(color = colors.deepBlue)
            ) {
                Text(
                    text = "+",
                    color = colors.white,
                    style = typography.regular18,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                )
            }
            Column {
                Text(
                    text = stringResource(resource = Res.string.create_new_food),
                    color = colors.deepBlue,
                    style = typography.bold14
                )
                Text(
                    text = stringResource(resource = Res.string.cant_find_it_add_custom_macros),
                    color = colors.deepBlue,
                    style = typography.regular10
                )
            }
            Spacer(modifier = Modifier.weight(weight = 1f))
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_left),
                contentDescription = null,
                tint = colors.deepBlue,
                modifier = Modifier.size(size = 14.dp).rotate(degrees = 180f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealCreateFood() {
    MacroTrackTheme {
        AddMealCreateFood(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp)
        ) {

        }
    }
}
