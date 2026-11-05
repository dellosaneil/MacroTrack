package org.thelazybattley.macrotrack.features.nextday.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_scale
import macrotrack.composeapp.generated.resources.next_day_heads_up_estimated
import macrotrack.composeapp.generated.resources.next_day_heads_up_footer
import macrotrack.composeapp.generated.resources.next_day_heads_up_hidden
import macrotrack.composeapp.generated.resources.next_day_heads_up_hide
import macrotrack.composeapp.generated.resources.next_day_heads_up_higher
import macrotrack.composeapp.generated.resources.next_day_heads_up_label
import macrotrack.composeapp.generated.resources.next_day_heads_up_lower
import macrotrack.composeapp.generated.resources.next_day_heads_up_usual
import macrotrack.composeapp.generated.resources.next_day_heads_up_water
import macrotrack.composeapp.generated.resources.next_day_heads_up_why
import macrotrack.composeapp.generated.resources.next_day_link_sub
import macrotrack.composeapp.generated.resources.next_day_link_sub_top
import macrotrack.composeapp.generated.resources.next_day_new
import macrotrack.composeapp.generated.resources.next_day_percent
import macrotrack.composeapp.generated.resources.next_day_percent_serving
import macrotrack.composeapp.generated.resources.next_day_row_aria
import macrotrack.composeapp.generated.resources.next_day_row_aria_waiting
import macrotrack.composeapp.generated.resources.next_day_nights
import macrotrack.composeapp.generated.resources.next_day_row_waiting
import macrotrack.composeapp.generated.resources.next_day_title
import macrotrack.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.DinnerFood
import org.thelazybattley.macrotrack.domain.model.DinnerHeadsUp
import org.thelazybattley.macrotrack.domain.model.DinnerItem
import org.thelazybattley.macrotrack.domain.model.DinnerSize
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_MIN_NIGHTS
import org.thelazybattley.macrotrack.domain.model.NextDayDinner
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatPortion
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/** A bar this long, in kg, fills its half of a dinner row. */
private const val BAR_FULL_KG = 1.4

/** A change in kg in the user's unit to two decimals with its sign: "+0.35", "−0.20", "+1", or "±0" when it barely moved. */
@Composable
internal fun Double.signedHundredths(): String {
    val value = LocalUnitSystem.current.weightFromKg(kg = this)
    val hundredths = round(abs(value) * 100).toInt()
    val sign = when {
        hundredths == 0 -> "±"
        value > 0 -> "+"
        else -> "−"
    }
    return sign + hundredths.hundredthsText()
}

/** [signedHundredths] with the unit: "+0.35 kg". */
@Composable
internal fun Double.signedHundredthsWithUnit(): String = signedHundredths() + " " + weightUnitLabel()

/** A weight in kg in the user's unit to two decimals, without the unit: "75.25", or "75" when it's whole. */
@Composable
internal fun Double.hundredths(): String = round(abs(LocalUnitSystem.current.weightFromKg(kg = this)) * 100).toInt().hundredthsText()

/** Hundredths as "0.35", leaving off the decimals when they're zero: "1". */
private fun Int.hundredthsText(): String =
    if (this % 100 == 0) "${this / 100}" else "${this / 100}." + (this % 100).toString().padStart(length = 2, padChar = '0')

/** An amount eaten: "70%" (or "70% of a serving" when [isLong]) for a recipe, "150 g" for a food. */
@Composable
internal fun nextDayAmount(isRecipe: Boolean, unit: String, amount: Double, isLong: Boolean = false): String = if (isRecipe) {
    val percent = amount.roundToInt().toString()
    stringResource(resource = if (isLong) Res.string.next_day_percent_serving else Res.string.next_day_percent, percent)
} else {
    formatPortion(amount = amount, storedUnit = unit)
}

/** The amount of each food, as in "250 g · 70%"; a quick add has no amount, so it's left out. */
@Composable
internal fun foodAmounts(foods: List<DinnerFood>, unitOf: (String, Boolean) -> String): String =
    foods.filterNot { it.isQuickAdd }
        .map { nextDayAmount(isRecipe = it.isRecipe, unit = unitOf(it.name, it.isRecipe), amount = it.amount) }
        .joinToString(separator = " · ")

/** One night's dinner as eaten: "250 g Rice + 8 pcs Chicken wings", the most kcal first. */
@Composable
internal fun nightAmounts(items: List<DinnerItem>, unitOf: (String, Boolean) -> String, separator: String = " + "): String =
    items.sortedByDescending { it.calories }.map { item ->
        if (item.isQuickAdd) {
            item.name
        } else {
            nextDayAmount(isRecipe = item.isRecipe, unit = unitOf(item.name, item.isRecipe), amount = item.amount) + " " + item.name
        }
    }.joinToString(separator = separator)

/** [this] with the first appearance of each of [parts] in bold. */
internal fun String.withBold(vararg parts: String): AnnotatedString = buildAnnotatedString {
    val ranges = parts.mapNotNull { part ->
        if (part.isEmpty()) return@mapNotNull null
        val start = this@withBold.indexOf(string = part)
        if (start < 0) null else start until start + part.length
    }.sortedBy { it.first }
    var at = 0
    ranges.forEach { range ->
        if (range.first < at) return@forEach
        append(this@withBold.substring(startIndex = at, endIndex = range.first))
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(this@withBold.substring(range)) }
        at = range.last + 1
    }
    append(this@withBold.substring(startIndex = at))
}

/** The rounded box around the scale icon, as on the other rows under the weight chart. */
@Composable
internal fun NextDayIconBox(tint: Color, background: Color = colors.surface) {
    Box(
        modifier = Modifier
            .size(size = 36.dp)
            .background(color = background, shape = RoundedCornerShape(size = 10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_scale),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size = 20.dp)
        )
    }
}

/**
 * The Next-day weight row on Progress and Weight history: what it is, and the dinner that raises the next morning's
 * weight most when there is one. [isNew] tags it until it's been opened.
 */
@Composable
fun NextDayWeightLink(
    modifier: Modifier = Modifier,
    biggest: NextDayDinner?,
    isNew: Boolean,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val change = biggest?.average
    val text = subtitle ?: if (biggest != null && change != null) {
        stringResource(resource = Res.string.next_day_link_sub_top, biggest.name, change.signedHundredthsWithUnit())
    } else {
        stringResource(resource = Res.string.next_day_link_sub)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.warmSoft)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        NextDayIconBox(tint = colors.warmInk)
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = stringResource(resource = Res.string.next_day_title), style = typography.bold14, color = colors.ink)
                if (isNew) {
                    Text(
                        text = stringResource(resource = Res.string.next_day_new),
                        style = typography.bold10,
                        color = colors.warmInk,
                        modifier = Modifier
                            .background(color = colors.surface, shape = RoundedCornerShape(size = 6.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
            Text(text = text, style = typography.regular11, color = colors.muted)
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

/** The foods of a dinner as chips joined by "+"; ones matching [highlight] stand out. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FoodChips(foods: List<String>, highlight: String = "") {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        foods.forEachIndexed { index, food ->
            if (index > 0) Text(text = "+", style = typography.bold13, color = colors.faint)
            val isHit = highlight.isNotBlank() && food.contains(other = highlight.trim(), ignoreCase = true)
            Text(
                text = food,
                style = typography.bold14,
                color = if (isHit) colors.accentInk else colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(color = if (isHit) colors.accentSoft else colors.background, shape = RoundedCornerShape(size = 8.dp))
                    .border(width = 1.dp, color = if (isHit) colors.accent else colors.line, shape = RoundedCornerShape(size = 8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * A dinner in a list: its foods, usual amounts and nights (or how close it is to a number), a bar left for lower and
 * right for higher, and the average change.
 */
@Composable
internal fun DinnerRow(
    dinner: NextDayDinner,
    unitOf: (String, Boolean) -> String,
    highlight: String = "",
    onClick: () -> Unit
) {
    val average = dinner.average
    val counted = dinner.counted.size
    val amounts = foodAmounts(foods = dinner.usualFoods, unitOf = unitOf)
    val description = if (average != null) {
        stringResource(resource = Res.string.next_day_row_aria, dinner.name, amounts, average.signedHundredthsWithUnit(), counted)
    } else {
        stringResource(resource = Res.string.next_day_row_aria_waiting, dinner.name, counted, NEXT_DAY_MIN_NIGHTS)
    }
    val upColor = colors.fat
    val downColor = colors.accent
    val lineColor = colors.dashed
    val waitingColor = colors.faint
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            FoodChips(foods = dinner.foods, highlight = highlight)
            if (average != null) {
                Text(
                    text = listOf(amounts, pluralStringResource(resource = Res.plurals.next_day_nights, quantity = counted, counted))
                        .filter { it.isNotEmpty() }
                        .joinToString(separator = " · "),
                    style = typography.regular11,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = stringResource(resource = Res.string.next_day_row_waiting, counted, NEXT_DAY_MIN_NIGHTS),
                    style = typography.bold11,
                    color = colors.warmInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Canvas(modifier = Modifier.width(width = 72.dp).height(height = 22.dp)) {
            val center = size.width / 2
            drawLine(color = lineColor, start = Offset(x = center, y = 0f), end = Offset(x = center, y = size.height), strokeWidth = 1.5.dp.toPx())
            if (average == null) {
                drawLine(
                    color = waitingColor,
                    start = Offset(x = size.width * 0.25f, y = size.height / 2),
                    end = Offset(x = size.width * 0.75f, y = size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx()))
                )
            } else {
                val width = (center * (abs(average) / BAR_FULL_KG).toFloat()).coerceIn(minimumValue = 2.dp.toPx(), maximumValue = center)
                val barHeight = 12.dp.toPx()
                drawRoundRect(
                    color = if (average > 0) upColor else downColor,
                    topLeft = Offset(x = if (average > 0) center else center - width, y = (size.height - barHeight) / 2),
                    size = Size(width = width, height = barHeight),
                    cornerRadius = CornerRadius(x = 4.dp.toPx())
                )
            }
        }
        Text(
            text = average?.signedHundredthsWithUnit() ?: "—",
            style = typography.bold15,
            color = when {
                average == null -> colors.faint
                average >= 0.05 -> colors.danger
                average <= -0.05 -> colors.accentInk
                else -> colors.muted
            },
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.width(width = 64.dp)
        )
    }
}

/**
 * What tonight's dinner so far usually does to tomorrow's weigh-in, under the foods picked for Dinner. Once it's been
 * turned off, it offers Undo instead.
 */
@Composable
fun NextDayHeadsUpCard(
    headsUp: DinnerHeadsUp,
    onWhy: () -> Unit,
    onHide: () -> Unit,
    onUndo: () -> Unit
) {
    val dinner = headsUp.dinner
    if (headsUp.isOff) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                .padding(start = 14.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(resource = Res.string.next_day_heads_up_hidden, dinner.name),
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = stringResource(resource = Res.string.undo),
                style = typography.bold12,
                color = colors.accentInk,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .clickable(role = Role.Button, onClick = onUndo)
                    .padding(horizontal = 10.dp, vertical = 14.dp)
            )
        }
        return
    }
    val estimate = headsUp.estimate
    val isUp = estimate >= 0
    val basis = if (headsUp.isUsualSize) {
        stringResource(resource = Res.string.next_day_heads_up_usual, dinner.name, dinner.size(size = DinnerSize.USUAL)?.nights?.size ?: dinner.counted.size)
    } else {
        stringResource(resource = Res.string.next_day_heads_up_estimated, dinner.counted.size, dinner.name)
    }
    val value = abs(estimate).hundredths() + " " + weightUnitLabel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = if (isUp) colors.dangerSoft else colors.hero)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            NextDayIconBox(tint = if (isUp) colors.danger else colors.accentInk)
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = stringResource(resource = Res.string.next_day_heads_up_label), style = typography.regular11, color = colors.muted)
                val line = stringResource(
                    resource = if (isUp) Res.string.next_day_heads_up_higher else Res.string.next_day_heads_up_lower,
                    value
                )
                Text(
                    text = buildAnnotatedString {
                        val start = line.indexOf(string = value)
                        if (start < 0) {
                            append(line)
                        } else {
                            append(line.substring(startIndex = 0, endIndex = start))
                            withStyle(style = SpanStyle(color = if (isUp) colors.danger else colors.accentInk)) { append(value) }
                            append(line.substring(startIndex = start + value.length))
                        }
                    },
                    style = typography.bold15,
                    color = colors.ink
                )
            }
            Text(
                text = stringResource(resource = Res.string.next_day_heads_up_why),
                style = typography.bold12,
                color = colors.accentInk,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .clickable(role = Role.Button, onClick = onWhy)
                    .padding(horizontal = 6.dp, vertical = 12.dp)
            )
        }
        Text(
            text = basis + " " + stringResource(resource = Res.string.next_day_heads_up_water),
            style = typography.regular11,
            color = colors.ink
        )
        Text(
            text = stringResource(resource = Res.string.next_day_heads_up_hide),
            style = typography.bold11,
            color = colors.muted,
            modifier = Modifier
                .heightIn(min = 36.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onHide)
                .padding(horizontal = 10.dp, vertical = 10.dp)
        )
        Text(
            text = stringResource(resource = Res.string.next_day_heads_up_footer),
            style = typography.regular10,
            color = colors.muted
        )
    }
}
