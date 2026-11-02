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
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_action_done
import macrotrack.composeapp.generated.resources.checklist_action_skip
import macrotrack.composeapp.generated.resources.checklist_action_snooze
import macrotrack.composeapp.generated.resources.checklist_reminder_channel
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetDueChecklistRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.HandleChecklistReminderActionUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.ScheduleChecklistRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.toEpochMillis
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** One alarm for the next reminder; [ChecklistReminderReceiver] checks what's ticked when it goes off, then sets the one after. */
class AndroidChecklistReminderScheduler(private val context: Context) : ChecklistReminderScheduler {

    override val days: Int = 2

    override suspend fun schedule(upcoming: List<ChecklistReminder>) {
        val intent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ChecklistReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        context.getSystemService(AlarmManager::class.java).cancel(intent)
        upcoming.firstOrNull()?.let { next ->
            context.setReminderAlarm(atMillis = next.at.toEpochMillis(), intent = intent)
        }
    }
}

/** Goes off at a reminder time: shows each item due and still not ticked, then sets the next alarm. */
class ChecklistReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = runAsync { koin ->
        koin.get<GetDueChecklistRemindersUseCase>()().forEach { ChecklistNotifications.post(context = context.applicationContext, reminder = it) }
        koin.get<ScheduleChecklistRemindersUseCase>()()
    }
}

/** Done, In 1 hour and Skip today on a reminder: does it without opening the app and takes the reminder away. */
class ChecklistActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = runAsync { koin ->
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L).takeIf { it >= 0 } ?: return@runAsync
        val date = intent.getStringExtra(EXTRA_DATE)?.let { LocalDate.parse(input = it) } ?: return@runAsync
        val action = intent.getStringExtra(EXTRA_ACTION)?.let { ChecklistReminderAction.valueOf(value = it) } ?: return@runAsync
        NotificationManagerCompat.from(context).cancel(ChecklistNotifications.id(itemId = itemId))
        koin.get<HandleChecklistReminderActionUseCase>()(itemId = itemId, date = date, action = action)
    }

    companion object {
        const val EXTRA_ITEM_ID = "checklist_item_id"
        const val EXTRA_DATE = "checklist_date"
        const val EXTRA_ACTION = "checklist_action"
    }
}

private object ChecklistNotifications {

    private const val CHANNEL_ID = "checklist_reminders"
    private const val NOTIFICATION_BASE_ID = 5_100

    fun id(itemId: Long): Int = NOTIFICATION_BASE_ID + (itemId % 100_000).toInt()

    @android.annotation.SuppressLint("MissingPermission")
    suspend fun post(context: Context, reminder: ChecklistReminder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasPermission(permission = Manifest.permission.POST_NOTIFICATIONS)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(resource = Res.string.checklist_reminder_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val id = id(itemId = reminder.itemId)
        val open = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_REMINDER_ROUTE, AppDestinations.Root.Checklist.route)
                .putExtra(EXTRA_REMINDER_NOTIFICATION, id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(reminder.message.title)
            .setContentText(reminder.message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.message.body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
        listOf(
            ChecklistReminderAction.DONE to Res.string.checklist_action_done,
            ChecklistReminderAction.SNOOZE to Res.string.checklist_action_snooze,
            ChecklistReminderAction.SKIP to Res.string.checklist_action_skip
        ).forEach { (action, label) ->
            val intent = PendingIntent.getBroadcast(
                context,
                id * 10 + action.ordinal,
                Intent(context, ChecklistActionReceiver::class.java)
                    .putExtra(ChecklistActionReceiver.EXTRA_ITEM_ID, reminder.itemId)
                    .putExtra(ChecklistActionReceiver.EXTRA_DATE, reminder.date.toString())
                    .putExtra(ChecklistActionReceiver.EXTRA_ACTION, action.name),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, getString(resource = label), intent)
        }
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
