package org.thelazybattley.macrotrack.features.addmeal.tabs.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
import macrotrack.composeapp.generated.resources.add_to_value
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.kcal_per_gram
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.one_hundred
import macrotrack.composeapp.generated.resources.portion_size
import macrotrack.composeapp.generated.resources.value_gram
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.ui.common.CommonButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddFoodCustomizeWeight(
    modifier: Modifier = Modifier,
    food: Food,
    calories: Int,
    originalWeight: Double,
    mealType: MealType,
    onCloseButtonClick: () -> Unit,
    onPortionSizeUpdated: (Double) -> Unit,
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
                text = stringResource(
                    resource = Res.string.kcal_per_gram,
                    calories,
                    originalWeight
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
                    modifier = Modifier
                        .weight(weight = 0.85f)
                        .onSizeChanged { size ->
                            textFieldHeight.value = size.height
                        },
                    placeholder = Res.string.one_hundred,
                    borderColor = colors.deepBlue, keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    initialValue = food.weight.toInt().toString()
                ) { value ->
                    onPortionSizeUpdated(value.toDoubleOrNull() ?: 0.0)
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
            buttonText = stringResource(
                resource = Res.string.add_to_value,
                stringResource(resource = mealType.title)
            )
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
private fun PreviewAddFoodCustomizeWeight() {
    MacroTrackTheme {
        AddFoodCustomizeWeight(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            food = dummyFood,
            onCloseButtonClick = {

            },
            onPortionSizeUpdated = {

            },
            calories = 93,
            onAddMealClick = {

            },
            originalWeight = 100.0,
            mealType = MealType.BREAKFAST
        )
    }
}
