package org.thelazybattley.macrotrack.core.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.METERS_PER_KM
import org.thelazybattley.macrotrack.domain.model.METERS_PER_MILE
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.features.activity.ActivityRecorder
import org.thelazybattley.macrotrack.features.activity.Recording
import java.util.Locale

/** The notification's numbers are refreshed at most this often; Pause and Resume show at once. */
private const val UPDATE_INTERVAL_MILLIS = 3_000L

/**
 * Keeps the app in the foreground while a walk or run is recorded, so location keeps coming with the screen off.
 * Its notification can't be swiped away until Finish or Discard: it shows the time, distance, steps and pace, with
 * Pause / Resume, Finish and Open, and on Android 16 a compact time chip at the top of the screen. Location still
 * needs only "while using the app".
 */
class ActivityRecordingService : Service(), KoinComponent {

    private val recorder: ActivityRecorder by inject()
    private val getUnitSystem: GetUnitSystemUseCase by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_TOGGLE_PAUSE) {
            recorder.togglePause()
            return START_NOT_STICKY
        }
        if (isStarted) return START_NOT_STICKY
        isStarted = true
        val type = intent?.getStringExtra(EXTRA_TYPE)?.let { name -> ActivityType.entries.find { it.name == name } } ?: ActivityType.WALK
        createChannel(context = this)
        val recording = recorder.state.value ?: Recording(type = type, startMillis = System.currentTimeMillis())
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(recording = recording, unitSystem = UnitSystem.METRIC),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        )
        val manager = getSystemService(NotificationManager::class.java)
        scope.launch {
            var shown: Recording? = null
            var shownAt = 0L
            combine(flow = recorder.state.filterNotNull(), flow2 = getUnitSystem()) { current, units -> current to units }
                .collectLatest { (current, units) ->
                    // A new fix cancels the wait, so the numbers land no later than the interval after the last update.
                    if (current.isPaused == shown?.isPaused) {
                        delay(timeMillis = (shownAt + UPDATE_INTERVAL_MILLIS - System.currentTimeMillis()).coerceAtLeast(minimumValue = 0))
                    }
                    manager.notify(NOTIFICATION_ID, buildNotification(recording = current, unitSystem = units))
                    shown = current
                    shownAt = System.currentTimeMillis()
                }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(recording: Recording, unitSystem: UnitSystem): Notification {
        val now = System.currentTimeMillis()
        val isRun = recording.type == ActivityType.RUN
        val movingMillis = recording.movingMillis(now = now)
        val stats = statsText(recording = recording, movingSeconds = movingMillis / 1000, unitSystem = unitSystem)
        val title = getString(
            when {
                recording.isPaused && isRun -> R.string.recording_paused_run
                recording.isPaused -> R.string.recording_paused_walk
                isRun -> R.string.recording_run
                else -> R.string.recording_walk
            }
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setSubText(getString(R.string.recording_using_location))
            .setContentIntent(openAppIntent(finish = false))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setRequestPromotedOngoing(true)
            .addAction(
                if (recording.isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                getString(if (recording.isPaused) R.string.recording_action_resume else R.string.recording_action_pause),
                PendingIntent.getService(
                    this,
                    REQUEST_TOGGLE,
                    Intent(this, ActivityRecordingService::class.java).setAction(ACTION_TOGGLE_PAUSE),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(0, getString(R.string.recording_action_finish), openAppIntent(finish = true))
            .addAction(0, getString(R.string.recording_action_open), openAppIntent(finish = false))
        if (recording.isPaused) {
            // Stopped clock: the time goes in the text, and the chip at the top shows it still.
            val time = clockDuration(seconds = movingMillis / 1000)
            builder.setContentText("$time · $stats").setShowWhen(false).setUsesChronometer(false).setShortCriticalText(time)
        } else {
            builder.setContentText(stats).setShowWhen(true).setUsesChronometer(true).setWhen(now - movingMillis)
        }
        return builder.build()
    }

    /** "1.61 km · 2,090 steps · 11:26 /km". */
    private fun statsText(recording: Recording, movingSeconds: Long, unitSystem: UnitSystem): String {
        val isImperial = unitSystem == UnitSystem.IMPERIAL
        val unitMeters = if (isImperial) METERS_PER_MILE else METERS_PER_KM
        val distance = String.format(Locale.getDefault(), "%.2f %s", recording.distanceMeters / unitMeters, getString(if (isImperial) R.string.recording_unit_mi else R.string.recording_unit_km))
        val steps = getString(R.string.recording_steps, String.format(Locale.getDefault(), "%,d", recording.steps))
        if (recording.distanceMeters < 50) return "$distance · $steps"
        val pace = clockDuration(seconds = (movingSeconds / (recording.distanceMeters / unitMeters)).toLong())
        return "$distance · $steps · " + getString(if (isImperial) R.string.recording_pace_mi else R.string.recording_pace_km, pace)
    }

    /** Opens the live map; with [finish], with its save sheet up. */
    private fun openAppIntent(finish: Boolean): PendingIntent = PendingIntent.getActivity(
        this,
        if (finish) REQUEST_FINISH else REQUEST_OPEN,
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_OPEN_RECORDING, if (finish) OPEN_FINISH else OPEN_MAP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    companion object {
        private const val CHANNEL_ID = "recording"
        private const val NOTIFICATION_ID = 41
        private const val EXTRA_TYPE = "type"
        private const val ACTION_TOGGLE_PAUSE = "org.thelazybattley.macrotrack.recording.TOGGLE_PAUSE"
        private const val REQUEST_TOGGLE = 1
        private const val REQUEST_OPEN = 2
        private const val REQUEST_FINISH = 3

        /** Set on the intent that opens the app from the notification: [OPEN_MAP] or [OPEN_FINISH]. */
        const val EXTRA_OPEN_RECORDING = "open_recording"
        const val OPEN_MAP = "map"
        const val OPEN_FINISH = "finish"

        fun start(context: Context, type: ActivityType) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ActivityRecordingService::class.java).putExtra(EXTRA_TYPE, type.name)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ActivityRecordingService::class.java))
        }

        private fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.recording_channel), NotificationManager.IMPORTANCE_LOW)
            )
        }

        /** "18:24", or "1:02:05" past an hour. */
        private fun clockDuration(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format(Locale.US, "%d:%02d", minutes, secs)
            }
        }
    }
}
