package org.thelazybattley.macrotrack.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.height_feet_inches
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.lb
import macrotrack.composeapp.generated.resources.unit_cm
import macrotrack.composeapp.generated.resources.unit_in
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.FeetInches
import org.thelazybattley.macrotrack.domain.model.PortionUnit
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.Weight
import kotlin.math.abs
import kotlin.math.round

/** The user's unit system, provided once at the root of the app. Values in state stay in kg and cm. */
val LocalUnitSystem = compositionLocalOf { UnitSystem.METRIC }

/** Weight changes smaller than this, in kg, read as "no change". */
const val SAME_WEIGHT_THRESHOLD_KG = 0.005

@Composable
fun weightUnitLabel(): String =
    stringResource(resource = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) Res.string.lb else Res.string.kg)

@Composable
fun lengthUnitLabel(): String =
    stringResource(resource = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) Res.string.unit_in else Res.string.unit_cm)

/** A weight in kg shown in the user's unit with two decimals, like "74.20" or "−1.50". */
@Composable
@ReadOnlyComposable
fun Double.formatWeight(): String = LocalUnitSystem.current.weightFromKg(kg = this).formatTwoDecimals()

/** [formatWeight] followed by the unit: "74.20 kg" or "163.58 lb". */
@Composable
fun Double.formatWeightWithUnit(): String =
    stringResource(resource = Res.string.value_unit, formatWeight(), weightUnitLabel())

/** A weight change in kg with its sign, like "+0.40" or "−1.20", and "0.00" when it barely moved. */
@Composable
@ReadOnlyComposable
fun Double.toSignedWeight(): String = when {
    this > SAME_WEIGHT_THRESHOLD_KG -> "+" + formatWeight()
    this < -SAME_WEIGHT_THRESHOLD_KG -> formatWeight()
    else -> "0.00"
}

/** How amounts of a food stored in [storedUnit] are shown in the user's units (grams as oz when imperial). */
@Composable
@ReadOnlyComposable
fun portionUnit(storedUnit: String): PortionUnit = LocalUnitSystem.current.portionUnit(storedUnit = storedUnit)

/** A stored food amount with its shown unit, like "150 g" or "5.3 oz". */
@Composable
fun formatPortion(amount: Double, storedUnit: String): String {
    val unit = portionUnit(storedUnit = storedUnit)
    return stringResource(resource = Res.string.value_unit, unit.format(amount = amount), unit.label)
}

/** A height in cm as "175 cm" or "5 ft 9 in". */
@Composable
fun Double.formatHeightWithUnit(): String = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) {
    val height = FeetInches.fromCm(cm = this)
    stringResource(resource = Res.string.height_feet_inches, height.feet, height.inches.toCleanString())
} else {
    stringResource(resource = Res.string.value_unit, (round(this * 10) / 10).toCleanString(), stringResource(resource = Res.string.unit_cm))
}

/** Weigh-ins in kg converted to the user's unit, for charts that only draw the numbers they're given. */
@Composable
fun List<Weight>.inWeightUnit(): List<Weight> {
    val unitSystem = LocalUnitSystem.current
    return remember(this, unitSystem) {
        if (unitSystem == UnitSystem.METRIC) this else map { it.copy(weight = unitSystem.weightFromKg(kg = it.weight)) }
    }
}

/** A weight in kg as the user would type it: their unit, to one decimal, without trailing ".0". */
fun UnitSystem.weightInputText(kg: Double): String = (round(weightFromKg(kg = kg) * 10) / 10).toCleanString()

internal fun Double.formatTwoDecimals(): String {
    val hundredths = round(abs(this) * 100).toInt()
    val sign = if (this < 0 && hundredths != 0) "−" else ""
    return "$sign${hundredths / 100}.${(hundredths % 100).toString().padStart(length = 2, padChar = '0')}"
}
