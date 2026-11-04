package org.thelazybattley.macrotrack.features.nextday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_scale
import macrotrack.composeapp.generated.resources.next_day_heads_up_estimated
import macrotrack.composeapp.generated.resources.next_day_heads_up_from
import macrotrack.composeapp.generated.resources.next_day_heads_up_hidden
import macrotrack.composeapp.generated.resources.next_day_heads_up_hide
import macrotrack.composeapp.generated.resources.next_day_heads_up_higher
import macrotrack.composeapp.generated.resources.next_day_heads_up_label
import macrotrack.composeapp.generated.resources.next_day_heads_up_lower
import macrotrack.composeapp.generated.resources.next_day_heads_up_water
import macrotrack.composeapp.generated.resources.next_day_heads_up_why
import macrotrack.composeapp.generated.resources.next_day_link_sub
import macrotrack.composeapp.generated.resources.next_day_link_sub_top
import macrotrack.composeapp.generated.resources.next_day_new
import macrotrack.composeapp.generated.resources.next_day_percent
import macrotrack.composeapp.generated.resources.next_day_percent_serving
import macrotrack.composeapp.generated.resources.next_day_title
import macrotrack.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.NextDayFood
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatPortion
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/** A change in kg in the user's unit to one decimal with its sign: "+1.0", "−0.2", or "±0.0" when it barely moved. */
@Composable
internal fun Double.signedTenths(): String {
    val value = LocalUnitSystem.current.weightFromKg(kg = this)
    val tenths = round(abs(value) * 10).toInt()
    val sign = when {
        tenths == 0 -> "±"
        value > 0 -> "+"
        else -> "−"
    }
    return "$sign${tenths / 10}.${tenths % 10}"
}

/** [signedTenths] with the unit: "+1.0 kg". */
@Composable
internal fun Double.signedTenthsWithUnit(): String = signedTenths() + " " + weightUnitLabel()

/** A weight in kg in the user's unit to one decimal, without the unit: "75.2". */
@Composable
internal fun Double.tenths(): String {
    val tenths = round(abs(LocalUnitSystem.current.weightFromKg(kg = this)) * 10).toInt()
    return "${tenths / 10}.${tenths % 10}"
}

/** An amount eaten: "70%" (or "70% of a serving" when [isLong]) for a recipe, "150 g" for a food. */
@Composable
internal fun nextDayAmount(isRecipe: Boolean, unit: String, amount: Double, isLong: Boolean = false): String = if (isRecipe) {
    val percent = amount.roundToInt().toString()
    stringResource(resource = if (isLong) Res.string.next_day_percent_serving else Res.string.next_day_percent, percent)
} else {
    formatPortion(amount = amount, storedUnit = unit)
}

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

/** The rounded box around a screen's icon, as on the other rows under the weight chart. */
@Composable
internal fun NextDayIconBox(tint: androidx.compose.ui.graphics.Color, background: androidx.compose.ui.graphics.Color = colors.surface) {
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
 * The Next-day weight row on Progress and Weight history: what it is, and the food that raises the next morning's
 * weight most when there is one. [isNew] tags it until it's been opened.
 */
@Composable
fun NextDayWeightLink(
    modifier: Modifier = Modifier,
    biggest: NextDayFood?,
    isNew: Boolean,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val change = biggest?.average
    val text = subtitle ?: if (biggest != null && change != null) {
        stringResource(resource = Res.string.next_day_link_sub_top, biggest.name, change.signedTenthsWithUnit())
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

/**
 * What [amount] of [food] at dinner usually does to tomorrow's weigh-in, shown under the amount while adding it to
 * Dinner. [isOff] shows the line that undoes turning it off instead.
 */
@Composable
fun NextDayHeadsUpCard(
    food: NextDayFood,
    amount: Double,
    estimate: Double,
    unit: String,
    isOff: Boolean,
    onWhy: () -> Unit,
    onHide: () -> Unit,
    onUndo: () -> Unit
) {
    HorizontalDivider(thickness = 1.dp, color = colors.divider)
    if (isOff) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(resource = Res.string.next_day_heads_up_hidden, food.name),
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
    val isUp = estimate >= 0
    val amountText = nextDayAmount(isRecipe = food.isRecipe, unit = unit, amount = amount)
    val portion = food.portion(amount = amount)
    val basis = if (portion != null) {
        pluralStringResource(resource = Res.plurals.next_day_heads_up_from, quantity = portion.nights.size, portion.nights.size, amountText)
    } else {
        stringResource(resource = Res.string.next_day_heads_up_estimated, food.counted.size, food.name)
    }
    val value = abs(estimate).tenths() + " " + weightUnitLabel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.dangerSoft)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            NextDayIconBox(tint = colors.danger)
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
            text = stringResource(resource = Res.string.next_day_heads_up_hide, food.name),
            style = typography.bold11,
            color = colors.muted,
            modifier = Modifier
                .heightIn(min = 34.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onHide)
                .padding(horizontal = 10.dp, vertical = 9.dp)
        )
    }
}
