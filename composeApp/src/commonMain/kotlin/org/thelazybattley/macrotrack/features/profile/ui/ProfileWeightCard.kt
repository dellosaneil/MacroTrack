package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_log
import macrotrack.composeapp.generated.resources.last_value
import macrotrack.composeapp.generated.resources.log_today_weight
import macrotrack.composeapp.generated.resources.log_weight
import macrotrack.composeapp.generated.resources.quick_adjust_from_last
import macrotrack.composeapp.generated.resources.same
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonBottomSheet
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileWeightCard(
    modifier: Modifier = Modifier,
    weightInput: String,
    previousWeight: String,
    onWeightChanged: (String) -> Unit
) {
    val showBottomSheet = remember { mutableStateOf(value = false) }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.white
        ),
        border = BorderStroke(width = 1.dp, color = colors.lightGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.log_today_weight),
                    style = typography.bold13,
                    color = colors.black
                )
                Text(
                    text = stringResource(
                        resource = Res.string.last_value,
                        previousWeight,
                        "Mar 16"
                    ),
                    style = typography.regular11,
                    color = colors.mediumGray
                )
            }
            Button(
                onClick = {
                    showBottomSheet.value = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.deepBlue,
                    contentColor = colors.white
                ),
                shape = RoundedCornerShape(size = 8.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.add_log),
                    style = typography.bold13
                )
            }
        }
    }

    val bottomSheetState = rememberModalBottomSheetState()
    CommonBottomSheet(
        bottomSheetState = bottomSheetState,
        showBottomSheet = showBottomSheet
    ) {
        WeightBottomSheetContent(
            modifier = Modifier
                .padding(paddingValues = AppPadding)
                .fillMaxWidth(),
            previousWeight = previousWeight,
            weightInput = weightInput,
            onWeightChanged = onWeightChanged
        )
    }
}


@Composable
private fun WeightBottomSheetContent(
    modifier: Modifier = Modifier,
    previousWeight: String,
    weightInput: String,
    onWeightChanged: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.log_weight),
            style = typography.bold18,
            color = colors.black
        )
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = previousWeight,
            textValue = weightInput,
            onValueChanged = { weight ->
                onWeightChanged(weight)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = typography.bold32
        )
        Text(
            text = stringResource(
                resource = Res.string.quick_adjust_from_last,
                previousWeight
            ),
            style = typography.regular10,
            color = colors.mediumGray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            val previousWeightDouble = previousWeight.toDoubleOrNull() ?: 0.0
            val weightInputDouble = weightInput.toDoubleOrNull() ?: 0.0
            QuickAdjustChip(
                modifier = Modifier.weight(weight = 1f),
                previousWeight = previousWeightDouble,
                adjustment = -0.5,
                inputtedWeight = weightInputDouble,
                onClick = { weight ->
                    onWeightChanged(weight.toString())
                }
            )
            QuickAdjustChip(
                modifier = Modifier.weight(weight = 1f),
                previousWeight = previousWeightDouble,
                adjustment = -0.25,
                inputtedWeight = weightInputDouble,
                onClick = { weight ->
                    onWeightChanged(weight.toString())
                }
            )
            QuickAdjustChip(
                modifier = Modifier.weight(weight = 1f),
                previousWeight = previousWeightDouble,
                adjustment = 0.0,
                inputtedWeight = weightInputDouble,
                onClick = { weight ->
                    onWeightChanged(weight.toString())
                }
            )
            QuickAdjustChip(
                modifier = Modifier.weight(weight = 1f),
                previousWeight = previousWeightDouble,
                adjustment = 0.25,
                inputtedWeight = weightInputDouble,
                onClick = { weight ->
                    onWeightChanged(weight.toString())
                }
            )
            QuickAdjustChip(
                modifier = Modifier.weight(weight = 1f),
                previousWeight = previousWeightDouble,
                adjustment = 0.5,
                inputtedWeight = weightInputDouble,
                onClick = { weight ->
                    onWeightChanged(weight.toString())
                }
            )
        }
    }
}

@Composable
private fun QuickAdjustChip(
    modifier: Modifier = Modifier,
    adjustment: Double,
    previousWeight: Double,
    inputtedWeight: Double,
    onClick: (Double) -> Unit
) {
    val adjustedWeight = previousWeight + adjustment
    val isSelected = inputtedWeight == adjustedWeight
    val containerBackground = if (isSelected) {
        colors.paleBlue
    } else {
        colors.iceMist
    }
    val borderColor = if (isSelected) {
        colors.deepBlue
    } else {
        colors.lightGray
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerBackground,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        onClick = {
            onClick(adjustedWeight)
        }
    ) {
        val labelTextColor = when {
            isSelected -> colors.deepBlue
            adjustment >= 0 -> colors.green
            else -> colors.crimsonRed
        }
        val adjustedWeightTextColor = if (isSelected) {
            colors.deepBlue
        } else {
            colors.mediumGray
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            val text = if (adjustment == 0.0) {
                stringResource(resource = Res.string.same)
            } else {
                adjustment.toString()
            }

            Text(
                text = text,
                style = typography.bold11,
                color = labelTextColor
            )
            Text(
                text = adjustedWeight.toString(),
                style = typography.regular10,
                color = adjustedWeightTextColor
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewWeightBottomSheetContent() {
    WeightBottomSheetContent(
        modifier = Modifier.fillMaxWidth(),
        previousWeight = "78.4",
        weightInput = "78.4",
        onWeightChanged = {

        }
    )
}

@Preview
@Composable
private fun PreviewProfileWeightCard() {
    MacroTrackTheme {
        ProfileWeightCard(
            modifier = Modifier.fillMaxWidth(),
            weightInput = "68.4",
            previousWeight = "68.4",
            onWeightChanged = {

            }

        )
    }
}
