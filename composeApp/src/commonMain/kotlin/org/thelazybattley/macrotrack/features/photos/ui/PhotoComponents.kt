package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.foundation.Image
import org.thelazybattley.macrotrack.data.local.tour.TOUR_SAMPLE_PHOTO_PREFIX
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_image
import macrotrack.composeapp.generated.resources.photo_no_weight
import macrotrack.composeapp.generated.resources.photo_pose_back
import macrotrack.composeapp.generated.resources.photo_pose_flex
import macrotrack.composeapp.generated.resources.photo_pose_front
import macrotrack.composeapp.generated.resources.photo_pose_side
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatWeightOneDecimalWithUnit
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.round

/** The dark strip photos carry their date and weight on, readable over any photo. */
internal val PhotoScrim = Color(0x9E0A0B0D)

/** Decoded photos kept for scrolling back; thumbnails are small, full photos are not. */
private object PhotoBitmapCache {
    private const val MAX_THUMBNAILS = 120
    private const val MAX_FULL = 6
    private val thumbnails = LinkedHashMap<String, ImageBitmap>()
    private val full = LinkedHashMap<String, ImageBitmap>()

    fun get(fileName: String, isThumbnail: Boolean): ImageBitmap? = map(isThumbnail = isThumbnail)[fileName]

    fun put(fileName: String, isThumbnail: Boolean, bitmap: ImageBitmap) {
        val map = map(isThumbnail = isThumbnail)
        map.remove(key = fileName)
        map[fileName] = bitmap
        val max = if (isThumbnail) MAX_THUMBNAILS else MAX_FULL
        while (map.size > max) map.remove(key = map.keys.first())
    }

    private fun map(isThumbnail: Boolean) = if (isThumbnail) thumbnails else full
}

/** A kept photo, decoded off the main thread. A plain tile shows while it loads or if it's gone missing. */
@Composable
internal fun PhotoImage(
    modifier: Modifier = Modifier,
    fileName: String,
    isThumbnail: Boolean,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = 1f
) {
    if (fileName.startsWith(prefix = TOUR_SAMPLE_PHOTO_PREFIX)) {
        SamplePhoto(modifier = modifier, fileName = fileName, alpha = alpha)
        return
    }
    val storage = koinInject<PhotoStorage>()
    // produceState keeps its value when the keys change, so a tile handed another photo (after one is added or
    // deleted ahead of it) would go on showing the old one; a fresh state per photo starts from that photo instead.
    val bitmap = key(fileName, isThumbnail) {
        produceState(initialValue = PhotoBitmapCache.get(fileName = fileName, isThumbnail = isThumbnail)) {
            if (value != null) return@produceState
            val decoded = withContext(context = Dispatchers.Default) {
                storage.read(fileName = fileName, isThumbnail = isThumbnail)?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
            }
            decoded?.let { PhotoBitmapCache.put(fileName = fileName, isThumbnail = isThumbnail, bitmap = it) }
            value = decoded
        }.value
    }
    Box(modifier = modifier.background(color = colors.line)) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = contentScale,
                alpha = alpha,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Stands in for the tour's example photos, which have no image file: a figure against a plain wall that slims a
 * little from one photo to the next, so first vs latest and the before & after slider show a change.
 */
@Composable
private fun SamplePhoto(modifier: Modifier, fileName: String, alpha: Float) {
    val progress = (fileName.removePrefix(prefix = TOUR_SAMPLE_PHOTO_PREFIX).toIntOrNull() ?: 0) % SAMPLE_PHOTO_STEPS / (SAMPLE_PHOTO_STEPS - 1f)
    Canvas(modifier = modifier.background(color = SampleWall)) {
        drawRect(brush = Brush.verticalGradient(colors = listOf(SampleWall, SampleWallShade)), alpha = alpha)
        drawRect(color = SampleFloor, topLeft = Offset(x = 0f, y = size.height * 0.86f), alpha = alpha)
        val centerX = size.width / 2
        val unit = minOf(size.width, size.height * 0.62f)
        val headRadius = unit * 0.1f
        val headY = size.height * 0.2f
        drawCircle(color = SampleFigure, radius = headRadius, center = Offset(x = centerX, y = headY), alpha = alpha)
        val shoulderY = headY + headRadius * 1.7f
        val shoulder = unit * 0.34f
        val waist = unit * (0.27f - 0.06f * progress)
        val hipY = size.height * 0.7f
        val torso = Path().apply {
            moveTo(x = centerX - shoulder, y = shoulderY + unit * 0.05f)
            quadraticTo(x1 = centerX - shoulder, y1 = shoulderY, x2 = centerX - shoulder * 0.6f, y2 = shoulderY)
            lineTo(x = centerX + shoulder * 0.6f, y = shoulderY)
            quadraticTo(x1 = centerX + shoulder, y1 = shoulderY, x2 = centerX + shoulder, y2 = shoulderY + unit * 0.05f)
            quadraticTo(x1 = centerX + waist, y1 = size.height * 0.5f, x2 = centerX + waist * 1.05f, y2 = hipY)
            lineTo(x = centerX + waist * 0.95f, y = size.height)
            lineTo(x = centerX - waist * 0.95f, y = size.height)
            lineTo(x = centerX - waist * 1.05f, y = hipY)
            quadraticTo(x1 = centerX - waist, y1 = size.height * 0.5f, x2 = centerX - shoulder, y2 = shoulderY + unit * 0.05f)
            close()
        }
        drawPath(path = torso, color = SampleFigure, alpha = alpha)
        val armWidth = unit * 0.09f
        listOf(-1f, 1f).forEach { side ->
            drawLine(
                color = SampleFigure,
                start = Offset(x = centerX + side * shoulder * 0.92f, y = shoulderY + unit * 0.06f),
                end = Offset(x = centerX + side * (shoulder + unit * 0.06f), y = hipY),
                strokeWidth = armWidth,
                cap = StrokeCap.Round,
                alpha = alpha
            )
        }
    }
}

private const val SAMPLE_PHOTO_STEPS = 5
private val SampleWall = Color(0xFFCFC8BC)
private val SampleWallShade = Color(0xFFB3AA9C)
private val SampleFloor = Color(0xFF9C9284)
private val SampleFigure = Color(0xFF5E5149)

/** A photo tile with its date and weight along the bottom, like the grid, hero and Progress card show. */
@Composable
internal fun PhotoTile(
    modifier: Modifier = Modifier,
    fileName: String,
    date: LocalDate,
    weight: Weight?,
    isOverlayShown: Boolean,
    cornerRadius: Dp = 12.dp,
    isThumbnail: Boolean = true,
    textStyle: TextStyle = typography.bold11,
    topStart: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(modifier = modifier.clip(shape = RoundedCornerShape(size = cornerRadius))) {
        PhotoImage(modifier = Modifier.fillMaxSize(), fileName = fileName, isThumbnail = isThumbnail)
        topStart?.let { Box(modifier = Modifier.align(alignment = Alignment.TopStart).padding(all = 6.dp), content = it) }
        if (isOverlayShown) {
            Row(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(color = PhotoScrim)
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = date.toAbbreviatedMonthDay(), style = textStyle, color = Color.White, maxLines = 1)
                Text(
                    text = photoWeightText(weight = weight),
                    style = textStyle,
                    color = Color.White.copy(alpha = if (weight == null) 0.85f else 1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** A small label in a photo's corner, like Before, After, First or New. */
@Composable
internal fun PhotoTag(text: String, isAccent: Boolean) {
    Box(
        modifier = Modifier
            .height(height = 22.dp)
            .clip(shape = RoundedCornerShape(size = 7.dp))
            .background(color = if (isAccent) colors.accent else PhotoScrim)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold11, color = if (isAccent) colors.onAccent else Color.White)
    }
}

/** A tile for when there's no photo yet. */
@Composable
internal fun EmptyPhotoTile(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(color = colors.line), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_image),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 26.dp)
        )
    }
}

/** "73.8 kg", or "No weight" for a day without a weigh-in. */
@Composable
internal fun photoWeightText(weight: Weight?): String =
    weight?.weight?.formatWeightOneDecimalWithUnit() ?: stringResource(resource = Res.string.photo_no_weight)

/** A change in kg, in the user's unit with its sign and one decimal: "−4.6 kg", "+0.4 lb". */
@Composable
internal fun Double.formatSignedWeightChange(): String {
    val tenths = round(LocalUnitSystem.current.weightFromKg(kg = this) * 10).toInt()
    val sign = when {
        tenths < 0 -> "−"
        tenths > 0 -> "+"
        else -> ""
    }
    return stringResource(resource = Res.string.value_unit, "$sign${abs(tenths) / 10}.${abs(tenths) % 10}", weightUnitLabel())
}

/** "Tue, Oct 20, 2026" */
internal fun LocalDate.toLongPhotoDate(): String = toShortDayMonthDay() + ", " + year

/** "October 2026" */
internal fun LocalDate.toMonthYear(): String = month.displayName() + " " + year

internal fun Month.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

internal val PhotoPose.label: StringResource
    get() = when (this) {
        PhotoPose.FRONT -> Res.string.photo_pose_front
        PhotoPose.SIDE -> Res.string.photo_pose_side
        PhotoPose.BACK -> Res.string.photo_pose_back
        PhotoPose.FLEX -> Res.string.photo_pose_flex
    }

/** Two to four options in a rounded track, the selected one raised; the design's segmented control. */
@Composable
internal fun <T> PhotoSegmented(
    modifier: Modifier = Modifier,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 38.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    style = typography.bold13,
                    color = if (isSelected) colors.ink else colors.muted,
                    maxLines = 1
                )
            }
        }
    }
}
