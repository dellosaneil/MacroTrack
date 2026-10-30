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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.reminder_done_logged_more
import macrotrack.composeapp.generated.resources.reminder_channel
import macrotrack.composeapp.generated.resources.reminder_done_kept
import macrotrack.composeapp.generated.resources.reminder_done_later
import macrotrack.composeapp.generated.resources.reminder_done_logged
import macrotrack.composeapp.generated.resources.reminder_done_skipped
import macrotrack.composeapp.generated.resources.reminder_done_skipped_weight
import macrotrack.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.model.LogReminder
import org.thelazybattley.macrotrack.domain.model.LogReminderKind
import org.thelazybattley.macrotrack.domain.usecase.photo.SchedulePhotoRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetDueLogRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetLogReminderMessageUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.LogReminderActions
import org.thelazybattley.macrotrack.domain.usecase.reminder.ScheduleLogRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.formatClockTime
import org.thelazybattley.macrotrack.domain.usecase.reminder.toEpochMillis
import org.thelazybattley.macrotrack.domain.usecase.water.ScheduleWaterRemindersUseCase
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.reminders.logReminderRoute

/**
 * One alarm for the next reminder. [LogReminderReceiver] checks the log when it goes off, so a meal logged since
 * sends nothing, then sets the one after.
 */
class AndroidLogReminderScheduler(private val context: Context) : LogReminderScheduler {

    override val daysAhead: Int = 8

    override val canSendTest: Boolean = true

    override suspend fun schedule(upcoming: List<LogReminder>, message: suspend (LogReminder) -> LogReminderMessage) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, LogReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(intent)
        upcoming.firstOrNull()?.let { next ->
            context.setReminderAlarm(atMillis = next.at.toEpochMillis(), intent = intent)
        }
    }

    override suspend fun sendTest(message: LogReminderMessage) {
        LogReminderNotifications.post(context = context, message = message)
    }
}

/** Runs [block] off the main thread for as long as a broadcast may, with the app's Koin. */
private fun BroadcastReceiver.runAsync(block: suspend (Koin) -> Unit) {
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        try {
            GlobalContext.getOrNull()?.let { block(it) }
        } finally {
            pending.finish()
        }
    }
}

/**
 * Goes off at a reminder time: shows what's due and still not logged, then sets the next alarm. After a restart or
 * an app update, when alarms are cleared, it sets them again, photo and water reminders too; likewise once on-time reminders
 * are allowed, so the ones already set become exact.
 */
class LogReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = runAsync { koin ->
        val appContext = context.applicationContext
        if (intent.action in RESCHEDULE_ACTIONS) {
            koin.get<SchedulePhotoRemindersUseCase>()()
            koin.get<ScheduleWaterRemindersUseCase>()()
        } else {
            koin.get<GetDueLogRemindersUseCase>()().forEach { LogReminderNotifications.post(context = appContext, message = it) }
        }
        koin.get<ScheduleLogRemindersUseCase>()()
    }
}

private val RESCHEDULE_ACTIONS = setOf(
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_MY_PACKAGE_REPLACED,
    AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
)

/** The buttons on a reminder that don't open the app, and Undo on what they did. */
class LogReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = runAsync { koin ->
        val appContext = context.applicationContext
        val kind = intent.getStringExtra(EXTRA_KIND)?.let { LogReminderKind.valueOf(value = it) } ?: return@runAsync
        val date = intent.getStringExtra(EXTRA_DATE)?.let { LocalDate.parse(input = it) } ?: return@runAsync
        val action = intent.getStringExtra(EXTRA_ACTION)?.let { LogReminderAction.valueOf(value = it) } ?: return@runAsync
        val actions = koin.get<LogReminderActions>()
        val done = Done(context = appContext, kind = kind, date = date)
        if (intent.getBooleanExtra(EXTRA_IS_UNDO, false)) {
            when (action) {
                LogReminderAction.SAME_AS_YESTERDAY -> actions.undoCopy(insertedIds = intent.getLongArrayExtra(EXTRA_IDS)?.toList().orEmpty())
                LogReminderAction.LATER, LogReminderAction.SKIP -> if (kind == LogReminderKind.WEIGHT) {
                    actions.restoreWeightLater(at = intent.getStringExtra(EXTRA_PREVIOUS)?.let { LocalDateTime.parse(input = it) })
                }
                else -> Unit
            }
            LogReminderNotifications.post(context = appContext, message = koin.get<GetLogReminderMessageUseCase>()(kind = kind, date = date))
            return@runAsync
        }
        val mealType = kind.mealType
        when (action) {
            LogReminderAction.SAME_AS_YESTERDAY -> {
                val logged = mealType?.let { actions.logSameAsYesterday(mealType = it, date = date) }
                if (logged == null) {
                    NotificationManagerCompat.from(appContext).cancel(LogReminderNotifications.id(kind = kind))
                    return@runAsync
                }
                val meal = getString(resource = mealType.title).lowercase()
                val first = logged.logs.first().foodName
                val calories = logged.logs.sumOf { it.calories }.withThousands()
                val text = if (logged.logs.size == 1) {
                    getString(resource = Res.string.reminder_done_logged, meal, first, calories)
                } else {
                    val more = logged.logs.size - 1
                    getPluralString(resource = Res.plurals.reminder_done_logged_more, quantity = more, meal, first, more, calories)
                }
                done.post(text = text, action = action) { it.putExtra(EXTRA_IDS, logged.insertedIds.toLongArray()) }
            }
            LogReminderAction.LATER -> {
                val (at, previous) = actions.remindLater()
                done.post(text = getString(resource = Res.string.reminder_done_later, formatClockTime(minutes = at.hour * 60 + at.minute)), action = action) {
                    it.putExtra(EXTRA_PREVIOUS, previous?.toString())
                }
            }
            LogReminderAction.SKIP -> if (mealType == null) {
                val previous = actions.skipWeight()
                done.post(text = getString(resource = Res.string.reminder_done_skipped_weight), action = action) {
                    it.putExtra(EXTRA_PREVIOUS, previous?.toString())
                }
            } else {
                actions.skipMeal()
                done.post(text = getString(resource = Res.string.reminder_done_skipped, getString(resource = mealType.title)), action = action)
            }
            LogReminderAction.KEEP_ON -> {
                actions.keepOn()
                done.post(text = getString(resource = Res.string.reminder_done_kept), action = null)
            }
            LogReminderAction.LOG, LogReminderAction.QUICK_ADD, LogReminderAction.CHANGE_TIMES -> Unit
        }
    }

    /** What a button did, shown in place of the reminder, with Undo when [action] can be undone. */
    private class Done(val context: Context, val kind: LogReminderKind, val date: LocalDate) {

        @android.annotation.SuppressLint("MissingPermission")
        suspend fun post(text: String, action: LogReminderAction?, undoExtras: (Intent) -> Unit = {}) {
            if (!context.canPostNotifications()) return
            val builder = NotificationCompat.Builder(context, LogReminderNotifications.channel(context = context))
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle(text)
                .setSilent(true)
                .setAutoCancel(true)
                .setTimeoutAfter(DONE_TIMEOUT_MILLIS)
                .setContentIntent(LogReminderNotifications.openIntent(context = context, kind = kind, date = date, action = LogReminderAction.LOG))
            if (action != null) {
                val undo = LogReminderNotifications.actionIntent(context = context, kind = kind, date = date, action = action, isUndo = true, extras = undoExtras)
                builder.addAction(0, getString(resource = Res.string.undo), undo)
            }
            NotificationManagerCompat.from(context).notify(LogReminderNotifications.id(kind = kind), builder.build())
        }
    }

    companion object {
        private const val DONE_TIMEOUT_MILLIS = 60_000L
    }
}

internal const val EXTRA_KIND = "log_reminder_kind"
internal const val EXTRA_DATE = "log_reminder_date"
internal const val EXTRA_ACTION = "log_reminder_action"
internal const val EXTRA_IS_UNDO = "log_reminder_undo"
internal const val EXTRA_IDS = "log_reminder_ids"
internal const val EXTRA_PREVIOUS = "log_reminder_previous"
/** On the intent that opens the app from a reminder: the screen to show, and the notification to take away. */
const val EXTRA_REMINDER_ROUTE = "log_reminder_route"
const val EXTRA_REMINDER_NOTIFICATION = "log_reminder_notification"

private fun Context.canPostNotifications(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasPermission(permission = Manifest.permission.POST_NOTIFICATIONS)

internal object LogReminderNotifications {

    private const val CHANNEL_ID = "log_reminders"
    private const val FIRST_ID = 4_310

    /** One notification per kind, so a new one replaces the last. */
    fun id(kind: LogReminderKind): Int = FIRST_ID + kind.ordinal

    suspend fun channel(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(resource = Res.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        return CHANNEL_ID
    }

    @android.annotation.SuppressLint("MissingPermission")
    suspend fun post(context: Context, message: LogReminderMessage) {
        if (!context.canPostNotifications()) return
        val builder = NotificationCompat.Builder(context, channel(context = context))
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openIntent(context = context, kind = message.kind, date = message.date, action = LogReminderAction.LOG))
        message.actions.forEach { (action, label) ->
            val intent = if (logReminderRoute(kind = message.kind, date = message.date, action = action) != null) {
                openIntent(context = context, kind = message.kind, date = message.date, action = action)
            } else {
                actionIntent(context = context, kind = message.kind, date = message.date, action = action, isUndo = false)
            }
            builder.addAction(0, label, intent)
        }
        NotificationManagerCompat.from(context).notify(id(kind = message.kind), builder.build())
    }

    /** Opens the app on the screen [action] leads to; Android doesn't let a broadcast open it any more. */
    fun openIntent(context: Context, kind: LogReminderKind, date: LocalDate, action: LogReminderAction): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_REMINDER_ROUTE, logReminderRoute(kind = kind, date = date, action = action))
            .putExtra(EXTRA_REMINDER_NOTIFICATION, id(kind = kind))
            .putExtra(EXTRA_ACTION, action.name)
        return PendingIntent.getActivity(
            context,
            requestCode(kind = kind, action = action, isUndo = false),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun actionIntent(
        context: Context,
        kind: LogReminderKind,
        date: LocalDate,
        action: LogReminderAction,
        isUndo: Boolean,
        extras: (Intent) -> Unit = {}
    ): PendingIntent {
        val intent = Intent(context, LogReminderActionReceiver::class.java)
            .setAction("${context.packageName}.log_reminder.${action.name}")
            .putExtra(EXTRA_KIND, kind.name)
            .putExtra(EXTRA_DATE, date.toString())
            .putExtra(EXTRA_ACTION, action.name)
            .putExtra(EXTRA_IS_UNDO, isUndo)
            .also(extras)
        return PendingIntent.getBroadcast(
            context,
            requestCode(kind = kind, action = action, isUndo = isUndo),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /** Different for every kind, button and Undo, so each keeps its own extras. */
    private fun requestCode(kind: LogReminderKind, action: LogReminderAction, isUndo: Boolean): Int =
        (kind.ordinal * LogReminderAction.entries.size + action.ordinal) * 2 + if (isUndo) 1 else 0
}
