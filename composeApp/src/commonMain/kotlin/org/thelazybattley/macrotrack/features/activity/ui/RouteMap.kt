package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.thelazybattley.macrotrack.domain.model.RoutePoint
import org.thelazybattley.macrotrack.domain.model.continuesTo
import org.thelazybattley.macrotrack.domain.model.distanceMeters
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max

/** A route on the map; faint ones stack up darker where they overlap. */
data class MapRoute(val points: List<RoutePoint>, val alpha: Float = 1f)

/** Free OpenStreetMap vector tiles, no key needed; a dark style to match the dark theme. */
private const val LIGHT_STYLE = "https://tiles.openfreemap.org/styles/positron"
private const val DARK_STYLE = "https://tiles.openfreemap.org/styles/dark"

/** The tiles' terms ask for this on every map. */
private const val ATTRIBUTION = "© OpenStreetMap"

/** A font the styles above serve, for the marker numbers. */
private const val LABEL_FONT = "Noto Sans Bold"

/** Meters per degree of latitude, near enough everywhere. */
private const val METERS_PER_DEGREE = 111_320.0

/** Even a short route is shown at least this wide, so a few steps don't zoom right into one street. */
private const val MIN_SPAN_METERS = 300.0

/** How far in + and − go; past this the tiles have nothing more to show. */
private const val MAX_ZOOM = 19f

/** How far one press of + or − zooms: a little under half or double the scale. */
private const val ZOOM_STEP = 0.8

private const val EMPTY_FEATURES = """{"type":"FeatureCollection","features":[]}"""

/**
 * Where a [RouteMap] is looking. It fits the routes when they first show, and keeps following them as a live
 * route grows, until someone drags the map; [fit] goes back to the whole route and starts following again.
 */
@Stable
class RouteMapState internal constructor(internal val camera: CameraState) {

    internal var fitRequests by mutableIntStateOf(value = 0)
        private set

    fun fit() {
        fitRequests++
    }

    suspend fun zoomIn() = zoomBy(step = ZOOM_STEP)

    suspend fun zoomOut() = zoomBy(step = -ZOOM_STEP)

    private suspend fun zoomBy(step: Double) {
        val position = camera.position
        camera.animateTo(finalPosition = position.copy(zoom = (position.zoom + step).coerceIn(minimumValue = 1.0, maximumValue = MAX_ZOOM.toDouble())))
    }
}

@Composable
fun rememberRouteMapState(): RouteMapState {
    val camera = rememberCameraState()
    return remember(camera) { RouteMapState(camera = camera) }
}

/**
 * Routes on a street map. [markers] puts a numbered marker every that many metres along the first route; [live]
 * is where the phone is now. With [interactive] off the map can't be moved, for small maps on a scrolling page.
 * [padding] keeps the routes clear of the edges and anything drawn over the map.
 */
@Composable
fun RouteMap(
    modifier: Modifier = Modifier,
    routes: List<MapRoute>,
    description: String,
    state: RouteMapState = rememberRouteMapState(),
    markers: Double? = null,
    showEnds: Boolean = true,
    live: RoutePoint? = null,
    interactive: Boolean = true,
    lineWidth: Dp = 5.dp,
    padding: PaddingValues = PaddingValues(all = 28.dp),
    attributionAlignment: Alignment = Alignment.BottomEnd,
    attributionPadding: PaddingValues = PaddingValues(all = 8.dp)
) {
    val isDark = colors.background.luminance() < 0.5f
    val route = colors.accent
    val casing = colors.surface
    val ink = colors.ink
    val you = colors.protein
    val riding = colors.muted
    val first = routes.firstOrNull()?.points.orEmpty()

    val solidRoutes = remember(routes) { routesJson(routes = routes.filter { it.alpha >= 1f }, isRiding = false) }
    val faintRoutes = remember(routes) { routesJson(routes = routes.filter { it.alpha < 1f }, isRiding = false) }
    val ridingRoutes = remember(routes) { routesJson(routes = routes.filter { it.alpha >= 1f }, isRiding = true) }
    val markerPoints = remember(first, markers) { markers?.let { unitMarkers(points = first, unitMeters = it) }.orEmpty() }
    val markersJson = remember(markerPoints) {
        pointsJson(points = markerPoints.mapIndexed { index, point -> point to "${index + 1}" })
    }
    val endsJson = remember(first, showEnds, live) {
        when {
            !showEnds || first.isEmpty() -> EMPTY_FEATURES
            // The live dot marks the end of a route still being recorded.
            live != null || first.size == 1 -> pointsJson(points = listOf(first.first() to "start"))
            else -> pointsJson(points = listOf(first.first() to "start", first.last() to "finish"))
        }
    }
    val liveJson = remember(live) { live?.let { pointsJson(points = listOf(it to "you")) } ?: EMPTY_FEATURES }

    val bounds = remember(routes, live) { boundsOf(points = routes.flatMap { it.points } + listOfNotNull(live)) }
    var isFollowing by remember(state) { mutableStateOf(value = true) }
    LaunchedEffect(state) {
        snapshotFlow { state.camera.moveReason }.collect { reason ->
            if (reason == CameraMoveReason.GESTURE) isFollowing = false
        }
    }
    LaunchedEffect(state, state.fitRequests) {
        isFollowing = true
    }
    LaunchedEffect(state, bounds, isFollowing, state.fitRequests) {
        if (!isFollowing || bounds == null) return@LaunchedEffect
        state.camera.animateTo(boundingBox = bounds, padding = padding)
    }

    Box(
        modifier = modifier
            .background(color = colors.divider)
            .then(if (description.isNotBlank()) Modifier.semantics { contentDescription = description } else Modifier)
    ) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri(uri = if (isDark) DARK_STYLE else LIGHT_STYLE),
            cameraState = state.camera,
            zoomRange = 1f..MAX_ZOOM,
            options = MapOptions(
                gestureOptions = if (interactive) GestureOptions.RotationLocked else GestureOptions.AllDisabled,
                ornamentOptions = OrnamentOptions.AllDisabled
            )
        ) {
            val faint = rememberGeoJsonSource(data = GeoJsonData.JsonString(json = faintRoutes))
            val solid = rememberGeoJsonSource(data = GeoJsonData.JsonString(json = solidRoutes))
            val rode = rememberGeoJsonSource(data = GeoJsonData.JsonString(json = ridingRoutes))
            val ends = rememberGeoJsonSource(data = GeoJsonData.JsonString(json = endsJson))
            val numbered = rememberGeoJsonSource(data = GeoJsonData.JsonString(json = markersJson))
            val here = rememberGeoJsonSource(data = GeoJsonData.JsonString(json = liveJson))
            LineLayer(
                id = "route-faint",
                source = faint,
                color = const(route),
                opacity = feature["alpha"].asNumber(),
                width = const(lineWidth),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round)
            )
            // Stretches spent in a vehicle: dashed and grey, so they read as not part of the walk or run.
            LineLayer(
                id = "route-riding",
                source = rode,
                color = const(riding),
                width = const(lineWidth * 0.8f),
                dasharray = const(listOf(1.2, 1.6)),
                join = const(LineJoin.Round)
            )
            LineLayer(
                id = "route-casing",
                source = solid,
                color = const(casing),
                width = const(lineWidth * 1.8f),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round)
            )
            LineLayer(
                id = "route",
                source = solid,
                color = const(route),
                width = const(lineWidth),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round)
            )
            CircleLayer(
                id = "route-markers",
                source = numbered,
                color = const(casing),
                radius = const(9.dp),
                strokeColor = const(route),
                strokeWidth = const(2.dp)
            )
            SymbolLayer(
                id = "route-marker-numbers",
                source = numbered,
                textField = format(span(value = feature["label"].asString())),
                textFont = const(listOf(LABEL_FONT)),
                textSize = const(10.sp),
                textColor = const(route),
                textAllowOverlap = const(true),
                textIgnorePlacement = const(true)
            )
            CircleLayer(
                id = "route-finish",
                source = ends,
                filter = feature["label"].asString() eq const("finish"),
                color = const(casing),
                radius = const(7.dp),
                strokeColor = const(ink),
                strokeWidth = const(3.dp)
            )
            CircleLayer(
                id = "route-start",
                source = ends,
                filter = feature["label"].asString() eq const("start"),
                color = const(route),
                radius = const(5.dp),
                strokeColor = const(casing),
                strokeWidth = const(2.dp)
            )
            CircleLayer(
                id = "live-halo",
                source = here,
                color = const(you.copy(alpha = 0.18f)),
                radius = const(20.dp)
            )
            CircleLayer(
                id = "live",
                source = here,
                color = const(you),
                radius = const(6.5.dp),
                strokeColor = const(Color.White),
                strokeWidth = const(2.5.dp)
            )
        }
        Text(
            text = ATTRIBUTION,
            style = typography.regular11.copy(fontSize = 9.sp),
            color = colors.muted,
            modifier = Modifier
                .align(alignment = attributionAlignment)
                .padding(paddingValues = attributionPadding)
                .clip(shape = RoundedCornerShape(size = 4.dp))
                .background(color = colors.surface.copy(alpha = 0.75f))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

/**
 * Each route as one feature, its paused stretches kept apart, with how faint to draw it. Only the stretches walked
 * or run, or with [isRiding] only those spent in a vehicle.
 */
private fun routesJson(routes: List<MapRoute>, isRiding: Boolean): String = featureCollection(
    features = routes.mapNotNull { mapRoute ->
        val segments = mapRoute.points.groupBy { it.segment }.values.filter { it.size > 1 && it.first().isRiding == isRiding }
        if (segments.isEmpty()) return@mapNotNull null
        feature(
            geometry = buildJsonObject {
                put(key = "type", value = "MultiLineString")
                put(key = "coordinates", element = JsonArray(content = segments.map { segment -> JsonArray(content = segment.map { it.coordinates() }) }))
            },
            properties = buildJsonObject { put(key = "alpha", value = mapRoute.alpha) }
        )
    }
)

private fun pointsJson(points: List<Pair<RoutePoint, String>>): String = featureCollection(
    features = points.map { (point, label) ->
        feature(
            geometry = buildJsonObject {
                put(key = "type", value = "Point")
                put(key = "coordinates", element = point.coordinates())
            },
            properties = buildJsonObject { put(key = "label", value = label) }
        )
    }
)

private fun feature(geometry: JsonObject, properties: JsonObject) = buildJsonObject {
    put(key = "type", value = "Feature")
    put(key = "geometry", element = geometry)
    put(key = "properties", element = properties)
}

private fun featureCollection(features: List<JsonObject>): String = buildJsonObject {
    put(key = "type", value = "FeatureCollection")
    put(key = "features", element = JsonArray(content = features))
}.toString()

/** GeoJSON puts longitude first. */
private fun RoutePoint.coordinates() = buildJsonArray {
    add(element = JsonPrimitive(value = lon))
    add(element = JsonPrimitive(value = lat))
}

/** The box around the points, widened to [MIN_SPAN_METERS] each way so a short route isn't zoomed into a street. */
private fun boundsOf(points: List<RoutePoint>): BoundingBox? {
    if (points.isEmpty()) return null
    val minLat = points.minOf { it.lat }
    val maxLat = points.maxOf { it.lat }
    val minLon = points.minOf { it.lon }
    val maxLon = points.maxOf { it.lon }
    val centerLat = (minLat + maxLat) / 2
    val centerLon = (minLon + maxLon) / 2
    val metersPerDegreeLon = max(METERS_PER_DEGREE * cos(x = centerLat * PI / 180), 1.0)
    val halfLat = max(maxLat - minLat, MIN_SPAN_METERS / METERS_PER_DEGREE) / 2
    val halfLon = max(maxLon - minLon, MIN_SPAN_METERS / metersPerDegreeLon) / 2
    return BoundingBox(west = centerLon - halfLon, south = centerLat - halfLat, east = centerLon + halfLon, north = centerLat + halfLat)
}

/** Where each full [unitMeters] falls along the route, gaps between paused segments left out. */
private fun unitMarkers(points: List<RoutePoint>, unitMeters: Double): List<RoutePoint> {
    val markers = mutableListOf<RoutePoint>()
    var travelled = 0.0
    var next = unitMeters
    points.zipWithNext().forEach { (a, b) ->
        if (!a.continuesTo(next = b)) return@forEach
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
