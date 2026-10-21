package org.thelazybattley.macrotrack.ui.common

import androidx.compose.runtime.Composable
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cm
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.keypad_feet_inches
import macrotrack.composeapp.generated.resources.keypad_in_centimetres
import macrotrack.composeapp.generated.resources.keypad_in_feet_inches
import macrotrack.composeapp.generated.resources.keypad_in_kilograms
import macrotrack.composeapp.generated.resources.keypad_in_pounds
import macrotrack.composeapp.generated.resources.lb
import macrotrack.composeapp.generated.resources.value_with_unit
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.FeetInches
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import kotlin.math.roundToInt

/** A height typed in cm, shown in feet and inches. */
@Composable
fun heightInFeetPreview(cm: String): KeypadPreview? {
    val height = cm.toDoubleOrNull()?.takeIf { it > 0 } ?: return null
    val feetInches = FeetInches.fromCm(cm = height)
    return KeypadPreview(
        value = stringResource(resource = Res.string.keypad_feet_inches, feetInches.feet, feetInches.inches.toCleanString()),
        caption = stringResource(resource = Res.string.keypad_in_feet_inches)
    )
}

/** A height typed in feet and inches, shown in cm. */
@Composable
fun heightInCmPreview(feet: String, inches: String): KeypadPreview? {
    val wholeFeet = feet.toIntOrNull() ?: return null
    val cm = FeetInches(feet = wholeFeet, inches = inches.toDoubleOrNull() ?: 0.0).cm
    return KeypadPreview(
        value = stringResource(resource = Res.string.value_with_unit, cm.roundToInt().toString(), stringResource(resource = Res.string.cm)),
        caption = stringResource(resource = Res.string.keypad_in_centimetres)
    )
}

/** A weight typed in [unitSystem]'s unit, shown in the other one. */
@Composable
fun weightInOtherUnitPreview(weight: String, unitSystem: UnitSystem): KeypadPreview? {
    val typed = weight.toDoubleOrNull()?.takeIf { it > 0 } ?: return null
    val kg = unitSystem.weightToKg(value = typed)
    val isImperial = unitSystem == UnitSystem.IMPERIAL
    val other = if (isImperial) kg else UnitSystem.IMPERIAL.weightFromKg(kg = kg)
    return KeypadPreview(
        value = stringResource(
            resource = Res.string.value_with_unit,
            ((other * 10).roundToInt() / 10.0).toCleanString(),
            stringResource(resource = if (isImperial) Res.string.kg else Res.string.lb)
        ),
        caption = stringResource(resource = if (isImperial) Res.string.keypad_in_kilograms else Res.string.keypad_in_pounds)
    )
}
