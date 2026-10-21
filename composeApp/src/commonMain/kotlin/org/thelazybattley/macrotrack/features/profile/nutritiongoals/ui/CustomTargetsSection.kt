package org.thelazybattley.macrotrack.features.profile.nutritiongoals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.custom_calculated_would_be
import macrotrack.composeapp.generated.resources.custom_calories_tap_to_type
import macrotrack.composeapp.generated.resources.custom_check_off_body
import macrotrack.composeapp.generated.resources.custom_check_ok_body
import macrotrack.composeapp.generated.resources.custom_check_ok_title
import macrotrack.composeapp.generated.resources.custom_check_over_title
import macrotrack.composeapp.generated.resources.custom_check_under_title
import macrotrack.composeapp.generated.resources.custom_macro_targets
import macrotrack.composeapp.generated.resources.custom_macro_tap_to_type
import macrotrack.composeapp.generated.resources.custom_row_grams
import macrotrack.composeapp.generated.resources.custom_row_percent
import macrotrack.composeapp.generated.resources.custom_set_carbs
import macrotrack.composeapp.generated.resources.custom_start_from_split
import macrotrack.composeapp.generated.resources.custom_targets_intro
import macrotrack.composeapp.generated.resources.custom_targets_note
import macrotrack.composeapp.generated.resources.custom_unit_grams
import macrotrack.composeapp.generated.resources.custom_unit_percent
import macrotrack.composeapp.generated.resources.custom_use_sum
import macrotrack.composeapp.generated.resources.daily_calorie_goal
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_info
import macrotrack.composeapp.generated.resources.ic_warning
import macrotrack.composeapp.generated.resources.kcal_unit
import macrotrack.composeapp.generated.resources.macro_mode_calculated
import macrotrack.composeapp.generated.resources.split_balanced
import macrotrack.composeapp.generated.resources.split_grams
import macrotrack.composeapp.generated.resources.split_high_protein
import macrotrack.composeapp.generated.resources.split_keto
import macrotrack.composeapp.generated.resources.split_low_fat
import macrotrack.composeapp.generated.resources.split_percent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.MacroSplit
import org.thelazybattley.macrotrack.domain.model.MacroTargets
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.carbsFor
import org.thelazybattley.macrotrack.domain.model.macroCalories
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonKeypadField
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Targets the user types in. Macros are kept in grams; [isPercent] shows and types them as a share of the calorie
 * goal instead.
 */
@Composable
internal fun CustomTargetsSection(
    custom: MacroGoals,
    calculated: MacroGoals?,
    isPercent: Boolean,
    openField: String?,
    onPercentChanged: (Boolean) -> Unit,
    onOpen: (String) -> Unit,
    onChanged: (MacroGoals) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        Text(
            text = stringResource(resource = Res.string.custom_targets_intro),
            style = typography.regular13,
            color = colors.muted
        )
        CustomCaloriesCard(
            custom = custom,
            calculated = calculated,
            isOpen = openField == CUSTOM_CALORIES,
            onOpen = { onOpen(CUSTOM_CALORIES) }
        )
        SplitPresets(custom = custom, calculated = calculated, onChanged = onChanged)
        CustomMacroRows(
            custom = custom,
            isPercent = isPercent,
            openField = openField,
            onPercentChanged = onPercentChanged,
            onOpen = onOpen
        )
        MacroSumCheck(custom = custom, onChanged = onChanged)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .dashedBorder(color = colors.dashed, width = 1.dp, cornerRadius = 16.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_info),
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.padding(top = 1.dp).size(size = 16.dp)
            )
            Text(
                text = stringResource(resource = Res.string.custom_targets_note),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun CustomCaloriesCard(
    custom: MacroGoals,
    calculated: MacroGoals?,
    isOpen: Boolean,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = Res.string.daily_calorie_goal),
                style = typography.regular11,
                color = colors.muted
            )
            calculated?.let {
                Text(
                    text = stringResource(resource = Res.string.custom_calculated_would_be, it.calories.withThousands()),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        CommonKeypadField(
            modifier = Modifier.widthIn(min = 96.dp).height(height = 48.dp),
            value = custom.calories.toString(),
            onClick = onOpen,
            textAlign = TextAlign.End,
            textStyle = typography.bold24,
            isOpen = isOpen,
            horizontalPadding = 10.dp,
            contentDescription = stringResource(resource = Res.string.custom_calories_tap_to_type)
        )
        Text(text = stringResource(resource = Res.string.kcal_unit), style = typography.regular13, color = colors.muted)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SplitPresets(
    custom: MacroGoals,
    calculated: MacroGoals?,
    onChanged: (MacroGoals) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionTitle(text = stringResource(resource = Res.string.custom_start_from_split))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            MacroSplit.entries.forEach { split ->
                val isSelected = split.matches(goals = custom)
                PresetChip(
                    label = stringResource(resource = split.label),
                    detail = stringResource(resource = Res.string.split_percent, split.protein, split.carbs, split.fat),
                    isSelected = isSelected,
                    onClick = { onChanged(split.goalsFor(calories = custom.calories)) }
                )
            }
            calculated?.let {
                PresetChip(
                    label = stringResource(resource = Res.string.macro_mode_calculated),
                    detail = stringResource(resource = Res.string.split_grams, it.protein, it.carbs, it.fat),
                    isSelected = false,
                    isDashed = true,
                    onClick = { onChanged(it) }
                )
            }
        }
    }
}

private val MacroSplit.label: StringResource
    get() = when (this) {
        MacroSplit.BALANCED -> Res.string.split_balanced
        MacroSplit.HIGH_PROTEIN -> Res.string.split_high_protein
        MacroSplit.KETO -> Res.string.split_keto
        MacroSplit.LOW_FAT -> Res.string.split_low_fat
    }

@Composable
private fun PresetChip(
    label: String,
    detail: String,
    isSelected: Boolean,
    isDashed: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 12.dp)
    Column(
        modifier = Modifier
            .clip(shape = shape)
            .then(
                if (isDashed) {
                    Modifier.dashedBorder(color = colors.dashed, width = 1.dp, cornerRadius = 12.dp)
                } else {
                    Modifier
                        .background(color = if (isSelected) colors.accentSoft else colors.surface)
                        .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                }
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(space = 1.dp)
    ) {
        val color = if (isSelected) colors.accentInk else colors.ink
        Text(text = label, style = typography.bold13, color = color)
        Text(text = detail, style = typography.regular11, color = color.copy(alpha = 0.8f))
    }
}

@Composable
private fun CustomMacroRows(
    custom: MacroGoals,
    isPercent: Boolean,
    openField: String?,
    onPercentChanged: (Boolean) -> Unit,
    onOpen: (String) -> Unit
) {
    val macros = customMacros(custom = custom)
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            SectionTitle(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.custom_macro_targets))
            UnitToggle(isPercent = isPercent, onChanged = onPercentChanged)
        }
        val shape = RoundedCornerShape(size = 18.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
        ) {
            macros.forEachIndexed { index, macro ->
                val name = stringResource(resource = macro.type.text())
                val percent = percentOf(grams = macro.grams, type = macro.type, calories = custom.calories)
                val kcal = (macro.grams * macro.type.calories).withThousands()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 10.dp)
                            .background(color = macro.color, shape = RoundedCornerShape(size = 3.dp))
                    )
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(text = name, style = typography.bold14, color = colors.ink)
                        Text(
                            text = if (isPercent) {
                                stringResource(resource = Res.string.custom_row_grams, macro.grams, kcal)
                            } else {
                                stringResource(resource = Res.string.custom_row_percent, percent, kcal)
                            },
                            style = typography.regular11,
                            color = colors.muted
                        )
                    }
                    CommonKeypadField(
                        modifier = Modifier.widthIn(min = 64.dp).height(height = 40.dp),
                        value = if (isPercent) percent.toString() else macro.grams.toString(),
                        onClick = { onOpen(macro.id) },
                        textAlign = TextAlign.End,
                        textStyle = typography.bold16,
                        isOpen = openField == macro.id,
                        cornerRadius = 10.dp,
                        horizontalPadding = 10.dp,
                        contentDescription = stringResource(resource = Res.string.custom_macro_tap_to_type, name)
                    )
                    Text(
                        text = if (isPercent) "%" else stringResource(resource = Res.string.g),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.width(width = 16.dp)
                    )
                }
                if (index < macros.lastIndex) HorizontalDivider(color = colors.divider)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 10.dp)
                .clip(shape = RoundedCornerShape(size = 5.dp))
                .background(color = colors.divider),
            horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            macros.filter { it.grams > 0 }.forEach { macro ->
                Box(
                    modifier = Modifier
                        .weight(weight = (macro.grams * macro.type.calories).toFloat())
                        .fillMaxHeight()
                        .background(color = macro.color)
                )
            }
        }
    }
}

@Composable
private fun UnitToggle(isPercent: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(size = 11.dp))
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 11.dp))
            .padding(all = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        listOf(false to Res.string.custom_unit_grams, true to Res.string.custom_unit_percent).forEach { (percent, label) ->
            val isSelected = percent == isPercent
            Box(
                modifier = Modifier
                    .height(height = 32.dp)
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onChanged(percent) }
                    .semantics { selected = isSelected }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = label),
                    style = typography.bold12,
                    color = if (isSelected) colors.ink else colors.muted
                )
            }
        }
    }
}

/** Whether the macros add up to the calorie goal, with one-tap ways to make them match when they don't. */
@Composable
private fun MacroSumCheck(custom: MacroGoals, onChanged: (MacroGoals) -> Unit) {
    val sum = custom.macroCalories
    val diff = sum - custom.calories
    val isMatch = abs(diff) <= MacroTargets.MATCH_TOLERANCE_KCAL
    val tint = if (isMatch) colors.accentInk else colors.warmInk
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = if (isMatch) colors.accentSoft else colors.warmSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Icon(
                painter = painterResource(resource = if (isMatch) Res.drawable.ic_check else Res.drawable.ic_warning),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size = 18.dp)
            )
            Text(
                text = when {
                    isMatch -> stringResource(resource = Res.string.custom_check_ok_title, sum.withThousands())
                    diff < 0 -> stringResource(resource = Res.string.custom_check_under_title, sum.withThousands(), (-diff).withThousands())
                    else -> stringResource(resource = Res.string.custom_check_over_title, sum.withThousands(), diff.withThousands())
                },
                style = typography.bold13,
                color = tint,
                modifier = Modifier.weight(weight = 1f)
            )
        }
        Text(
            text = stringResource(
                resource = if (isMatch) Res.string.custom_check_ok_body else Res.string.custom_check_off_body,
                custom.calories.withThousands()
            ),
            style = typography.regular11,
            color = colors.ink
        )
        if (!isMatch) {
            val carbs = carbsFor(calories = custom.calories, protein = custom.protein, fat = custom.fat)
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Box(
                    modifier = Modifier
                        .height(height = 34.dp)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .background(color = colors.surface)
                        .clickable(role = Role.Button) { onChanged(custom.copy(carbs = carbs)) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(resource = Res.string.custom_set_carbs, carbs), style = typography.bold12, color = colors.ink)
                }
                Box(
                    modifier = Modifier
                        .height(height = 34.dp)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .clickable(role = Role.Button) { onChanged(custom.copy(calories = sum)) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resource = Res.string.custom_use_sum, sum.withThousands()),
                        style = typography.bold12.copy(textDecoration = TextDecoration.Underline),
                        color = colors.ink
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(modifier: Modifier = Modifier, text: String) {
    Text(modifier = modifier, text = text, style = typography.bold12, color = colors.muted)
}

internal data class CustomMacro(val id: String, val type: MacroType, val grams: Int, val color: Color)

@Composable
internal fun customMacros(custom: MacroGoals) = listOf(
    CustomMacro(id = CUSTOM_PROTEIN, type = MacroType.PROTEIN, grams = custom.protein, color = colors.protein),
    CustomMacro(id = CUSTOM_CARBS, type = MacroType.CARBS, grams = custom.carbs, color = colors.carbs),
    CustomMacro(id = CUSTOM_FAT, type = MacroType.FAT, grams = custom.fat, color = colors.fat)
)

internal fun percentOf(grams: Int, type: MacroType, calories: Int): Int =
    if (calories > 0) (grams * type.calories * 100.0 / calories).roundToInt() else 0

internal fun gramsOf(percent: Double, type: MacroType, calories: Int): Int =
    (percent / 100 * calories / type.calories).roundToInt()

internal const val CUSTOM_CALORIES = "calories"
internal const val CUSTOM_PROTEIN = "protein"
internal const val CUSTOM_CARBS = "carbs"
internal const val CUSTOM_FAT = "fat"
internal const val BODY_FAT = "bodyFat"
