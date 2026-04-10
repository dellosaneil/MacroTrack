package org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.one_hundred
import macrotrack.composeapp.generated.resources.percentage_eaten
import macrotrack.composeapp.generated.resources.value_gram
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.ui.common.CommonButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddRecipeInputPercentageEaten(
    modifier: Modifier = Modifier,
    food: Food,
    buttonText: String,
    onCloseButtonClick: () -> Unit,
    onPercentageEatenValue: (Double) -> Unit,
    onAddMealClick: () -> Unit
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
                text = stringResource(resource = Res.string.percentage_eaten),
                style = typography.regular10,
                color = colors.gray
            )
            var textValue by rememberSaveable { mutableStateOf(value = "100") }
            CommonTextField(
                modifier = Modifier.fillMaxWidth(),
                placeholder = Res.string.one_hundred,
                borderColor = colors.deepBlue, keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                textValue = textValue
            ) { value ->
                textValue = value
                val percentage = value.toDoubleOrNull() ?: 0.0
                onPercentageEatenValue(percentage)
            }
        }
        Row(
            modifier = Modifier
                .background(
                    color = colors.white,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .fillMaxWidth(fraction = 0.85f)
                .padding(all = 16.dp)
                .align(alignment = Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = macroDetails(
                    value = food.macros.calories.toString(),
                    label = Res.string.kcal_text,
                    color = colors.black
                )
            )
            Text(
                text = macroDetails(
                    value = stringResource(
                        resource = Res.string.value_gram,
                        food.macros.protein.toInt()
                    ),
                    label = MacroType.PROTEIN.text(),
                    color = MacroType.PROTEIN.toColor()
                )
            )
            Text(
                text = macroDetails(
                    value = stringResource(
                        resource = Res.string.value_gram,
                        food.macros.carbs.toInt()
                    ),
                    label = MacroType.CARBS.text(),
                    color = MacroType.CARBS.toColor()
                )
            )
            Text(
                text = macroDetails(
                    value = stringResource(
                        resource = Res.string.value_gram,
                        food.macros.fat.toInt()
                    ),
                    label = MacroType.FAT.text(),
                    color = MacroType.FAT.toColor()
                ),
                style = typography.regular11,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(height = 16.dp))
        CommonButton(
            modifier = Modifier.padding(horizontal = 12.dp),
            buttonText = buttonText
        ) {
            onAddMealClick()
        }
        Spacer(modifier = Modifier.height(height = 16.dp))
    }
}

@Composable
private fun macroDetails(
    value: String,
    label: StringResource,
    color: Color
) = buildAnnotatedString {
    withStyle(
        style = SpanStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = color,
        )
    ) {
        append(value)
        append("\n")
    }
    withStyle(
        style = SpanStyle(
            color = colors.black,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp
        )
    ) {
        append(text = stringResource(resource = label).lowercase())
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddRecipeInputPercentageEaten() {
    MacroTrackTheme {
        AddRecipeInputPercentageEaten(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            food = dummyFood,
            onCloseButtonClick = {

            },
            onPercentageEatenValue = {

            },
            onAddMealClick = {

            },
            buttonText = "Add to Breakfast"
        )
    }
}
