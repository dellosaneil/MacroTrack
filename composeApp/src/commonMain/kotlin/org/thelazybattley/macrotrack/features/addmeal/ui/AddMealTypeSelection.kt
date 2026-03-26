package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography



@Composable
fun AddMealTypeSelection(
    modifier: Modifier = Modifier,
    selectedMealType: MealType,
    callbacks: AddMealCallbacks
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        MealType.entries.forEach { mealType ->
            MealTypeCard(
                modifier = Modifier.weight(weight = 1f),
                mealType = mealType,
                isSelected = selectedMealType == mealType,
            ) {
                callbacks.onMealTypeSelected(mealType = mealType)
            }
        }
    }
}

@Composable
private fun MealTypeCard(
    modifier: Modifier = Modifier,
    mealType: MealType,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    val containerColor = if (isSelected) {
        colors.deepBlue
    } else {
        colors.offWhite
    }
    val textColor = if (isSelected) {
        colors.white
    } else {
        colors.gray
    }


    Card(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(size = 8.dp)
            ).clickable {
                onSelected()
            },
        shape = RoundedCornerShape(size = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(resource = mealType.title),
                style = typography.bold12,
                color = textColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
