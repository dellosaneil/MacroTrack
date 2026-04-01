package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.kcal_per_gram
import macrotrack.composeapp.generated.resources.one_hundred
import macrotrack.composeapp.generated.resources.portion_size
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealCustomWeight(
    modifier: Modifier = Modifier,
    food: Food,
    onCloseButtonClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.paleBlue)
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.name,
                    style = typography.bold14,
                    color = colors.black
                )
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = null,
                    tint = colors.deepBlue,
                    modifier = Modifier
                        .size(size = 14.dp)
                        .clickable {
                            onCloseButtonClick()
                        }
                )

            }

            Text(
                text = stringResource(
                    resource = Res.string.kcal_per_gram,
                    food.macros.calories,
                    food.weight
                ),
                style = typography.regular11,
                color = colors.deepBlue
            )
            Text(
                text = stringResource(resource = Res.string.portion_size),
                style = typography.regular10,
                color = colors.gray
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                val textFieldHeight = remember { mutableStateOf(0) }
                val height = with(receiver = LocalDensity.current) {
                    textFieldHeight.value.toDp()
                }
                CommonTextField(
                    modifier = Modifier.weight(weight = 0.85f).onSizeChanged { size ->
                        textFieldHeight.value = size.height
                    },
                    placeholder = Res.string.one_hundred,
                    borderColor = colors.deepBlue, keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                ) {

                }
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp, color = colors.deepBlue,
                            shape = RoundedCornerShape(size = 12.dp)
                        )
                        .weight(weight = 0.15f)
                        .height(height = height)
                        .background(color = colors.white, shape = RoundedCornerShape(size = 12.dp)),
                ) {
                    Text(
                        text = stringResource(resource = Res.string.g),
                        style = typography.bold12,
                        color = colors.deepBlue,
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
            }
        }


    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealCustomWeight() {
    MacroTrackTheme {
        AddMealCustomWeight(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            food = dummyFood
        ) {

        }
    }
}
