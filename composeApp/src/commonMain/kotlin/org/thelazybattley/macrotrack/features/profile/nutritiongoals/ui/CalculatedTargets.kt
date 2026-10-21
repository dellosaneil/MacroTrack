package org.thelazybattley.macrotrack.features.profile.nutritiongoals.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.body_fat
import macrotrack.composeapp.generated.resources.body_fat_tap_to_type
import macrotrack.composeapp.generated.resources.ic_warning
import macrotrack.composeapp.generated.resources.macro_basis_add_body_fat
import macrotrack.composeapp.generated.resources.macro_basis_healthy
import macrotrack.composeapp.generated.resources.macro_basis_lean
import macrotrack.composeapp.generated.resources.macro_basis_lean_value
import macrotrack.composeapp.generated.resources.macro_basis_note_healthy
import macrotrack.composeapp.generated.resources.macro_basis_note_lean
import macrotrack.composeapp.generated.resources.macro_basis_note_target
import macrotrack.composeapp.generated.resources.macro_basis_target
import macrotrack.composeapp.generated.resources.macro_basis_title
import macrotrack.composeapp.generated.resources.macro_rule_per_weight
import macrotrack.composeapp.generated.resources.macro_mode_calculated
import macrotrack.composeapp.generated.resources.macro_mode_calculated_sub
import macrotrack.composeapp.generated.resources.macro_mode_custom
import macrotrack.composeapp.generated.resources.macro_mode_custom_sub
import macrotrack.composeapp.generated.resources.macro_mode_label
import macrotrack.composeapp.generated.resources.macro_warning_below_resting_body
import macrotrack.composeapp.generated.resources.macro_warning_below_resting_title
import macrotrack.composeapp.generated.resources.macro_warning_carbs_zero_body
import macrotrack.composeapp.generated.resources.macro_warning_carbs_zero_title
import macrotrack.composeapp.generated.resources.macro_warning_low_carb_body
import macrotrack.composeapp.generated.resources.macro_warning_low_carb_title
import macrotrack.composeapp.generated.resources.macro_warning_set_custom
import macrotrack.composeapp.generated.resources.macro_warning_use_pace
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.GoalPace
import org.thelazybattley.macrotrack.domain.model.MacroBasis
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.profile.nutritiongoals.NutritionGoalsViewState
import org.thelazybattley.macrotrack.ui.common.CommonKeypadField
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatWeightOneDecimalWithUnit
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.round

/** Calculated from the user's stats and pace, or typed in. */
@Composable
internal fun TargetModeToggle(isCustom: Boolean, onSelected: (Boolean) -> Unit) {
    val label = stringResource(resource = Res.string.macro_mode_label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .semantics { contentDescription = label }
            .padding(all = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        listOf(
            Triple(false, Res.string.macro_mode_calculated, Res.string.macro_mode_calculated_sub),
            Triple(true, Res.string.macro_mode_custom, Res.string.macro_mode_custom_sub)
        ).forEach { (custom, title, subtitle) ->
            val isSelected = custom == isCustom
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .clip(shape = RoundedCornerShape(size = 11.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelected(custom) }
                    .semantics { selected = isSelected }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(resource = title), style = typography.bold14, color = colors.ink)
                Text(text = stringResource(resource = subtitle), style = typography.regular11, color = colors.muted)
            }
        }
    }
}

/**
 * Warns when a loss pace leaves little or nothing for carbs, or drops the target under the resting burn. The carb
 * warnings offer the fastest pace that keeps enough carbs, and custom targets.
 */
@Composable
internal fun MacroWarnings(
    viewState: NutritionGoalsViewState,
    onPaceChanged: (Double) -> Unit,
    onCustom: () -> Unit
) {
    val macroGoals = viewState.macroGoals ?: return
    val unitSystem = LocalUnitSystem.current
    val slowerPace = viewState.fastestPaceKeepingCarbs
        ?.let { GoalPace.stepAtOrBelow(kgPerMonth = it, unitSystem = unitSystem) }
        ?.takeIf { it < (viewState.goalPace ?: 0.0) }
    val slowerLabel = slowerPace?.let {
        stringResource(
            resource = Res.string.macro_warning_use_pace,
            GoalPace.shown(kgPerMonth = it, unitSystem = unitSystem).toString(),
            weightUnitLabel()
        )
    }
    val proteinAndFat = macroGoals.protein * MacroType.PROTEIN.calories + macroGoals.fat * MacroType.FAT.calories
    if (viewState.isCarbsAtZero) {
        WarningCard(
            isSevere = true,
            title = stringResource(resource = Res.string.macro_warning_carbs_zero_title),
            body = stringResource(
                resource = Res.string.macro_warning_carbs_zero_body,
                proteinAndFat.withThousands(),
                (proteinAndFat - macroGoals.calories).withThousands()
            ),
            slowerLabel = slowerLabel,
            onSlower = { slowerPace?.let(onPaceChanged) },
            onCustom = onCustom
        )
    } else if (viewState.isLowCarb) {
        WarningCard(
            isSevere = false,
            title = stringResource(resource = Res.string.macro_warning_low_carb_title),
            body = stringResource(resource = Res.string.macro_warning_low_carb_body, macroGoals.carbs),
            slowerLabel = slowerLabel,
            onSlower = { slowerPace?.let(onPaceChanged) },
            onCustom = onCustom
        )
    }
    if (viewState.isBelowRestingBurn) {
        WarningCard(
            isSevere = false,
            title = stringResource(resource = Res.string.macro_warning_below_resting_title),
            body = stringResource(
                resource = Res.string.macro_warning_below_resting_body,
                macroGoals.calories.withThousands(),
                (viewState.restingBurn ?: 0).withThousands()
            )
        )
    }
}

@Composable
private fun WarningCard(
    isSevere: Boolean,
    title: String,
    body: String,
    slowerLabel: String? = null,
    onSlower: () -> Unit = {},
    onCustom: (() -> Unit)? = null
) {
    val tint = if (isSevere) colors.danger else colors.warmInk
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = if (isSevere) colors.dangerSoft else colors.warmSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_warning),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.padding(top = 1.dp).size(size = 18.dp)
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(text = title, style = typography.bold13, color = tint)
            Text(text = body, style = typography.regular11, color = colors.ink)
            if (onCustom != null) {
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    slowerLabel?.let {
                        WarningAction(text = it, isFilled = true, onClick = onSlower)
                    }
                    WarningAction(text = stringResource(resource = Res.string.macro_warning_set_custom), isFilled = false, onClick = onCustom)
                }
            }
        }
    }
}

@Composable
private fun WarningAction(text: String, isFilled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(height = 34.dp)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = if (isFilled) colors.surface else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (isFilled) typography.bold12 else typography.bold12.copy(textDecoration = TextDecoration.Underline),
            color = colors.ink
        )
    }
}

/** Target (or healthy) weight against lean mass, with the body fat that lean mass needs. */
@Composable
internal fun MacroBasisSection(
    viewState: NutritionGoalsViewState,
    isBodyFatOpen: Boolean,
    onSelected: (MacroBasis) -> Unit,
    onOpenBodyFat: () -> Unit
) {
    val userDetails = viewState.userDetails ?: return
    val bodyFat = viewState.bodyFat
    val isLean = viewState.macroBasis == MacroBasis.LEAN_MASS && viewState.leanBasisWeight != null
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = Res.string.macro_basis_title), style = typography.bold12, color = colors.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            BasisChoice(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(
                    resource = if (userDetails.targetWeight != null) Res.string.macro_basis_target else Res.string.macro_basis_healthy
                ),
                value = viewState.targetBasisWeight?.formatWeightOneDecimalWithUnit().orEmpty(),
                isSelected = !isLean,
                onClick = { onSelected(MacroBasis.TARGET_WEIGHT) }
            )
            BasisChoice(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.macro_basis_lean),
                value = viewState.leanBasisWeight?.let {
                    stringResource(resource = Res.string.macro_basis_lean_value, it.formatWeightOneDecimalWithUnit(), bodyFat!!.toCleanString())
                } ?: stringResource(resource = Res.string.macro_basis_add_body_fat),
                isSelected = isLean,
                onClick = { if (bodyFat == null) onOpenBodyFat() else onSelected(MacroBasis.LEAN_MASS) }
            )
        }
        Text(
            text = when {
                isLean -> stringResource(resource = Res.string.macro_basis_note_lean)
                userDetails.targetWeight != null -> stringResource(
                    resource = Res.string.macro_basis_note_target,
                    userDetails.targetWeight.formatWeightOneDecimalWithUnit(),
                    userDetails.weight.formatWeightOneDecimalWithUnit()
                )
                else -> stringResource(resource = Res.string.macro_basis_note_healthy)
            },
            style = typography.regular11,
            color = colors.muted
        )
        if (isLean) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.background)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.body_fat),
                    style = typography.medium13,
                    color = colors.ink,
                    modifier = Modifier.weight(weight = 1f)
                )
                CommonKeypadField(
                    modifier = Modifier.widthIn(min = 64.dp).height(height = 40.dp),
                    value = bodyFat?.toCleanString().orEmpty(),
                    onClick = onOpenBodyFat,
                    textAlign = TextAlign.End,
                    textStyle = typography.bold16,
                    isOpen = isBodyFatOpen,
                    cornerRadius = 10.dp,
                    horizontalPadding = 10.dp,
                    contentDescription = stringResource(resource = Res.string.body_fat_tap_to_type)
                )
                Text(text = "%", style = typography.regular13, color = colors.muted, modifier = Modifier.width(width = 14.dp))
            }
        }
    }
}

@Composable
private fun BasisChoice(
    modifier: Modifier,
    label: String,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 12.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = typography.bold13, color = colors.ink)
        Text(text = value, style = typography.regular11, color = colors.muted)
    }
}

/** How protein or fat is worked out, like "2.0 g × 69.6 kg" or "0.91 g × 153.4 lb". */
@Composable
internal fun perWeightRule(gramsPerKg: Double, basisWeight: Double): String {
    val unitSystem = LocalUnitSystem.current
    // Grams per kg is grams per lb times kg per lb.
    val perUnit = gramsPerKg * unitSystem.weightToKg(value = 1.0)
    val decimals = if (unitSystem == UnitSystem.IMPERIAL) 2 else 1
    return stringResource(
        resource = Res.string.macro_rule_per_weight,
        perUnit.toFixed(decimals = decimals),
        basisWeight.formatWeightOneDecimalWithUnit()
    )
}

private fun Double.toFixed(decimals: Int): String {
    val factor = if (decimals == 2) 100 else 10
    val scaled = round(this * factor).toInt()
    return "${scaled / factor}." + (scaled % factor).toString().padStart(length = decimals, padChar = '0')
}
