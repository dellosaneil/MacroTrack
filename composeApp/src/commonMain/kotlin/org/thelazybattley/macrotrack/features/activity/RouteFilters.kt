package org.thelazybattley.macrotrack.features.activity

import org.thelazybattley.macrotrack.core.platform.MotionActivity
import org.thelazybattley.macrotrack.domain.model.ActivityType

/**
 * Smooths GPS fixes with a simple Kalman filter: each fix pulls the position toward it by how sure it is, and the
 * longer since the last one, the further the user may have gone, at about [metersPerSecond]. Jitter of a few metres
 * either way, and the odd fix far off to one side, no longer zigzag the route.
 */
class LocationSmoother(var metersPerSecond: Double) {

    var lat: Double = 0.0
        private set
    var lon: Double = 0.0
        private set

    /** How sure the position is, as a variance in square metres; negative before the first fix. */
    private var variance = -1.0
    private var lastMillis = 0L

    val hasPosition: Boolean get() = variance >= 0

    fun add(lat: Double, lon: Double, accuracyMeters: Double, timeMillis: Long) {
        val accuracy = accuracyMeters.coerceAtLeast(minimumValue = MIN_ACCURACY_METERS)
        if (variance < 0) {
            reset(lat = lat, lon = lon, accuracyMeters = accuracy, timeMillis = timeMillis)
            return
        }
        val elapsedMillis = timeMillis - lastMillis
        if (elapsedMillis > 0) {
            variance += elapsedMillis * metersPerSecond * metersPerSecond / 1000
            lastMillis = timeMillis
        }
        val gain = variance / (variance + accuracy * accuracy)
        this.lat += gain * (lat - this.lat)
        this.lon += gain * (lon - this.lon)
        variance *= 1 - gain
    }

    /** Starts again from this fix, as after a jump the filter wouldn't believe. */
    fun reset(lat: Double, lon: Double, accuracyMeters: Double, timeMillis: Long) {
        this.lat = lat
        this.lon = lon
        variance = accuracyMeters * accuracyMeters
        lastMillis = timeMillis
    }

    private companion object {
        const val MIN_ACCURACY_METERS = 1.0
    }
}

/**
 * Tells when the user stops walking or running and gets into a car, bus or onto a bike, and back. Each switch needs
 * a run of evidence, so a sprint for the bus or a stop at the lights doesn't flip it:
 *
 * - The phone's motion chip saying driving or cycling, where it can tell.
 * - Moving faster than anyone goes on foot for this [type].
 * - Moving at a decent clip with no steps counted, where steps are being counted.
 *
 * Being on foot is the chip saying so, or steps coming in at a pace people walk or run.
 */
class RidingDetector(private val type: ActivityType) {

    private var switchSince: Long? = null

    /** Faster than this is a vehicle or a bike. */
    val maxFootSpeed: Double get() = if (type == ActivityType.RUN) MAX_RUN_SPEED else MAX_WALK_SPEED

    /**
     * Feeds one moment in. [isRiding] is what's believed now; [lastStepMillis] is when a step last came in, and
     * [stepsSinceMillis] when step counting started, or null without a step counter. Returns when the switch began
     * once there's been enough evidence to flip [isRiding], or null.
     */
    fun update(
        isRiding: Boolean,
        timeMillis: Long,
        speed: Double?,
        lastStepMillis: Long?,
        stepsSinceMillis: Long?,
        activity: MotionActivity?
    ): Long? {
        val hasSteps = lastStepMillis != null && timeMillis - lastStepMillis <= STEP_WINDOW_MILLIS
        // Steps arrive late on some phones, so no steps means none for a while since counting began.
        val isStepless = stepsSinceMillis != null && timeMillis - stepsSinceMillis >= STEP_WINDOW_MILLIS && !hasSteps
        val looksRiding = activity == MotionActivity.AUTOMOTIVE ||
            activity == MotionActivity.CYCLING ||
            (speed != null && speed > maxFootSpeed) ||
            (speed != null && speed >= MIN_RIDING_SPEED && isStepless)
        // Far too fast for feet beats the motion chip, which can take a minute or two to notice a car.
        val isFarTooFast = speed != null && speed > maxFootSpeed * 2
        val looksOnFoot = !isFarTooFast && (
            activity == MotionActivity.ON_FOOT || (activity == null && hasSteps && (speed == null || speed <= maxFootSpeed))
        )
        val toward = if (isRiding) looksOnFoot && !looksRiding else looksRiding && !looksOnFoot
        val away = if (isRiding) looksRiding else looksOnFoot
        when {
            toward -> {
                val since = switchSince ?: timeMillis.also { switchSince = it }
                val needed = if (isRiding) STOP_RIDING_MILLIS else START_RIDING_MILLIS
                if (timeMillis - since >= needed) {
                    switchSince = null
                    return since
                }
            }
            away -> switchSince = null
        }
        return null
    }

    private companion object {
        /** A brisk walk tops out around 8 km/h, a fast run around 22 km/h. */
        const val MAX_WALK_SPEED = 2.8
        const val MAX_RUN_SPEED = 6.5

        /** Slower than this with no steps could be standing still with GPS drifting, so it says nothing. */
        const val MIN_RIDING_SPEED = 2.2

        const val STEP_WINDOW_MILLIS = 30_000L
        const val START_RIDING_MILLIS = 15_000L
        const val STOP_RIDING_MILLIS = 20_000L
    }
}
