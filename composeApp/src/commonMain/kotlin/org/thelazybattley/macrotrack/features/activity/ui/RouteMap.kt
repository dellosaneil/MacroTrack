package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.thelazybattley.macrotrack.domain.model.RoutePoint
import org.thelazybattley.macrotrack.domain.model.distanceMeters
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** A route on the map; faint ones stack up darker where they overlap. */
data class MapRoute(val points: List<RoutePoint>, val alpha: Float = 1f)

/** Meters per degree of latitude, near enough everywhere. */
private const val METERS_PER_DEGREE = 111_320.0

/** Even a short route is shown at least this wide, so a few steps don't fill the map. */
private const val MIN_SPAN_METERS = 300.0

/** Grid lines every this many metres stand in for streets, giving a sense of scale. */
private const val GRID_METERS = 100.0

/**
 * Routes drawn on a plain ground with a scale grid, no map tiles: fitted to the routes, then [zoom]ed around
 * their middle. [markers] puts a numbered marker every that many metres along the first route; [live] is
 * where the phone is now. A blank [description] leaves the map out for screen readers, for thumbnails beside text.
 */
@Composable
fun RouteMap(
    modifier: Modifier = Modifier,
    routes: List<MapRoute>,
    description: String,
    zoom: Float = 1f,
    markers: Double? = null,
    showEnds: Boolean = true,
    live: RoutePoint? = null,
    lineWidth: Dp = 5.dp,
    padding: Dp = 28.dp
) {
    val ground = colors.divider
    val grid = colors.line
    val route = colors.accent
    val casing = colors.surface
    val ink = colors.ink
    val you = colors.protein
    val textMeasurer = rememberTextMeasurer()
    val markerPoints = remember(routes, markers) {
        markers?.let { unit -> routes.firstOrNull()?.points?.let { unitMarkers(points = it, unitMeters = unit) } }.orEmpty()
    }
    Box(
        modifier = modifier
            .background(color = ground)
            .clipToBounds()
            .then(if (description.isNotBlank()) Modifier.semantics { contentDescription = description } else Modifier)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val all = routes.flatMap { it.points } + listOfNotNull(live)
            if (all.isEmpty()) {
                drawGrid(color = grid, offset = Offset.Zero, metersPerPixel = 2f)
                return@Canvas
            }
            val projection = Projection.fit(points = all, width = size.width, height = size.height, padding = padding.toPx(), zoom = zoom)
            drawGrid(color = grid, offset = projection.gridOffset, metersPerPixel = projection.metersPerPixel)
            val stroke = lineWidth.toPx()
            routes.forEach { mapRoute ->
                mapRoute.points.groupBy { it.segment }.values.forEach { segment ->
                    val path = projection.path(points = segment)
                    if (mapRoute.alpha >= 1f) {
                        drawPath(path = path, color = casing, style = Stroke(width = stroke * 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    drawPath(
                        path = path,
                        color = route.copy(alpha = mapRoute.alpha),
                        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
            val first = routes.firstOrNull()?.points
            markerPoints.forEachIndexed { index, point ->
                val center = projection.offset(point = point)
                drawCircle(color = casing, radius = 9.dp.toPx(), center = center)
                drawCircle(color = route, radius = 9.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
                val label = textMeasurer.measure(
                    text = (index + 1).toString(),
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = route)
                )
                drawText(textLayoutResult = label, topLeft = center - Offset(x = label.size.width / 2f, y = label.size.height / 2f))
            }
            if (showEnds && first != null && first.size > 1 && live == null) {
                val finish = projection.offset(point = first.last())
                drawCircle(color = casing, radius = 8.dp.toPx(), center = finish)
                drawCircle(color = ink, radius = 8.dp.toPx(), center = finish, style = Stroke(width = 3.dp.toPx()))
            }
            if (showEnds && first != null && first.isNotEmpty()) {
                val start = projection.offset(point = first.first())
                drawCircle(color = casing, radius = 7.dp.toPx(), center = start)
                drawCircle(color = route, radius = 5.dp.toPx(), center = start)
            }
            live?.let { point ->
                val center = projection.offset(point = point)
                drawCircle(color = you.copy(alpha = 0.18f), radius = 20.dp.toPx(), center = center)
                drawCircle(color = Color.White, radius = 9.dp.toPx(), center = center)
                drawCircle(color = you, radius = 6.5.dp.toPx(), center = center)
            }
        }
    }
}

/** Lat/lon to pixels: flat metres around the routes' middle, fitted to the canvas and scaled by zoom. */
private class Projection(
    private val centerLat: Double,
    private val centerLon: Double,
    private val metersPerDegreeLon: Double,
    val metersPerPixel: Float,
    private val canvasCenter: Offset
) {
    fun offset(point: RoutePoint): Offset {
        val x = (point.lon - centerLon) * metersPerDegreeLon / metersPerPixel
        val y = -(point.lat - centerLat) * METERS_PER_DEGREE / metersPerPixel
        return canvasCenter + Offset(x = x.toFloat(), y = y.toFloat())
    }

    fun path(points: List<RoutePoint>): Path = Path().apply {
        points.forEachIndexed { index, point ->
            val offset = offset(point = point)
            if (index == 0) moveTo(x = offset.x, y = offset.y) else lineTo(x = offset.x, y = offset.y)
        }
    }

    /** Where the grid lines fall, so they stay put on the ground as the map zooms. */
    val gridOffset: Offset
        get() {
            val eastMeters = centerLon * metersPerDegreeLon
            val northMeters = centerLat * METERS_PER_DEGREE
            val x = canvasCenter.x - (eastMeters % GRID_METERS / metersPerPixel).toFloat()
            val y = canvasCenter.y + (northMeters % GRID_METERS / metersPerPixel).toFloat()
            return Offset(x = x, y = y)
        }

    companion object {
        fun fit(points: List<RoutePoint>, width: Float, height: Float, padding: Float, zoom: Float): Projection {
            val minLat = points.minOf { it.lat }
            val maxLat = points.maxOf { it.lat }
            val minLon = points.minOf { it.lon }
            val maxLon = points.maxOf { it.lon }
            val centerLat = (minLat + maxLat) / 2
            val metersPerDegreeLon = METERS_PER_DEGREE * cos(x = centerLat * PI / 180)
            val spanX = max((maxLon - minLon) * metersPerDegreeLon, MIN_SPAN_METERS)
            val spanY = max((maxLat - minLat) * METERS_PER_DEGREE, MIN_SPAN_METERS)
            val usableWidth = max(width - 2 * padding, 1f)
            val usableHeight = max(height - 2 * padding, 1f)
            val fitted = max(spanX / usableWidth, spanY / usableHeight)
            return Projection(
                centerLat = centerLat,
                centerLon = (minLon + maxLon) / 2,
                metersPerDegreeLon = metersPerDegreeLon,
                metersPerPixel = (fitted / zoom).toFloat(),
                canvasCenter = Offset(x = width / 2, y = height / 2)
            )
        }
    }
}

private fun DrawScope.drawGrid(color: Color, offset: Offset, metersPerPixel: Float) {
    val step = (GRID_METERS / metersPerPixel).toFloat()
    // Too dense to read when zoomed far out; the plain ground is enough then.
    if (step < 12f) return
    val lineColor = color.copy(alpha = 0.6f)
    var x = offset.x - floor(x = offset.x / step) * step
    while (x < size.width) {
        drawLine(color = lineColor, start = Offset(x = x, y = 0f), end = Offset(x = x, y = size.height), strokeWidth = 1f)
        x += step
    }
    var y = offset.y - floor(x = offset.y / step) * step
    while (y < size.height) {
        drawLine(color = lineColor, start = Offset(x = 0f, y = y), end = Offset(x = size.width, y = y), strokeWidth = 1f)
        y += step
    }
}

/** Where each full [unitMeters] falls along the route, gaps between paused segments left out. */
private fun unitMarkers(points: List<RoutePoint>, unitMeters: Double): List<RoutePoint> {
    val markers = mutableListOf<RoutePoint>()
    var travelled = 0.0
    var next = unitMeters
    points.zipWithNext().forEach { (a, b) ->
        if (a.segment != b.segment) return@forEach
        val step = distanceMeters(from = a, to = b)
        while (step > 0 && travelled + step >= next) {
            val share = (next - travelled) / step
            markers += RoutePoint(lat = a.lat + (b.lat - a.lat) * share, lon = a.lon + (b.lon - a.lon) * share, timeMillis = a.timeMillis)
            next += unitMeters
        }
        travelled += step
    }
    return markers
}

/** The zoom one press of + or − leads to, kept between showing a little more than the route and close up. */
internal fun Float.zoomedBy(step: Float): Float = min(max(this + step, 0.7f), 4f)
