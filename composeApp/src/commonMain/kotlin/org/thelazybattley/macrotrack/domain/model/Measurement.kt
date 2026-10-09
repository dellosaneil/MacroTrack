package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.calves
import macrotrack.composeapp.generated.resources.chest
import macrotrack.composeapp.generated.resources.core
import macrotrack.composeapp.generated.resources.hips
import macrotrack.composeapp.generated.resources.left_bicep
import macrotrack.composeapp.generated.resources.left_thigh
import macrotrack.composeapp.generated.resources.lower_body
import macrotrack.composeapp.generated.resources.neck
import macrotrack.composeapp.generated.resources.right_bicep
import macrotrack.composeapp.generated.resources.right_thigh
import macrotrack.composeapp.generated.resources.shoulders
import macrotrack.composeapp.generated.resources.upper_body
import macrotrack.composeapp.generated.resources.waist
import org.jetbrains.compose.resources.StringResource

/** A single body measurement. Values are always stored in centimetres. */
data class Measurement(
    val date: LocalDate,
    val type: MeasurementType,
    val valueCm: Double,
    val note: String? = null
)

enum class MeasurementGroup(val title: StringResource) {
    UPPER_BODY(title = Res.string.upper_body),
    CORE(title = Res.string.core),
    LOWER_BODY(title = Res.string.lower_body)
}

enum class MeasurementType(val title: StringResource, val group: MeasurementGroup) {
    NECK(title = Res.string.neck, group = MeasurementGroup.UPPER_BODY),
    SHOULDERS(title = Res.string.shoulders, group = MeasurementGroup.UPPER_BODY),
    CHEST(title = Res.string.chest, group = MeasurementGroup.UPPER_BODY),
    LEFT_BICEP(title = Res.string.left_bicep, group = MeasurementGroup.UPPER_BODY),
    RIGHT_BICEP(title = Res.string.right_bicep, group = MeasurementGroup.UPPER_BODY),
    WAIST(title = Res.string.waist, group = MeasurementGroup.CORE),
    HIPS(title = Res.string.hips, group = MeasurementGroup.CORE),
    LEFT_THIGH(title = Res.string.left_thigh, group = MeasurementGroup.LOWER_BODY),
    RIGHT_THIGH(title = Res.string.right_thigh, group = MeasurementGroup.LOWER_BODY),
    CALVES(title = Res.string.calves, group = MeasurementGroup.LOWER_BODY)
}
