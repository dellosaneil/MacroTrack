package org.thelazybattley.macrotrack.features.share.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.days_on_target
import macrotrack.composeapp.generated.resources.ic_trend_up
import macrotrack.composeapp.generated.resources.macrotrack
import macrotrack.composeapp.generated.resources.photo_before_after
import macrotrack.composeapp.generated.resources.photo_days
import macrotrack.composeapp.generated.resources.photo_stat_waist
import macrotrack.composeapp.generated.resources.share_avg_calories
import macrotrack.composeapp.generated.resources.share_avg_protein
import macrotrack.composeapp.generated.resources.share_change
import macrotrack.composeapp.generated.resources.share_days
import macrotrack.composeapp.generated.resources.share_down
import macrotrack.composeapp.generated.resources.share_in_days
import macrotrack.composeapp.generated.resources.share_kind_week
import macrotrack.composeapp.generated.resources.share_kind_weight
import macrotrack.composeapp.generated.resources.share_now
import macrotrack.composeapp.generated.resources.share_of_progress
import macrotrack.composeapp.generated.resources.share_per_week
import macrotrack.composeapp.generated.resources.share_photos
import macrotrack.composeapp.generated.resources.share_photos_alt
import macrotrack.composeapp.generated.resources.share_same
import macrotrack.composeapp.generated.resources.share_start
import macrotrack.composeapp.generated.resources.share_up
import macrotrack.composeapp.generated.resources.share_week_alt
import macrotrack.composeapp.generated.resources.share_week_on_target
import macrotrack.composeapp.generated.resources.share_week_range
import macrotrack.composeapp.generated.resources.share_weigh_ins
import macrotrack.composeapp.generated.resources.share_weight_alt
import macrotrack.composeapp.generated.resources.share_weight_range
import macrotrack.composeapp.generated.resources.share_within_goal
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.weight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.photos.PhotoItem
import org.thelazybattley.macrotrack.features.photos.ui.PhotoImage
import org.thelazybattley.macrotrack.features.photos.ui.formatSignedWeightChange
import org.thelazybattley.macrotrack.features.share.PhotosCard
import org.thelazybattley.macrotrack.features.share.ShareCardFormat
import org.thelazybattley.macrotrack.features.share.ShareCardKind
import org.thelazybattley.macrotrack.features.share.ShareCardStyle
import org.thelazybattley.macrotrack.features.share.ShareCardViewState
import org.thelazybattley.macrotrack.features.share.WeekCard
import org.thelazybattley.macrotrack.features.share.WeightCard
import org.thelazybattley.macrotrack.ui.common.SAME_WEIGHT_THRESHOLD_KG
import org.thelazybattley.macrotrack.ui.common.formatWeightOneDecimalWithUnit
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

/** A card's own colors; they don't follow the app's theme, so a shared image looks the same from any phone. */
internal class CardPalette(
    val background: Color,
    val tile: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val soft: Color,
    val warn: Color
)

internal val ShareCardStyle.palette: CardPalette
    get() = when (this) {
        ShareCardStyle.LIGHT -> CardPalette(
            background = Color(color = 0xFFF4F3EE),
            tile = Color(color = 0xFFFFFFFF),
            ink = Color(color = 0xFF15171A),
            muted = Color(color = 0xFF5E6168),
            accent = Color(color = 0xFF0E6E52),
            soft = Color(color = 0xFFDDEFE7),
            warn = Color(color = 0xFFB07A12)
        )
        ShareCardStyle.DARK -> CardPalette(
            background = Color(color = 0xFF111214),
            tile = Color(color = 0xFF1B1D21),
            ink = Color(color = 0xFFEEEDE8),
            muted = Color(color = 0xFFA3A6AD),
            accent = Color(color = 0xFF3FBF8F),
            soft = Color(color = 0xFF15322A),
            warn = Color(color = 0xFFF0B44C)
        )
        ShareCardStyle.GREEN -> CardPalette(
            background = Color(color = 0xFF0E6E52),
            tile = Color(color = 0xFF13805F),
            ink = Color(color = 0xFFFFFFFF),
            muted = Color(color = 0xFFCBE6DA),
            accent = Color(color = 0xFFFFFFFF),
            soft = Color(color = 0xFF2A8A6C),
            warn = Color(color = 0xFFFBD58A)
        )
    }

/** The card's size on screen; the saved image is the same layout at a larger scale. */
internal val ShareCardFormat.frameWidth: Dp get() = if (this == ShareCardFormat.STORY) 252.dp else 320.dp

private val ShareCardFormat.frameHeight: Dp get() = if (this == ShareCardFormat.STORY) 448.dp else 320.dp

internal val CARD_RADIUS = 18.dp

/** What a card says, worked out for the user's units and whether weights are shown. */
private class CardText(
    val kicker: String,
    val title: String,
    val big: String,
    val bigSub: String,
    val stats: List<Pair<String, String>>,
    val footer: String,
    val description: String
)

/**
 * The card for [ShareCardViewState.kind] in its format and color. Text ignores the system font size, since the card
 * is a fixed-size picture.
 */
@Composable
internal fun ShareCard(modifier: Modifier = Modifier, viewState: ShareCardViewState) {
    val palette = viewState.style.palette
    val isStory = viewState.format == ShareCardFormat.STORY
    val text = cardText(viewState = viewState) ?: return
    CompositionLocalProvider(LocalDensity provides Density(density = LocalDensity.current.density, fontScale = 1f)) {
        Column(
            modifier = modifier
                .requiredSize(width = viewState.format.frameWidth, height = viewState.format.frameHeight)
                .clip(shape = RoundedCornerShape(size = CARD_RADIUS))
                .background(color = palette.background)
                .semantics { contentDescription = text.description }
                .padding(paddingValues = if (isStory) PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp) else PaddingValues(all = 16.dp)),
            verticalArrangement = Arrangement.spacedBy(space = if (isStory) 12.dp else 8.dp)
        ) {
            Column {
                Text(text = text.kicker, style = typography.medium11, color = palette.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = text.title, style = typography.bold18.copy(fontWeight = FontWeight.ExtraBold), color = palette.ink, maxLines = 1)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(
                    text = text.big,
                    style = typography.bold36.copy(fontSize = 40.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold),
                    color = palette.accent,
                    maxLines = 1
                )
                Text(
                    text = text.bigSub,
                    style = typography.bold12,
                    color = palette.muted,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Box(modifier = Modifier.fillMaxWidth().weight(weight = 1f)) {
                when (viewState.kind) {
                    ShareCardKind.WEEK -> viewState.week?.let { WeekBars(week = it, palette = palette) }
                    ShareCardKind.WEIGHT -> viewState.weight?.let { WeightLine(card = it, isWeightShown = viewState.isWeightShown, palette = palette) }
                    ShareCardKind.PHOTOS -> viewState.photos?.let { BeforeAfter(card = it, isWeightShown = viewState.isWeightShown) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                text.stats.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .background(color = palette.tile, shape = RoundedCornerShape(size = 10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, style = typography.bold9, color = palette.muted, maxLines = 1)
                        Text(text = value, style = typography.bold14, color = palette.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Box(
                    modifier = Modifier
                        .size(size = 20.dp)
                        .background(color = palette.accent, shape = RoundedCornerShape(size = 6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_trend_up),
                        contentDescription = null,
                        tint = palette.background,
                        modifier = Modifier.size(size = 12.dp)
                    )
                }
                Text(
                    text = stringResource(resource = Res.string.macrotrack),
                    style = typography.bold13.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink
                )
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(text = text.footer, style = typography.regular10, color = palette.muted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun cardText(viewState: ShareCardViewState): CardText? = when (viewState.kind) {
    ShareCardKind.WEEK -> viewState.week?.let { weekText(week = it, isWeightShown = viewState.isWeightShown) }
    ShareCardKind.WEIGHT -> viewState.weight?.let { weightText(card = it, isWeightShown = viewState.isWeightShown) }
    ShareCardKind.PHOTOS -> viewState.photos?.let { photosText(card = it, viewState = viewState) }
}

@Composable
private fun weekText(week: WeekCard, isWeightShown: Boolean): CardText {
    val change = week.weightChangeKg
    val weight = when {
        change == null -> NO_VALUE
        isWeightShown -> change.formatSignedWeightChange()
        change < -SAME_WEIGHT_THRESHOLD_KG -> stringResource(resource = Res.string.share_down)
        change > SAME_WEIGHT_THRESHOLD_KG -> stringResource(resource = Res.string.share_up)
        else -> stringResource(resource = Res.string.share_same)
    }
    val endText = if (week.start.month == week.end.month) week.end.day.toString() else week.end.toAbbreviatedMonthDay()
    return CardText(
        kicker = stringResource(resource = Res.string.share_week_range, week.start.toAbbreviatedMonthDay(), endText),
        title = stringResource(resource = Res.string.days_on_target),
        big = stringResource(resource = Res.string.share_week_on_target, week.daysOnTarget),
        bigSub = stringResource(resource = Res.string.share_within_goal),
        stats = listOf(
            stringResource(resource = Res.string.share_avg_calories) to (week.averageCalories?.withThousands() ?: NO_VALUE),
            stringResource(resource = Res.string.share_avg_protein) to (week.averageProtein?.let { "$it g" } ?: NO_VALUE),
            stringResource(resource = Res.string.weight) to weight
        ),
        footer = stringResource(resource = Res.string.share_kind_week),
        description = stringResource(resource = Res.string.share_week_alt, week.daysOnTarget)
    )
}

@Composable
private fun weightText(card: WeightCard, isWeightShown: Boolean): CardText {
    val inDays = pluralStringResource(resource = Res.plurals.share_in_days, quantity = card.days, card.days)
    return CardText(
        kicker = stringResource(
            resource = Res.string.share_weight_range,
            card.first.date.toAbbreviatedMonthDay(),
            card.last.date.toAbbreviatedMonthDay(),
            card.last.date.year
        ),
        title = stringResource(resource = Res.string.share_kind_weight),
        big = if (isWeightShown) card.changeKg.formatSignedWeightChange() else card.percentChange.signedPercent(),
        bigSub = inDays,
        stats = if (isWeightShown) {
            listOf(
                stringResource(resource = Res.string.share_start) to card.first.weight.formatWeightOneDecimalWithUnit(),
                stringResource(resource = Res.string.share_now) to card.last.weight.formatWeightOneDecimalWithUnit(),
                stringResource(resource = Res.string.share_per_week) to card.perWeekKg.formatSignedWeightChange()
            )
        } else {
            listOf(
                stringResource(resource = Res.string.share_change) to card.percentChange.signedPercent(),
                stringResource(resource = Res.string.share_days) to card.days.toString(),
                stringResource(resource = Res.string.share_weigh_ins) to card.weights.size.toString()
            )
        },
        footer = stringResource(resource = Res.string.share_kind_weight),
        description = stringResource(resource = Res.string.share_weight_alt)
    )
}

@Composable
private fun photosText(card: PhotosCard, viewState: ShareCardViewState): CardText {
    val change = card.weightChangeKg
    val days = pluralStringResource(resource = Res.plurals.photo_days, quantity = card.days, card.days)
    val isChangeShown = viewState.isWeightShown && change != null
    val waist = card.waistChangeCm?.let { cm ->
        val tenths = (viewState.unit.fromCm(cm = cm) * 10).roundToInt()
        val sign = if (tenths < 0) "−" else if (tenths > 0) "+" else ""
        stringResource(resource = Res.string.value_unit, "$sign${abs(tenths) / 10}.${abs(tenths) % 10}", stringResource(resource = viewState.unit.label))
    } ?: NO_VALUE
    return CardText(
        kicker = card.albumName,
        title = stringResource(resource = Res.string.photo_before_after),
        big = if (isChangeShown) change.formatSignedWeightChange() else days,
        bigSub = if (isChangeShown) {
            pluralStringResource(resource = Res.plurals.share_in_days, quantity = card.days, card.days)
        } else {
            stringResource(resource = Res.string.share_of_progress)
        },
        stats = listOf(
            stringResource(resource = Res.string.share_photos) to card.photoCount.toString(),
            stringResource(resource = Res.string.share_days) to card.days.toString(),
            stringResource(resource = Res.string.photo_stat_waist) to waist
        ),
        footer = stringResource(resource = Res.string.photo_before_after),
        description = stringResource(resource = Res.string.share_photos_alt)
    )
}

/** One bar a day, against the tallest day; green when within 10% of that day's goal. */
@Composable
private fun WeekBars(week: WeekCard, palette: CardPalette) {
    val scale = week.bars.maxOfOrNull { maxOf(it.eaten ?: 0, it.goal ?: 0) }?.takeIf { it > 0 } ?: 1
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        week.bars.forEach { bar ->
            Column(
                modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().weight(weight = 1f), contentAlignment = Alignment.BottomCenter) {
                    val eaten = bar.eaten ?: 0
                    if (eaten > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction = (eaten.toFloat() / scale).coerceIn(minimumValue = 0.02f, maximumValue = 1f))
                                .background(
                                    color = if (bar.isOnTarget) palette.accent else palette.warn,
                                    shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                                )
                        )
                    }
                }
                Text(text = bar.start.dayOfWeek.name.take(n = 1), style = typography.bold9, color = palette.muted)
            }
        }
    }
}

/** Every weigh-in as a line over a filled area, with the first and latest day under it. */
@Composable
private fun WeightLine(card: WeightCard, isWeightShown: Boolean, palette: CardPalette) {
    Column(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(weight = 1f)) {
            val low = card.weights.minOf { it.weight }
            val high = card.weights.maxOf { it.weight }
            // A little room above and below, and a flat line sits in the middle.
            val pad = ((high - low) * CHART_PADDING).takeIf { it > 0 } ?: 1.0
            val bottom = low - pad
            val range = high + pad - bottom
            val firstDay = card.first.date.toEpochDays()
            val span = (card.last.date.toEpochDays() - firstDay).coerceAtLeast(minimumValue = 1)
            val points = card.weights.map {
                Offset(
                    x = (it.date.toEpochDays() - firstDay).toFloat() / span * size.width,
                    y = size.height - ((it.weight - bottom) / range).toFloat() * size.height
                )
            }
            val line = Path().apply {
                points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
            }
            val area = Path().apply {
                addPath(path = line)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = area, color = palette.soft)
            drawPath(
                path = line,
                color = palette.accent,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(card.first, card.last).forEach { weight ->
                Text(
                    text = weight.date.toAbbreviatedMonthDay() + if (isWeightShown) " · " + weight.weight.formatWeightOneDecimalWithUnit() else "",
                    style = typography.bold10,
                    color = palette.muted
                )
            }
        }
    }
}

@Composable
private fun BeforeAfter(card: PhotosCard, isWeightShown: Boolean) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        listOf(card.before, card.after).forEach { item ->
            PhotoTile(modifier = Modifier.weight(weight = 1f).fillMaxHeight(), item = item, isWeightShown = isWeightShown)
        }
    }
}

@Composable
private fun PhotoTile(modifier: Modifier, item: PhotoItem, isWeightShown: Boolean) {
    Box(modifier = modifier.clip(shape = RoundedCornerShape(size = 12.dp))) {
        PhotoImage(modifier = Modifier.fillMaxSize(), fileName = item.photo.fileName, isThumbnail = false)
        Row(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .fillMaxWidth()
                .background(color = CAPTION_BACKGROUND)
                .padding(horizontal = 7.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = item.photo.date.toAbbreviatedMonthDay(), style = typography.bold10, color = Color.White)
            val weight = item.weight
            if (isWeightShown && weight != null) {
                Text(text = weight.weight.formatWeightOneDecimalWithUnit(), style = typography.bold10, color = Color.White)
            }
        }
    }
}

/** "−6.1%", "+0.4%" or "0.0%". */
private fun Double.signedPercent(): String {
    val tenths = (this * 10).roundToInt()
    val sign = if (tenths < 0) "−" else if (tenths > 0) "+" else ""
    return "$sign${abs(tenths) / 10}.${abs(tenths) % 10}%"
}

private const val NO_VALUE = "—"
private const val CHART_PADDING = 0.1
private val CAPTION_BACKGROUND = Color(color = 0x9E0A0B0D)
