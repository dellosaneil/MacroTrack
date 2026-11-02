package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatTwoDecimals
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

/** "2,650" */
internal fun Int.withThousands(): String {
    val digits = abs(this).toString()
    val grouped = digits.reversed().chunked(size = 3).joinToString(separator = ",").reversed()
    return if (this < 0) "−$grouped" else grouped
}

/** "Dec 19", with the year when it isn't [today]'s: "Feb 3, 2027". */
internal fun LocalDate.forecastDate(today: LocalDate): String =
    toAbbreviatedMonthDay() + if (year != today.year) ", $year" else ""

/** A weight change a week in kg, signed by [direction] and shown in the user's unit: "−0.42". */
@Composable
@ReadOnlyComposable
internal fun Double.signedWeekly(direction: Int): String {
    val shown = LocalUnitSystem.current.weightFromKg(kg = abs(this)).formatTwoDecimals()
    return (if (direction < 0) "−" else "+") + shown
}

/** A weight in kg in the user's unit to one decimal: "69.6". */
@Composable
@ReadOnlyComposable
internal fun Double.oneDecimal(): String {
    val tenths = (LocalUnitSystem.current.weightFromKg(kg = abs(this)) * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

@Composable
internal fun Double.oneDecimalWithUnit(): String = stringResource(resource = Res.string.value_unit, oneDecimal(), weightUnitLabel())

@Composable
internal fun ForecastScaffold(
    title: StringResource,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    overlay: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = { CommonScreenHeader(title = title, onBack = onBack, trailing = trailing) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp),
            content = content
        )
    }
    overlay()
}

/** A tinted card leading with a small capitalised [kicker]. */
@Composable
internal fun ForecastHeroCard(
    modifier: Modifier = Modifier,
    kicker: String,
    background: Color,
    kickerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = background)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Text(text = kicker, style = typography.bold11, color = kickerColor)
        content()
    }
}

@Composable
internal fun ForecastCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
        content = content
    )
}

internal data class ForecastStat(val label: String, val value: String)

@Composable
internal fun ForecastStatTiles(stats: List<ForecastStat>) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        stats.forEach { stat ->
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(text = stat.label, style = typography.medium11, color = colors.muted, maxLines = 1)
                Text(text = stat.value, style = typography.bold18, color = colors.ink, maxLines = 1)
            }
        }
    }
}

/** A row in a [ForecastLinksCard]; [onClick] null shows it without a chevron. */
internal data class ForecastLink(
    val icon: DrawableResource,
    val title: String,
    val subtitle: String,
    val tint: Color,
    val background: Color,
    val onClick: (() -> Unit)?
)

@Composable
internal fun ForecastLinksCard(links: List<ForecastLink>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
    ) {
        links.forEachIndexed { index, link ->
            if (index > 0) HorizontalDivider(color = colors.divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp)
                    .then(if (link.onClick != null) Modifier.clickable(role = Role.Button, onClick = link.onClick) else Modifier)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                ForecastIconBox(icon = link.icon, tint = link.tint, background = link.background)
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = link.title, style = typography.bold14, color = colors.ink)
                    Text(text = link.subtitle, style = typography.regular11, color = colors.muted)
                }
                if (link.onClick != null) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = colors.faint,
                        modifier = Modifier.size(size = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ForecastIconBox(icon: DrawableResource, tint: Color, background: Color) {
    Box(
        modifier = Modifier
            .size(size = 36.dp)
            .background(color = background, shape = RoundedCornerShape(size = 11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = tint, modifier = Modifier.size(size = 18.dp))
    }
}

@Composable
internal fun ForecastSectionLabel(text: String) {
    Text(text = text, style = typography.bold12, color = colors.muted)
}

@Composable
internal fun ForecastNote(text: String, background: Color = colors.surface, color: Color = colors.muted) {
    Text(
        text = text,
        style = typography.regular13,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = background)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

@Composable
internal fun ForecastFootnote(text: String) {
    Text(text = text, style = typography.regular11, color = colors.muted)
}

@Composable
internal fun ForecastSecondaryButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(height = 54.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold15, color = colors.ink)
    }
}

/** A dashed-outline button for adding or picking something extra. */
@Composable
internal fun ForecastDashedButton(text: String, icon: DrawableResource, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 14.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 17.dp))
        Text(text = text, style = typography.bold14, color = colors.accentInk)
    }
}

@Composable
internal fun ForecastSwitchRow(label: String, subtitle: String, isOn: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = label, style = typography.bold14, color = colors.ink)
            if (subtitle.isNotEmpty()) Text(text = subtitle, style = typography.regular11, color = colors.muted)
        }
        CommonSwitch(isOn = isOn, description = label, onToggle = onToggle)
    }
}

/** A radio dot: a thick accent ring when [isSelected]. */
@Composable
internal fun ForecastRadioDot(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(size = 20.dp)
            .border(width = if (isSelected) 6.dp else 2.dp, color = if (isSelected) colors.accent else colors.dashed, shape = CircleShape)
    )
}

/** Start to goal, with a dot where today's trend weight sits at the end of the fill. */
@Composable
internal fun ForecastProgressBar(progress: Float, start: String, now: String, goal: String) {
    val fraction = progress.coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(height = 18.dp), contentAlignment = Alignment.CenterStart) {
            val dotSize = 18.dp
            val trackWidth = maxWidth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 10.dp)
                    .clip(shape = CircleShape)
                    .background(color = colors.heroTrack)
            ) {
                Box(
                    modifier = Modifier
                        .width(width = trackWidth * fraction)
                        .fillMaxHeight()
                        .background(color = colors.accent, shape = CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .offset(x = (trackWidth * fraction - dotSize / 2).coerceIn(0.dp, trackWidth - dotSize))
                    .size(size = dotSize)
                    .background(color = colors.surface, shape = CircleShape)
                    .border(width = 4.dp, color = colors.accent, shape = CircleShape)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = start, style = typography.medium11, color = colors.muted)
            Text(text = now, style = typography.bold11, color = colors.accentInk)
            Text(text = goal, style = typography.medium11, color = colors.muted)
        }
    }
}

/** A ring filling [fraction] of the way, with [value] over [label] in the middle. */
@Composable
internal fun ForecastRing(fraction: Float, value: String, label: String) {
    val track = colors.surface
    val fill = colors.nightInk
    Box(modifier = Modifier.size(size = 104.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(size = 92.dp)) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 9.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(color = fill, startAngle = -90f, sweepAngle = 360f * fraction.coerceIn(0f, 1f), useCenter = false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = typography.bold18, color = colors.ink)
            Text(text = label, style = typography.bold9, color = colors.muted, modifier = Modifier.offset(y = (-2).dp))
        }
    }
}
