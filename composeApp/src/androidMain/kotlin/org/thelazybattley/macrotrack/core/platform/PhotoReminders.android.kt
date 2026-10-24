package org.thelazybattley.macrotrack.core.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.repository.ProgressPhotoRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** One inexact alarm for the next day any album has a reminder; [PhotoReminderReceiver] sets the one after. */
class AndroidPhotoReminderScheduler(private val context: Context) : PhotoReminderScheduler {

    override suspend fun schedule(albums: List<PhotoAlbum>) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, PhotoReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(intent)
        nextReminderMillis(albums = albums)?.let { atMillis ->
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        }
    }

    /** The soonest reminder still ahead across every album's days and time. */
    @OptIn(ExperimentalTime::class)
    private fun nextReminderMillis(albums: List<PhotoAlbum>): Long? {
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(timeZone = zone).date
        return albums.flatMap { album ->
            (0..7).map { offset -> today.plus(period = DatePeriod(days = offset)) }
                .filter { it.dayOfWeek in album.reminderDays }
                .map { date -> LocalDateTime(date = date, time = album.reminderTime()).toInstant(timeZone = zone) }
        }.filter { it > now }.minOrNull()?.toEpochMilliseconds()
    }
}

private fun PhotoAlbum.reminderTime() = LocalTime(hour = reminderMinutes / 60, minute = reminderMinutes % 60)

/**
 * Runs at a reminder time: names the albums due a photo in the last [DUE_WINDOW_MINUTES] (the alarm can run a little
 * late, and albums can share a time), then sets the next reminder.
 */
class PhotoReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                remind(context = context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun remind(context: Context) {
        val koin = GlobalContext.getOrNull() ?: return
        val albums = koin.get<ProgressPhotoRepository>().getAlbums().first()
        koin.get<PhotoReminderScheduler>().schedule(albums = albums)
        val now = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        val nowMinutes = now.hour * 60 + now.minute
        val due = albums.filter { album ->
            now.dayOfWeek in album.reminderDays && nowMinutes - album.reminderMinutes in 0 until DUE_WINDOW_MINUTES
        }
        if (due.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasPermission(permission = Manifest.permission.POST_NOTIFICATIONS)) return
        post(context = context, names = due.map { it.name })
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun post(context: Context, names: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.photo_reminder_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.photo_reminder_title))
            .setContentText(context.getString(R.string.photo_reminder_body, names.joinToString(separator = ", ")))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "photo_reminders"
        private const val NOTIFICATION_ID = 4_300
        private const val DUE_WINDOW_MINUTES = 60
    }
}
