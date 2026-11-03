package org.thelazybattley.macrotrack.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.c_value_gram
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.f_value_gram
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.p_value_gram
import macrotrack.composeapp.generated.resources.protein
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun getCurrentDate() = run {
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
}

/**
 * Today, and the new day each time the date changes at midnight. Checks at least once a minute, so a missed wake-up
 * while the device slept, or a time zone change, still lands soon after.
 */
@OptIn(ExperimentalTime::class)
fun currentDateFlow(): Flow<LocalDate> = flow {
    while (true) {
        val today = getCurrentDate()
        emit(value = today)
        val timeZone = TimeZone.currentSystemDefault()
        val untilMidnight = today.plus(period = DatePeriod(days = 1)).atStartOfDayIn(timeZone = timeZone) - Clock.System.now()
        delay(timeMillis = untilMidnight.inWholeMilliseconds.coerceIn(minimumValue = 1L, maximumValue = DATE_CHECK_MILLIS))
    }
}.distinctUntilChanged()

private const val DATE_CHECK_MILLIS = 60_000L

fun Double.to2Decimal() = round(x = this * 100) / 100

/** "Tue" */
fun LocalDate.toShortDayName(): String = dayOfWeek.name.take(n = 3).lowercase().replaceFirstChar { it.uppercase() }

/** "Tue, Oct 6" */
fun LocalDate.toShortDayMonthDay(): String = toShortDayName() + ", " + toAbbreviatedMonthDay()

/** "Wednesday, Oct 7" */
fun LocalDate.toDayMonthDay(): String = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } + ", " + toAbbreviatedMonthDay()

fun LocalDate.toAbbreviatedMonthDay() : String {
    return month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3) + " " + day
}


@Composable
fun MacroType.toColor(): Color {
    return when (this) {
        MacroType.PROTEIN -> colors.deepBlue
        MacroType.CARBS -> colors.green
        MacroType.FAT -> colors.orange
    }
}

@Composable
fun MacroType.text(): StringResource {
    return when (this) {
        MacroType.PROTEIN -> Res.string.protein
        MacroType.CARBS -> Res.string.carbs
        MacroType.FAT -> Res.string.fat
    }
}

@Composable
fun buildMacroNutrientText(
    protein: Int,
    fat: Int,
    carbs: Int
): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MacroType.PROTEIN.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.p_value_gram, protein))

        }
        withStyle(style = SpanStyle(color = colors.black)) {
            append("\t·\t")
        }
        withStyle(
            style = SpanStyle(
                color = MacroType.FAT.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.f_value_gram, fat))

        }
        withStyle(style = SpanStyle(color = colors.black)) {
            append("\t·\t")
        }
        withStyle(
            style = SpanStyle(
                color = MacroType.CARBS.toColor()
            )
        ) {
            append(text = stringResource(resource = Res.string.c_value_gram, carbs))
        }
    }
}

fun Double.toCleanString(): String {
    return if (this == toInt().toDouble()) toInt().toString() else toString()
}
