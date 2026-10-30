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
import kotlinx.coroutines.launch
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.water_reminder_channel
import org.jetbrains.compose.resources.getString
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.usecase.reminder.toEpochMillis
import org.thelazybattley.macrotrack.domain.usecase.water.GetDueWaterReminderUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.ScheduleWaterRemindersUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** One alarm for the next check; [WaterReminderReceiver] looks at the water drunk when it goes off, then sets the one after. */
class AndroidWaterReminderScheduler(private val context: Context) : WaterReminderScheduler {

    override val days: Int = 2

    override suspend fun schedule(upcoming: List<WaterReminder>) {
        val intent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, WaterReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        context.getSystemService(AlarmManager::class.java).cancel(intent)
        upcoming.firstOrNull()?.let { next ->
            context.setReminderAlarm(atMillis = next.at.toEpochMillis(), intent = intent)
        }
    }
}

/** Goes off at a check time: reminds only when today's water is behind the usual pace, then sets the next check. */
class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                val koin = GlobalContext.getOrNull() ?: return@launch
                koin.get<GetDueWaterReminderUseCase>()()?.let { post(context = context.applicationContext, message = it) }
                koin.get<ScheduleWaterRemindersUseCase>()()
            } finally {
                pending.finish()
            }
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private suspend fun post(context: Context, message: WaterReminderMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasPermission(permission = Manifest.permission.POST_NOTIFICATIONS)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(resource = Res.string.water_reminder_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val openWater = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_REMINDER_ROUTE, AppDestinations.Root.Water.route)
                .putExtra(EXTRA_REMINDER_NOTIFICATION, NOTIFICATION_ID),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openWater)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "water_reminders"
        private const val NOTIFICATION_ID = 4_330
    }
}
