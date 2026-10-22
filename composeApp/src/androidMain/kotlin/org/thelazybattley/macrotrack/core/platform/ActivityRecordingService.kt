package org.thelazybattley.macrotrack.core.platform

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
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.model.ActivityType

/**
 * Keeps the app in the foreground while a walk or run is recorded, so location keeps coming with the screen off.
 * Its notification says what's being recorded and opens the app. Location still needs only "while using the app".
 */
class ActivityRecordingService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = intent?.getStringExtra(EXTRA_TYPE)?.let { name -> ActivityType.entries.find { it.name == name } } ?: ActivityType.WALK
        createChannel(context = this)
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(getString(if (type == ActivityType.RUN) R.string.recording_run else R.string.recording_walk))
            .setContentText(getString(R.string.recording_body))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .build()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        )
        return START_NOT_STICKY
    }

    companion object {
        private const val CHANNEL_ID = "recording"
        private const val NOTIFICATION_ID = 41
        private const val EXTRA_TYPE = "type"

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
    }
}
