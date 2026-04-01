package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.value_gram
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.AddMealLoggedFood
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealSummary(
    modifier: Modifier = Modifier,
    loggedFood: AddMealLoggedFood
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.deepBlue
        ),
        border = BorderStroke(width = 1.dp, color = colors.deepBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Column {
                Text(
                    text = pluralStringResource(
                        resource = Res.plurals.items,
                        quantity = loggedFood.loggedMeals.size,
                        loggedFood.loggedMeals.size
                    ),
                    style = typography.bold12,
                    color = colors.white55
                )
                Text(
                    text = stringResource(resource = Res.string.kcal, loggedFood.totalCalories),
                    style = typography.bold24,
                    color = colors.white
                )
            }
            Text(
                text = macroText(macroType = MacroType.PROTEIN, value = loggedFood.totalProtein),
                style = typography.bold12,
                textAlign = TextAlign.Center
            )

            Text(
                text = macroText(macroType = MacroType.CARBS, value = loggedFood.totalCarbs),
                style = typography.bold12,
                textAlign = TextAlign.Center
            )

            Text(
                text = macroText(macroType = MacroType.FAT, value = loggedFood.totalFat),
                style = typography.bold12,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun macroText(macroType: MacroType, value: Int): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = colors.white)) {
            append(text = stringResource(resource = Res.string.value_gram, value))
            append("\n")
        }
        withStyle(style = SpanStyle(color = colors.white55, fontWeight = FontWeight.Normal)) {
            append(text = stringResource(resource = macroType.text()).lowercase())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealSummary() {
    MacroTrackTheme {
        AddMealSummary(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            loggedFood = AddMealLoggedFood(
                name = "chicken",
                id = 123,
                totalCarbs = 12,
                totalFat = 12,
                totalProtein = 12,
                totalCalories = 330,
                loggedMeals = listOf(dummyFood, dummyFood)
            )
        )
    }
}
