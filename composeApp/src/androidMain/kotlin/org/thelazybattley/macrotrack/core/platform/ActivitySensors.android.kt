package org.thelazybattley.macrotrack.core.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import org.thelazybattley.macrotrack.domain.usecase.activity.recordStepCount

/** Readings are saved at most this often while walking, so the database isn't written on every step. */
private const val STEP_SAVE_INTERVAL_MILLIS = 5_000L

/** Steps may arrive up to this late, letting the phone batch them and sleep in between. */
private const val STEP_REPORT_LATENCY_MICROS = 10_000_000

private const val LOCATION_INTERVAL_MILLIS = 1_000L

private const val PRESSURE_SMOOTHING = 0.1

/**
 * Android's step counter counts every step since the phone started, whether the app is open or not. Reading it
 * whenever the app runs and saving the difference catches up on steps taken while it was closed.
 */
class AndroidStepSensor(context: Context) : StepSensor {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    override val isAvailable: Boolean get() = sensor != null

    override suspend fun track(repository: ActivityRepository) {
        counts().conflate().collect { count ->
            repository.recordStepCount(count = count, atMillis = System.currentTimeMillis())
            delay(timeMillis = STEP_SAVE_INTERVAL_MILLIS)
        }
    }

    override fun counts(): Flow<Long> = callbackFlow {
        val stepSensor = sensor ?: run {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event.values[0].toLong())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL, STEP_REPORT_LATENCY_MICROS)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

/**
 * GPS fixes from the platform's location service, with a foreground service running alongside so recording
 * carries on with the screen off. The service only keeps the app in the foreground; the fixes come here.
 */
class AndroidLocationTracker(private val context: Context) : LocationTracker {

    @SuppressLint("MissingPermission")
    override fun fixes(type: ActivityType): Flow<LocationFix> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!context.hasPermission(permission = Manifest.permission.ACCESS_FINE_LOCATION)) {
            close()
            return@callbackFlow
        }
        ActivityRecordingService.start(context = context, type = type)
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location.toFix())
            }

            // Older Android versions call these and crash on the default implementations.
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_MILLIS, 0f, listener, Looper.getMainLooper())
        awaitClose {
            locationManager.removeUpdates(listener)
            ActivityRecordingService.stop(context = context)
        }
    }

    private fun Location.toFix() = LocationFix(
        lat = latitude,
        lon = longitude,
        altitude = if (hasAltitude()) altitude else null,
        accuracyMeters = if (hasAccuracy()) accuracy.toDouble() else Double.MAX_VALUE,
        timeMillis = time,
        verticalAccuracyMeters = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasVerticalAccuracy()) verticalAccuracyMeters.toDouble() else null,
        speedMetersPerSecond = if (hasSpeed()) speed.toDouble() else null
    )
}

/**
 * Height from the barometer, on phones that have one. Android can't say whether someone is driving without Google
 * Play services, so [activities] ends at once and riding is told from speed and steps alone.
 */
class AndroidMotionSensor(context: Context) : MotionSensor {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    override fun relativeAltitudes(): Flow<Double> = callbackFlow {
        val pressureSensor = sensor ?: run {
            close()
            return@callbackFlow
        }
        var smoothed: Double? = null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Readings wobble by about a metre; a little smoothing settles them.
                val height = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0]).toDouble()
                val next = smoothed?.let { it + PRESSURE_SMOOTHING * (height - it) } ?: height
                smoothed = next
                trySend(next)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }.conflate()

    override fun activities(): Flow<MotionActivity> = emptyFlow()
}

internal fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

@Composable
actual fun rememberMotionPermissionState(): SensorPermissionState =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(permissions = listOf(Manifest.permission.ACTIVITY_RECOGNITION))
    } else {
        remember { SensorPermissionState(isGranted = true, isPermanentlyDenied = false, request = {}) }
    }

@Composable
actual fun rememberLocationPermissionState(): SensorPermissionState =
    rememberPermissionState(permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))

@Composable
actual fun rememberNotificationPermissionState(): SensorPermissionState =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permissions = listOf(Manifest.permission.POST_NOTIFICATIONS))
    } else {
        remember { SensorPermissionState(isGranted = true, isPermanentlyDenied = false, request = {}) }
    }

/** Asks for [permissions] together; the first one decides whether access is granted. */
@Composable
private fun rememberPermissionState(permissions: List<String>): SensorPermissionState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val main = permissions.first()
    var isGranted by remember { mutableStateOf(value = context.hasPermission(permission = main)) }
    var isPermanentlyDenied by remember { mutableStateOf(value = false) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { result ->
        isGranted = result[main] == true
        // Once someone denies twice Android stops showing the dialog, so the only way back is the app's settings.
        isPermanentlyDenied = !isGranted && activity != null && !activity.shouldShowRationale(permission = main)
    }
    // Picks up access granted in settings while the app was in the background.
    LifecycleResumeEffect(key1 = Unit) {
        isGranted = context.hasPermission(permission = main)
        onPauseOrDispose {}
    }
    return SensorPermissionState(
        isGranted = isGranted,
        isPermanentlyDenied = isPermanentlyDenied,
        request = {
            if (isPermanentlyDenied) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                launcher.launch(permissions.toTypedArray())
            }
        }
    )
}

private fun Activity.shouldShowRationale(permission: String): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
