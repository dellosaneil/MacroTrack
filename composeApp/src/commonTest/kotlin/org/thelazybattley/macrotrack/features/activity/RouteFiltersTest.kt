package org.thelazybattley.macrotrack.features.activity

import org.thelazybattley.macrotrack.core.platform.MotionActivity
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.RoutePoint
import org.thelazybattley.macrotrack.domain.model.distanceMeters
import org.thelazybattley.macrotrack.domain.model.movingSeconds
import org.thelazybattley.macrotrack.domain.model.routeClimb
import org.thelazybattley.macrotrack.domain.model.routeDistance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteFiltersTest {

    @Test
    fun smootherDampsAJumpyFix() {
        val smoother = LocationSmoother(metersPerSecond = 2.8)
        smoother.add(lat = 0.0, lon = 0.0, accuracyMeters = 5.0, timeMillis = 0)
        // A second later, a rough fix about 111 m north.
        smoother.add(lat = 0.001, lon = 0.0, accuracyMeters = 30.0, timeMillis = 1_000)
        val moved = distanceMeters(from = point(lat = 0.0), to = point(lat = smoother.lat))
        assertTrue(moved < 10, "moved $moved m")
    }

    @Test
    fun smootherFollowsSteadyFixes() {
        val smoother = LocationSmoother(metersPerSecond = 2.8)
        repeat(times = 30) { second ->
            smoother.add(lat = 0.00001 * second, lon = 0.0, accuracyMeters = 5.0, timeMillis = second * 1_000L)
        }
        // Within a couple of metres of the last fix.
        assertTrue(distanceMeters(from = point(lat = 0.00029), to = point(lat = smoother.lat)) < 2)
    }

    @Test
    fun drivingIsSpottedFromSpeedAfterAWhile() {
        val detector = RidingDetector(type = ActivityType.WALK)
        assertNull(detector.feed(seconds = 0, speed = 12.0))
        assertNull(detector.feed(seconds = 10, speed = 12.0))
        assertEquals(0L, detector.feed(seconds = 15, speed = 12.0))
    }

    @Test
    fun aShortSprintIsntRiding() {
        val detector = RidingDetector(type = ActivityType.WALK)
        assertNull(detector.feed(seconds = 0, speed = 4.0, lastStep = 0))
        assertNull(detector.feed(seconds = 5, speed = 4.0, lastStep = 5))
        // Back to walking pace with steps before the 15 s are up.
        assertNull(detector.feed(seconds = 8, speed = 1.4, lastStep = 8))
        assertNull(detector.feed(seconds = 20, speed = 4.0, lastStep = 20))
    }

    @Test
    fun cyclingAtRunningSpeedIsSpottedByTheMissingSteps() {
        val detector = RidingDetector(type = ActivityType.RUN)
        // Steps were counted at the start, then none while moving at 5 m/s.
        assertNull(detector.feed(seconds = 40, speed = 5.0, lastStep = 0, stepsSince = 0))
        assertEquals(40_000L, detector.feed(seconds = 55, speed = 5.0, lastStep = 0, stepsSince = 0))
    }

    @Test
    fun noStepCounterNeverMeansStepless() {
        val detector = RidingDetector(type = ActivityType.RUN)
        assertNull(detector.feed(seconds = 0, speed = 5.0, stepsSince = null))
        assertNull(detector.feed(seconds = 60, speed = 5.0, stepsSince = null))
    }

    @Test
    fun theMotionChipSaysWhenRidingStops() {
        val detector = RidingDetector(type = ActivityType.WALK)
        assertNull(detector.feed(seconds = 0, speed = 1.3, isRiding = true, activity = MotionActivity.ON_FOOT))
        assertNotNull(detector.feed(seconds = 20, speed = 1.3, isRiding = true, activity = MotionActivity.ON_FOOT))
    }

    @Test
    fun ridingStretchesDontCount() {
        val walked = listOf(point(lat = 0.0, minute = 0), point(lat = 0.001, minute = 1))
        val rode = listOf(
            point(lat = 0.001, minute = 1, segment = 1, isRiding = true, altitude = 0.0),
            point(lat = 0.02, minute = 3, segment = 1, isRiding = true, altitude = 50.0)
        )
        val route = walked + rode
        assertEquals(walked.routeDistance(), route.routeDistance())
        assertEquals(60, route.movingSeconds())
        assertEquals(0.0, route.routeClimb())
    }

    private fun RidingDetector.feed(
        seconds: Int,
        speed: Double,
        isRiding: Boolean = false,
        lastStep: Int? = null,
        stepsSince: Int? = 0,
        activity: MotionActivity? = null
    ): Long? = update(
        isRiding = isRiding,
        timeMillis = seconds * 1_000L,
        speed = speed,
        lastStepMillis = lastStep?.let { it * 1_000L },
        stepsSinceMillis = stepsSince?.let { it * 1_000L },
        activity = activity
    )

    private fun point(lat: Double, minute: Int = 0, segment: Int = 0, isRiding: Boolean = false, altitude: Double? = null) =
        RoutePoint(lat = lat, lon = 0.0, altitude = altitude, timeMillis = minute * 60_000L, segment = segment, isRiding = isRiding)
}
