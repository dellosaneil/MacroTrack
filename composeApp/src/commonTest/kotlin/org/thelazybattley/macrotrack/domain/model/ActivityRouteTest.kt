package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActivityRouteTest {

    /** A thousandth of a degree of latitude is about 111.19 m. */
    private val step = 111.195

    /** Points heading north, one a minute. */
    private fun north(count: Int, segment: Int = 0, startMinute: Int = 0, altitudes: List<Double?> = List(size = count) { null }) =
        (0 until count).map { index ->
            RoutePoint(
                lat = (startMinute + index) * 0.001,
                lon = 0.0,
                altitude = altitudes[index],
                timeMillis = (startMinute + index) * 60_000L,
                segment = segment
            )
        }

    @Test
    fun distanceAddsUpAlongTheRoute() {
        assertEquals(step * 9, north(count = 10).routeDistance(), absoluteTolerance = 0.5)
    }

    @Test
    fun pausesLeaveTheirGapOutOfDistanceAndTime() {
        // Paused for 20 minutes between the two stretches.
        val route = north(count = 3) + north(count = 3, segment = 1, startMinute = 22)
        assertEquals(step * 4, route.routeDistance(), absoluteTolerance = 0.5)
        assertEquals(4 * 60, route.movingSeconds())
    }

    @Test
    fun climbIgnoresGpsWobbleOnTheFlat() {
        val flat = north(count = 4, altitudes = listOf(10.0, 12.0, 10.0, 12.0))
        assertEquals(0.0, flat.routeClimb())
        val hill = north(count = 6, altitudes = listOf(10.0, 11.0, 12.0, 15.0, 14.0, 20.0))
        assertEquals(11.0, hill.routeClimb())
    }

    @Test
    fun splitsEveryKmWithTheTimeEachTook() {
        val splits = north(count = 20).splits(unitMeters = METERS_PER_KM)
        // 19 steps is about 2.11 km: two full splits and a short last one.
        assertEquals(3, splits.size)
        assertEquals(METERS_PER_KM, splits[0].meters)
        assertEquals(METERS_PER_KM / step * 60, splits[0].seconds, absoluteTolerance = 0.5)
        assertEquals(splits[0].pace(unitMeters = METERS_PER_KM), splits[2].pace(unitMeters = METERS_PER_KM), absoluteTolerance = 0.5)
    }

    @Test
    fun paceNeedsSomeDistance() {
        val session = ActivitySession(
            type = ActivityType.WALK, startMillis = 0, endMillis = 60_000, movingSeconds = 60, distanceMeters = 20.0, steps = 30, climbMeters = 0.0
        )
        assertNull(session.paceSecondsPerKm)
        assertEquals(600.0, session.copy(distanceMeters = 100.0).paceSecondsPerKm)
    }
}
