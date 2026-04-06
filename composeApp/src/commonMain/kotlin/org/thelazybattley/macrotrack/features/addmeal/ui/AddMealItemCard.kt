package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.kcal_per_gram
import macrotrack.composeapp.generated.resources.plus_text
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.buildMacroNutrientText
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealItemCard(
    modifier: Modifier = Modifier,
    food: Food,
    onButtonClicked: (() -> Unit)? = null,
    onMealClicked: (() -> Unit)? = null,
) {
    val color = food.dominantMacro.toColor()
    Box(
        modifier = modifier
            .clickable(enabled = onMealClicked != null) {
                if (onMealClicked != null) {
                    onMealClicked()
                }
            }
            .drawWithContent {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x = 0f, y = 0f),
                    size = Size(width = 4.dp.toPx(), height = size.height),
                    cornerRadius = CornerRadius(x = 2.dp.toPx(), y = 2.dp.toPx())
                )
                drawContent()
            }
    ) {
        Row(
            modifier = Modifier
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(
                    text = food.name,
                    style = typography.bold14,
                    color = colors.black
                )
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    Text(
                        text = buildMacroNutrientText(
                            protein = food.macros.protein.toInt(),
                            carbs = food.macros.carbs.toInt(),
                            fat = food.macros.fat.toInt()
                        ),
                        style = typography.regular11,
                    )
                    Text(
                        text = stringResource(
                            resource = Res.string.kcal_per_gram,
                            food.macros.calories,
                            food.weight.toInt()
                        ),
                        color = colors.mediumGray,
                        style = typography.regular11
                    )
                }
            }
            if (onButtonClicked != null) {
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colors.lightGray,
                            shape = CircleShape
                        )
                        .clip(shape = CircleShape)
                        .size(size = 28.dp)
                        .clickable {
                            onButtonClicked()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resource = Res.string.plus_text),
                        color = colors.deepBlue,
                        style = typography.regular18,
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewAddMealItemCard() {
    MacroTrackTheme {
        AddMealItemCard(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            food = dummyFood,
            onButtonClicked = {

            }
        ) {

        }
    }
}
