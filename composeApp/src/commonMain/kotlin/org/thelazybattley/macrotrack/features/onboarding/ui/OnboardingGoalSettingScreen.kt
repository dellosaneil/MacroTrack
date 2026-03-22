package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.age
import macrotrack.composeapp.generated.resources.calorie_deficit
import macrotrack.composeapp.generated.resources.calorie_surplus
import macrotrack.composeapp.generated.resources.cm
import macrotrack.composeapp.generated.resources.current_weight
import macrotrack.composeapp.generated.resources.gain_weight
import macrotrack.composeapp.generated.resources.height
import macrotrack.composeapp.generated.resources.ic_chevron_down
import macrotrack.composeapp.generated.resources.ic_gain_weight
import macrotrack.composeapp.generated.resources.ic_lose_weight
import macrotrack.composeapp.generated.resources.ic_maintain_weight
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.lose_weight
import macrotrack.composeapp.generated.resources.maintain_weight
import macrotrack.composeapp.generated.resources.sex
import macrotrack.composeapp.generated.resources.stay_balanced
import macrotrack.composeapp.generated.resources.your_main_goal
import macrotrack.composeapp.generated.resources.your_stats
import macrotrack.composeapp.generated.resources.yrs
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun OnboardingGoalAndStatsScreen(
    modifier: Modifier = Modifier,
    viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Text(
            text = stringResource(resource = Res.string.your_main_goal),
            color = colors.charcoalGray,
            style = typography.bold12
        )
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Adaptive(minSize = 148.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            item {
                GoalChoices(
                    titleRes = Res.string.lose_weight,
                    isSelected = viewState.selectedGoal == Goal.LOSE_WEIGHT,
                    descriptionRes = Res.string.calorie_deficit,
                    icon = Res.drawable.ic_lose_weight
                ) {
                    callbacks.onGoalSelected(goal = Goal.LOSE_WEIGHT)
                }
            }
            item {
                GoalChoices(
                    titleRes = Res.string.maintain_weight,
                    isSelected = viewState.selectedGoal == Goal.MAINTAIN_WEIGHT,
                    descriptionRes = Res.string.stay_balanced,
                    icon = Res.drawable.ic_maintain_weight
                ) {
                    callbacks.onGoalSelected(goal = Goal.MAINTAIN_WEIGHT)
                }
            }
            item {
                GoalChoices(
                    titleRes = Res.string.gain_weight,
                    isSelected = viewState.selectedGoal == Goal.GAIN_WEIGHT,
                    descriptionRes = Res.string.calorie_surplus,
                    icon = Res.drawable.ic_gain_weight
                ) {
                    callbacks.onGoalSelected(goal = Goal.GAIN_WEIGHT)
                }
            }
        }
        Spacer(modifier = Modifier)
        Text(
            text = stringResource(Res.string.your_stats),
            color = colors.charcoalGray,
            style = typography.bold12
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 148.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(
                        text = stringResource(resource = Res.string.age),
                        color = colors.gray,
                        style = typography.regular11
                    )
                    OnboardingStatsTextField(
                        suffixText = Res.string.yrs,
                        maxLength = 3,
                        keyboardType = KeyboardType.Number
                    ) { age ->
                        callbacks.onAgeUpdated(age = age.toIntOrNull() ?: 0)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(
                        text = stringResource(resource = Res.string.sex),
                        color = colors.gray,
                        style = typography.regular11
                    )
                    OnboardingSelectSex(
                        callbacks = callbacks,
                        selectedGender = viewState.selectedGender
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(
                        text = stringResource(resource = Res.string.height),
                        color = colors.gray,
                        style = typography.regular11
                    )
                    OnboardingStatsTextField(
                        suffixText = Res.string.cm,
                        maxLength = 6,
                        keyboardType = KeyboardType.Decimal
                    ) { height ->
                        callbacks.onHeightUpdated(height = height.toDoubleOrNull() ?: 0.0)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(
                        text = stringResource(resource = Res.string.current_weight),
                        color = colors.gray,
                        style = typography.regular11
                    )
                    OnboardingStatsTextField(
                        suffixText = Res.string.kg,
                        maxLength = 6,
                        keyboardType = KeyboardType.Decimal
                    ) { weight ->
                        callbacks.onWeightUpdated(weight = weight.toDoubleOrNull() ?: 0.0)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingSelectSex(
    modifier: Modifier = Modifier,
    callbacks: OnboardingCallbacks,
    selectedGender: UserGender?,
) {
    var isExpanded by remember { mutableStateOf(value = false) }
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = !isExpanded
        }
    ) {
        Box(
            modifier = Modifier.height(height = 48.dp)
                .fillMaxWidth()
                .background(
                    color = colors.offWhite,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .border(
                    width = 1.dp,
                    color = colors.lightGray,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_down),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(size = 12.dp)
                    .align(alignment = Alignment.CenterEnd)
                    .graphicsLayer {
                        rotationZ = if (isExpanded) 180f else 0f
                    },
                tint = colors.gray,
            )
            Text(
                text = selectedGender?.name ?: "",
                color = colors.black,
                style = typography.bold15,
                modifier = Modifier.align(alignment = Alignment.Center)
            )
        }
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
            },
            containerColor = colors.white,
            modifier = Modifier.clip(shape = RoundedCornerShape(size = 12.dp))
        ) {
            UserGender.entries.forEach { gender ->
                val isSelected = gender == selectedGender
                val textStyle = typography.regular11.copy(
                    color = if (isSelected) colors.blue else colors.black,
                    fontWeight = if (isSelected) typography.bold11.fontWeight else typography.regular11.fontWeight
                )
                val background = if (isSelected) colors.lightBlue else colors.white
                DropdownMenuItem(
                    modifier = Modifier.background(color = background),
                    text = {
                        Text(
                            text = gender.name,
                            style = textStyle,
                        )
                    },
                    onClick = {
                        callbacks.onGenderSelected(gender)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingStatsTextField(
    modifier: Modifier = Modifier,
    suffixText: StringResource,
    maxLength: Int,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography
    var value by remember { mutableStateOf(value = "") }
    TextField(
        value = value,
        onValueChange = { newText ->
            val filtered = buildString {
                if (keyboardType == KeyboardType.Decimal) {
                    var hasDecimal = false
                    newText.forEach { char ->
                        if (char.isDigit()) {
                            append(char)
                        } else if (char == '.' && !hasDecimal) {
                            append(char)
                            hasDecimal = true
                        }
                    }
                } else {
                    newText.forEach { char ->
                        if (char.isDigit()) {
                            append(char)
                        }
                    }
                }
            }
            value = filtered.take(n = maxLength)
            onValueChange(value)
        },
        modifier = modifier.height(height = 48.dp)
            .border(
                width = 1.dp,
                color = MacroTrackTheme.colors.lightGray,
                shape = RoundedCornerShape(size = 12.dp)
            ),
        shape = RoundedCornerShape(size = 12.dp),
        textStyle = typography.bold15.copy(
            color = colors.black
        ),
        suffix = {
            Text(
                text = stringResource(resource = suffixText),
                color = colors.mediumGray,
                style = typography.regular11
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.offWhite,
            unfocusedContainerColor = colors.offWhite,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        placeholder = {
            Text(
                text = "-",
                color = colors.mediumGray,
                style = typography.bold15,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        singleLine = true
    )
}

@Composable
private fun GoalChoices(
    modifier: Modifier = Modifier,
    titleRes: StringResource,
    descriptionRes: StringResource,
    icon: DrawableResource,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography
    val borderColor = if (isSelected) colors.blue else colors.gray
    val backgroundColor = if (isSelected) colors.lightBlue else colors.white
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(size = 14.dp),
        border = BorderStroke(
            color = borderColor,
            width = 1.dp
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.padding(top = 8.dp).size(size = 30.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(resource = titleRes),
                color = colors.black,
                style = typography.bold13
            )
            Text(
                text = stringResource(resource = descriptionRes),
                color = colors.gray,
                style = typography.regular10
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewOnboardingGoalAndStatsScreen() {
    MacroTrackTheme {
        OnboardingGoalAndStatsScreen(
            modifier = Modifier.padding(8.dp),
            viewState = OnboardingViewState(),
            callbacks = OnboardingCallbacks.default()
        )
    }
}
