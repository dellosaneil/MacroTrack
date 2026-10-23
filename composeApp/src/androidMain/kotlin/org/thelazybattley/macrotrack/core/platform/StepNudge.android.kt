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
import kotlinx.coroutines.withTimeoutOrNull
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
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.STEPS_PER_WALKING_MINUTE
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import org.thelazybattley.macrotrack.domain.usecase.activity.recordStepCount
import java.text.NumberFormat
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Waits this long for the step counter's first reading before nudging on what's already saved. */
private const val SENSOR_READ_TIMEOUT_MILLIS = 5_000L

/** An inexact alarm at the hour picked (6 PM unless changed); the phone may run it a little late to save battery. */
class AndroidStepNudgeScheduler(private val context: Context) : StepNudgeScheduler {

    override fun schedule(isEnabled: Boolean, hour: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, StepNudgeReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(intent)
        if (isEnabled) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextNudgeMillis(hour = hour), intent)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun nextNudgeMillis(hour: Int): Long {
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(timeZone = zone).date
        val todayAt = LocalDateTime(date = today, time = LocalTime(hour = hour, minute = 0)).toInstant(timeZone = zone)
        val next = if (todayAt > now) {
            todayAt
        } else {
            LocalDateTime(date = today.plus(period = DatePeriod(days = 1)), time = LocalTime(hour = hour, minute = 0))
                .toInstant(timeZone = zone)
        }
        return next.toEpochMilliseconds()
    }
}

/**
 * Runs at the nudge hour: reads the step counter once so steps taken with the app closed count, and posts a
 * reminder only when the goal is more than [StepGoal.NUDGE_MIN_SHORT] steps off. Then sets tomorrow's. Not today
 * on the reminder just clears it.
 */
class StepNudgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_NOT_TODAY) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            return
        }
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                nudgeIfShort(context = context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun nudgeIfShort(context: Context) {
        val koin = GlobalContext.getOrNull() ?: return
        val repository = koin.get<ActivityRepository>()
        val sensor = koin.get<StepSensor>()
        val isEnabled = repository.isStepNudgeEnabled().first()
        koin.get<StepNudgeScheduler>().schedule(isEnabled = isEnabled, hour = repository.getStepNotificationSettings().first().nudgeHour)
        if (!isEnabled) return
        if (sensor.isAvailable && context.canCountSteps()) {
            withTimeoutOrNull(timeMillis = SENSOR_READ_TIMEOUT_MILLIS) { sensor.counts().first() }?.let { count ->
                repository.recordStepCount(count = count, atMillis = System.currentTimeMillis())
            }
        }
        val today = getCurrentDate()
        val steps = repository.getStepHours(fromDate = today).first().filter { it.date == today }.sumOf { it.steps }
        val short = repository.getStepGoal().first() - steps
        if (short <= StepGoal.NUDGE_MIN_SHORT) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasPermission(permission = Manifest.permission.POST_NOTIFICATIONS)) return
        post(context = context, short = short)
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun post(context: Context, short: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.step_nudge_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val minutes = (short + STEPS_PER_WALKING_MINUTE - 1) / STEPS_PER_WALKING_MINUTE
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.step_nudge_title, NumberFormat.getIntegerInstance().format(short)))
            .setContentText(context.getString(R.string.step_nudge_body, minutes))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .addAction(
                0,
                context.getString(R.string.step_nudge_not_today),
                PendingIntent.getBroadcast(
                    context,
                    1,
                    Intent(context, StepNudgeReceiver::class.java).setAction(ACTION_NOT_TODAY),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                0,
                context.getString(R.string.step_nudge_walk),
                PendingIntent.getActivity(
                    context,
                    1,
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra(AndroidStepNotifier.EXTRA_APP_LINK, AppLink.RECORD_WALK.name),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "step_nudge"
        private const val NOTIFICATION_ID = 42
        private const val ACTION_NOT_TODAY = "org.thelazybattley.macrotrack.nudge.NOT_TODAY"
    }
}
