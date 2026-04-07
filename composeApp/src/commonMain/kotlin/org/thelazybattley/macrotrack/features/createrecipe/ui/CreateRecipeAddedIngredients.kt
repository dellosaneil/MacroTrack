package org.thelazybattley.macrotrack.features.createrecipe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ingredients_added
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealItemCard
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CreateRecipeAddedIngredients(
    modifier: Modifier = Modifier,
    selectedIngredients: List<Food>
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.ingredients_added),
            style = typography.bold11,
            color = colors.charcoalGray
        )
        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
            items(items = selectedIngredients, key = { it.name }) { food ->
                AddMealItemCard(
                    modifier = Modifier.fillMaxWidth(),
                    food = food,
                )
                HorizontalDivider(
                    thickness = 1.dp, color = colors.lightGray,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreateRecipeAddedIngredients() {
    MacroTrackTheme {
        CreateRecipeAddedIngredients(
            modifier = Modifier.fillMaxWidth()
                .padding(all = 12.dp),
            selectedIngredients = listOf(
                dummyFood,
                dummyFood.copy(name = "123"),
            )
        )
    }
}
