@file:OptIn(ExperimentalForeignApi::class)

package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.toLocalDateTime
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import platform.CoreLocation.CLActivityTypeFitness
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLDistanceFilterNone
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreMotion.CMAuthorizationStatusAuthorized
import platform.CoreMotion.CMAuthorizationStatusDenied
import platform.CoreMotion.CMAuthorizationStatusNotDetermined
import platform.CoreMotion.CMAuthorizationStatusRestricted
import platform.CoreMotion.CMPedometer
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

private const val MILLIS_PER_HOUR = 3_600_000L

/** iOS keeps a week of step history. */
private const val HISTORY_HOURS = 7 * 24

/** While the app is open, the current hour is read again this often. */
private const val STEP_REFRESH_MILLIS = 60_000L

/**
 * iOS keeps its own step history, so each hour is read straight from it: the hours since the last read, then the
 * current hour every minute while the app is open.
 */
class IosStepSensor : StepSensor {

    private val pedometer = CMPedometer()

    override val isAvailable: Boolean get() = CMPedometer.isStepCountingAvailable()

    override suspend fun track(repository: ActivityRepository) {
        if (!isAvailable) return
        while (true) {
            val now = currentMillis()
            val currentHour = now / MILLIS_PER_HOUR * MILLIS_PER_HOUR
            val from = maxOf(repository.getStepsSyncedUntil() ?: 0L, currentHour - HISTORY_HOURS * MILLIS_PER_HOUR)
            var hourStart = from / MILLIS_PER_HOUR * MILLIS_PER_HOUR
            while (hourStart <= currentHour) {
                val steps = stepsBetween(fromMillis = hourStart, toMillis = minOf(hourStart + MILLIS_PER_HOUR, now))
                if (steps != null) {
                    val time = hourStart.toLocalDateTime()
                    repository.setSteps(date = time.date, hour = time.hour, steps = steps)
                }
                hourStart += MILLIS_PER_HOUR
            }
            // The current hour is still filling up, so the next pass starts there.
            repository.setStepsSyncedUntil(millis = currentHour)
            delay(timeMillis = STEP_REFRESH_MILLIS)
        }
    }

    override fun counts(): Flow<Long> = callbackFlow {
        if (!isAvailable) {
            close()
            return@callbackFlow
        }
        val livePedometer = CMPedometer()
        livePedometer.startPedometerUpdatesFromDate(start = NSDate()) { data, _ ->
            data?.numberOfSteps?.longLongValue?.let { trySend(it) }
        }
        awaitClose { livePedometer.stopPedometerUpdates() }
    }

    private suspend fun stepsBetween(fromMillis: Long, toMillis: Long): Int? = suspendCancellableCoroutine { continuation ->
        pedometer.queryPedometerDataFromDate(start = fromMillis.toNSDate(), toDate = toMillis.toNSDate()) { data, _ ->
            if (continuation.isActive) continuation.resume(value = data?.numberOfSteps?.intValue)
        }
    }
}

/**
 * Location from Core Location while a walk or run is recorded. Background updates are on only while it's collected,
 * with the blue indicator showing, so recording carries on with the screen locked.
 */
class IosLocationTracker : LocationTracker {

    override fun fixes(type: ActivityType): Flow<LocationFix> = callbackFlow {
        val manager = CLLocationManager()
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                didUpdateLocations.filterIsInstance<CLLocation>().forEach { trySend(it.toFix()) }
            }
        }
        manager.delegate = delegate
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = kCLDistanceFilterNone
        manager.activityType = CLActivityTypeFitness
        manager.pausesLocationUpdatesAutomatically = false
        manager.allowsBackgroundLocationUpdates = true
        manager.showsBackgroundLocationIndicator = true
        manager.startUpdatingLocation()
        awaitClose {
            manager.stopUpdatingLocation()
            manager.allowsBackgroundLocationUpdates = false
            manager.delegate = null
        }
    }.flowOn(context = Dispatchers.Main)

    private fun CLLocation.toFix(): LocationFix {
        val (lat, lon) = coordinate.useContents { latitude to longitude }
        return LocationFix(
            lat = lat,
            lon = lon,
            // A negative vertical accuracy means the altitude isn't valid.
            altitude = altitude.takeIf { verticalAccuracy >= 0 },
            accuracyMeters = horizontalAccuracy.takeIf { it >= 0 } ?: Double.MAX_VALUE,
            timeMillis = (timestamp.timeIntervalSince1970 * 1000).toLong()
        )
    }
}

@Composable
actual fun rememberMotionPermissionState(): SensorPermissionState {
    var status by remember { mutableStateOf(value = CMPedometer.authorizationStatus()) }
    LifecycleResumeEffect(key1 = Unit) {
        status = CMPedometer.authorizationStatus()
        onPauseOrDispose {}
    }
    val isPermanentlyDenied = status == CMAuthorizationStatusDenied || status == CMAuthorizationStatusRestricted
    return SensorPermissionState(
        // Without a step counter there's nothing to ask for; the screens then show steps as unavailable.
        isGranted = status == CMAuthorizationStatusAuthorized || !CMPedometer.isStepCountingAvailable(),
        isPermanentlyDenied = isPermanentlyDenied,
        request = {
            if (isPermanentlyDenied || status != CMAuthorizationStatusNotDetermined) {
                openSettings()
            } else {
                // iOS has no separate request: the first read asks.
                val now = currentMillis()
                CMPedometer().queryPedometerDataFromDate(start = (now - 60_000).toNSDate(), toDate = now.toNSDate()) { _, _ ->
                    dispatch_async(queue = dispatch_get_main_queue()) { status = CMPedometer.authorizationStatus() }
                }
            }
        }
    )
}

@Composable
actual fun rememberLocationPermissionState(): SensorPermissionState {
    val manager = remember { CLLocationManager() }
    var status by remember { mutableStateOf(value = manager.authorizationStatus) }
    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                status = manager.authorizationStatus
            }
        }
    }
    DisposableEffect(key1 = manager) {
        manager.delegate = delegate
        onDispose { manager.delegate = null }
    }
    LifecycleResumeEffect(key1 = Unit) {
        status = manager.authorizationStatus
        onPauseOrDispose {}
    }
    // iOS asks only once; after a denial the only way back is the app's page in Settings.
    val isPermanentlyDenied = status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted
    return SensorPermissionState(
        isGranted = status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways,
        isPermanentlyDenied = isPermanentlyDenied,
        request = {
            if (isPermanentlyDenied) openSettings() else manager.requestWhenInUseAuthorization()
        }
    )
}

/** The evening reminder isn't scheduled on iOS, so there's nothing to ask for. */
@Composable
actual fun rememberNotificationPermissionState(): SensorPermissionState =
    remember { SensorPermissionState(isGranted = true, isPermanentlyDenied = false, request = {}) }

private fun openSettings() {
    NSURL.URLWithString(URLString = UIApplicationOpenSettingsURLString)?.let { url ->
        UIApplication.sharedApplication.openURL(url = url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

private fun currentMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

private fun Long.toNSDate(): NSDate = NSDate.dateWithTimeIntervalSince1970(secs = this / 1000.0)
