package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.all
import macrotrack.composeapp.generated.resources.favourites
import macrotrack.composeapp.generated.resources.foods
import macrotrack.composeapp.generated.resources.recipes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealFoodFilterSelection(
    modifier: Modifier = Modifier,
    callbacks: AddMealCallbacks,
    viewState: AddMealViewState
) {
    Row(
        modifier = modifier
            .background(
                color = colors.offWhite,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(all = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FoodFilter.entries.forEach { foodFilter ->
            FoodFilterChip(
                modifier = Modifier.weight(weight = 1f),
                foodFilter = foodFilter,
                isSelected = foodFilter == viewState.selectedFoodFilter,
            ) {
                callbacks.onFoodFilterSelected(foodFilter = foodFilter)
            }
        }
    }
}

@Composable
private fun FoodFilterChip(
    modifier: Modifier = Modifier,
    foodFilter: FoodFilter,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val background = if (isSelected) {
        colors.iceBlue
    } else {
        colors.offWhite
    }
    val textColor = if (isSelected) {
        colors.deepBlue
    } else {
        colors.gray
    }

    Card(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(size = 8.dp)
            )
            .clickable {
                onSelected()
            },
        colors = CardDefaults.cardColors(
            containerColor = background,
            contentColor = textColor
        ),
    ) {
        Text(
            text = stringResource(resource = foodFilter.text),
            style = typography.bold11,
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xffffffff
)
@Composable
private fun PreviewAddMealFoodFilterSelection() {
    MacroTrackTheme {
        AddMealFoodFilterSelection(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            callbacks = AddMealCallbacks.default(),
            viewState = AddMealViewState()
        )
    }
}

enum class FoodFilter(
    val text: StringResource
) {
    ALL(text = Res.string.all),
    RECIPES(text = Res.string.recipes),
    FOODS(text = Res.string.foods),
    FAVOURITES(text = Res.string.favourites)
}
