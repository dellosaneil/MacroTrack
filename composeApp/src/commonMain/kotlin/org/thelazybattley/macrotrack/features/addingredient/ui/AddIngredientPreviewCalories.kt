package org.thelazybattley.macrotrack.features.addingredient.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.calories_auto_calculated
import macrotrack.composeapp.generated.resources.calories_formula
import macrotrack.composeapp.generated.resources.kcal_text
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddIngredientPreviewCalories(
    modifier: Modifier = Modifier,
    calories: Int,
    fats: Double,
    protein: Double,
    carbs: Double,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(size = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.skyBlue
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.calories_auto_calculated),
                style = typography.regular13,
                color = colors.deepBlue
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = calories.toString(),
                    style = typography.bold36,
                    color = colors.deepBlue,
                )
                Text(
                    text = stringResource(resource = Res.string.kcal_text),
                    style = typography.regular13,
                    color = colors.babyBlue,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                text = stringResource(resource = Res.string.calories_formula, protein, carbs, fats),
                style = typography.regular8,
                color = colors.deepBlue
            )
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewAddIngredientPreviewCalories() {
    MacroTrackTheme {
        AddIngredientPreviewCalories(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            calories = 120,
            fats = 1.0,
            carbs = 2.0,
            protein = 33.0
        )
    }
}
