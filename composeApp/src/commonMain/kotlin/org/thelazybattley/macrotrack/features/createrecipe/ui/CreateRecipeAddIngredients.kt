package org.thelazybattley.macrotrack.features.createrecipe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_ingredient
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealItemCard
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CreateRecipeAddIngredients(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(resource = Res.string.add_ingredient),
            style = typography.regular10,
            color = colors.mediumGray
        )
        LazyColumn {
            items(10) {
                AddMealItemCard(
                    modifier = Modifier.fillMaxWidth(),
                    food = dummyFood,
                    onButtonClicked = {},
                    onMealClicked = {}
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
private fun Preview() {
    MacroTrackTheme {
        CreateRecipeAddIngredients(modifier = Modifier.fillMaxWidth())
    }
}
