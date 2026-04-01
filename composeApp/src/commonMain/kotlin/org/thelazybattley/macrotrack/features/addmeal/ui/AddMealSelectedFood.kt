package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_checkmark
import macrotrack.composeapp.generated.resources.value_gram_value_kcal
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealSelectedFood(
    modifier: Modifier = Modifier,
    food: Food
) {
    Row(
        modifier = modifier
            .padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier.clip(shape = CircleShape)
                .background(color = colors.freshGreen)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_checkmark),
                contentDescription = null,
                tint = colors.white,
                modifier = Modifier.padding(all = 6.dp)
            )
        }
        Column {
            Text(
                text = food.name,
                style = typography.bold12,
                color = colors.mediumGray
            )
            Text(
                text = stringResource(
                    resource = Res.string.value_gram_value_kcal,
                    food.weight,
                    food.macros.calories
                ),
                style = typography.bold10,
                color = colors.freshGreen
            )
        }
        Spacer(modifier = Modifier.weight(weight = 1f))
        Icon(
            painter = painterResource(resource = Res.drawable.ic_checkmark),
            contentDescription = null,
            tint = colors.freshGreen,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealSelectedFood() {
    MacroTrackTheme {
        AddMealSelectedFood(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            food = dummyFood
        )
    }
}
