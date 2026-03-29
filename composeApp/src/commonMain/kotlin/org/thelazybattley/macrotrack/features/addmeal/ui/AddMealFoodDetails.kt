package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.plus
import macrotrack.composeapp.generated.resources.w_p_c_f
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealFoodDetails(
    modifier: Modifier = Modifier,
    food: Food,
    onFoodSelected: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Column {
            Text(
                text = food.name,
                color = colors.black,
                style = typography.bold14,
            )
            food.macros.let { macros ->
                Text(
                    text = stringResource(
                        resource = Res.string.w_p_c_f,
                        food.weight,
                        macros.protein.toInt(),
                        macros.carbs.toInt(),
                        macros.fat.toInt()
                    ),
                    color = colors.gray,
                    style = typography.regular11
                )
            }
        }
        Spacer(modifier = Modifier.weight(weight = 1f))
        Text(
            text = stringResource(resource = Res.string.kcal, food.macros.calories),
            color = colors.black,
            style = typography.bold12
        )
        Box(
            modifier = Modifier
                .border(width = 1.dp, shape = CircleShape, color = colors.lightGray)
                .size(size = 24.dp)
                .clickable {
                    onFoodSelected()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(resource = Res.string.plus),
                style = typography.regular18,
                color = colors.gray,
                modifier = Modifier
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewAddMealFoodDetails() {
    MacroTrackTheme {
        AddMealFoodDetails(
            modifier = Modifier.fillMaxWidth(),
            food = dummyFood
        ) {

        }
    }
}
