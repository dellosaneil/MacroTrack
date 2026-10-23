package org.thelazybattley.macrotrack.core.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.METERS_PER_KM
import org.thelazybattley.macrotrack.domain.model.METERS_PER_MILE
import org.thelazybattley.macrotrack.domain.model.StepNotificationExtra
import org.thelazybattley.macrotrack.domain.model.StepNotificationSettings
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetDayActivityUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.recordStepCount
import org.thelazybattley.macrotrack.domain.usecase.pro.GetProStatusUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.features.activity.ActivityRecorder
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

/** Waits this long for the step counter's first reading before showing what's already saved. */
private const val SENSOR_READ_TIMEOUT_MILLIS = 5_000L

/**
 * The silent steps notification, in its ordinary or steps-only form, and the goal-reached alert. An inexact alarm
 * runs [StepNotificationReceiver] about every 15 minutes while either is on; it doesn't wake the phone, so the
 * numbers catch up when the screen comes on. While a walk or run records, its notification takes this one's place.
 */
class AndroidStepNotifier(
    private val context: Context,
    private val repository: ActivityRepository,
    private val recorder: ActivityRecorder,
    private val getDayActivityUseCase: GetDayActivityUseCase,
    private val getBurnProfileUseCase: GetBurnProfileUseCase,
    private val getProStatusUseCase: GetProStatusUseCase,
    private val getUnitSystemUseCase: GetUnitSystemUseCase
) : StepNotifier {

    private val mutex = Mutex()
    private val manager = NotificationManagerCompat.from(context)

    override suspend fun refresh() = mutex.withLock {
        val settings = repository.getStepNotificationSettings().first()
        // With counting off there are no new steps to show or celebrate.
        val isCounting = repository.isStepCountingOn().first()
        scheduleRefresh(isEnabled = isCounting && (settings.isOn || settings.isGoalAlertOn))
        if (!isCounting) {
            manager.cancel(STEPS_NOTIFICATION_ID)
            return@withLock
        }
        if (!canPost()) return@withLock
        createChannels()
        val today = getCurrentDate()
        val goal = repository.getStepGoal().first()
        val week = repository.getStepHours(fromDate = today.minus(period = DatePeriod(days = 6))).first()
        val todayHours = week.filter { it.date == today }
        val steps = todayHours.sumOf { it.steps }
        if (settings.isGoalAlertOn && steps >= goal && repository.getGoalAlertSentOn() != today) {
            repository.setGoalAlertSentOn(date = today)
            val daysHit = week.groupBy { it.date }.count { (_, hours) -> hours.sumOf { it.steps } >= goal }
            postGoalAlert(goal = goal, daysHit = daysHit)
        }
        val isHidden = repository.getStepNotificationHiddenOn() == today
        if (!settings.isOn || isHidden || recorder.state.value != null) {
            manager.cancel(STEPS_NOTIFICATION_ID)
            return@withLock
        }
        val hours = List(size = 24) { hour -> todayHours.filter { it.hour == hour }.sumOf { it.steps } }
        postSteps(settings = settings, steps = steps, goal = goal, hours = hours)
    }

    @android.annotation.SuppressLint("MissingPermission")
    private suspend fun postSteps(settings: StepNotificationSettings, steps: Int, goal: Int, hours: List<Int>) {
        val left = (goal - steps).coerceAtLeast(minimumValue = 0)
        val toGoal = if (left == 0) {
            context.getString(R.string.steps_goal_reached, goal.formatted())
        } else {
            context.getString(R.string.steps_to_goal, left.formatted(), goal.formatted())
        }
        val builder = NotificationCompat.Builder(context, STEPS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setColor(accentColor())
            .setContentIntent(linkIntent(link = AppLink.STEPS, requestCode = REQUEST_OPEN))
            .setDeleteIntent(receiverIntent(action = StepNotificationReceiver.ACTION_DISMISSED, requestCode = REQUEST_DISMISSED))
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(if (settings.isOnLockScreen) NotificationCompat.VISIBILITY_PUBLIC else NotificationCompat.VISIBILITY_SECRET)
            .addAction(0, context.getString(R.string.steps_action_open), linkIntent(link = AppLink.STEPS, requestCode = REQUEST_OPEN))
        val title = context.getString(R.string.steps_title, steps.formatted())
        val text = when (val extra = extraText(extra = settings.extra)) {
            null -> toGoal
            else -> if (left == 0) context.getString(R.string.steps_goal_reached, extra) else context.getString(R.string.steps_to_go_extra, left.formatted(), extra)
        }
        builder
            .setContentTitle(title)
            .setContentText(text)
            .setLargeIcon(ringBitmap(percent = if (goal > 0) steps * 100 / goal else 0))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(expandedView(title = title, text = text, hours = hours))
            .addAction(0, context.getString(R.string.steps_action_hide), receiverIntent(action = StepNotificationReceiver.ACTION_HIDE_TODAY, requestCode = REQUEST_HIDE))
            .addAction(0, context.getString(R.string.steps_action_walk), linkIntent(link = AppLink.RECORD_WALK, requestCode = REQUEST_WALK))
        manager.notify(STEPS_NOTIFICATION_ID, builder.build())
    }

    /** The second number: distance, or calories burned with Pro once the profile is set up. Null for steps to goal. */
    private suspend fun extraText(extra: StepNotificationExtra): String? {
        if (extra == StepNotificationExtra.STEPS_TO_GOAL) return null
        val today = getCurrentDate()
        val day = getDayActivityUseCase(date = today).first()
        val profile = getBurnProfileUseCase().first()
        if (extra == StepNotificationExtra.CALORIES && profile != null && getProStatusUseCase().first().isActive) {
            return context.getString(R.string.steps_kcal_burned, day.activeKcal(profile = profile).formatted())
        }
        val isImperial = getUnitSystemUseCase().first() == UnitSystem.IMPERIAL
        val distance = day.distanceMeters(profile = profile) / (if (isImperial) METERS_PER_MILE else METERS_PER_KM)
        return context.getString(
            R.string.steps_distance,
            String.format(Locale.getDefault(), "%.2f", distance),
            context.getString(if (isImperial) R.string.recording_unit_mi else R.string.recording_unit_km)
        )
    }

    private fun expandedView(title: String, text: String, hours: List<Int>): RemoteViews {
        val now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val busiest = hours.withIndex().filter { it.value > 0 }.maxByOrNull { it.value }?.index
        return RemoteViews(context.packageName, R.layout.notification_steps_expanded).apply {
            setTextViewText(R.id.steps_title, title)
            setTextViewText(R.id.steps_text, text)
            setImageViewBitmap(R.id.steps_chart, chartBitmap(hours = hours.take(n = now + 1), busiest = busiest))
            setTextViewText(R.id.steps_chart_start, context.getString(R.string.steps_chart_start))
            setTextViewText(R.id.steps_chart_busiest, busiest?.let { context.getString(R.string.steps_chart_busiest, hourLabel(hour = it)) }.orEmpty())
            setTextViewText(R.id.steps_chart_now, context.getString(R.string.steps_chart_now))
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun postGoalAlert(goal: Int, daysHit: Int) {
        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setColor(accentColor())
            .setContentTitle(context.getString(R.string.goal_alert_title, goal.formatted()))
            .setContentText(context.getString(R.string.goal_alert_body, daysHit))
            .setContentIntent(linkIntent(link = AppLink.STEPS, requestCode = REQUEST_GOAL))
            .setAutoCancel(true)
            .build()
        manager.notify(GOAL_NOTIFICATION_ID, notification)
    }

    private fun scheduleRefresh(isEnabled: Boolean) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = receiverIntent(action = StepNotificationReceiver.ACTION_REFRESH, requestCode = REQUEST_REFRESH)
        alarmManager.cancel(intent)
        if (!isEnabled) return
        val interval = StepNotificationSettings.REFRESH_MINUTES * 60_000L
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, intent)
    }

    private fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || context.hasPermission(permission = Manifest.permission.POST_NOTIFICATIONS)

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(STEPS_CHANNEL_ID, context.getString(R.string.steps_channel), NotificationManager.IMPORTANCE_LOW).apply {
                setShowBadge(false)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(ALERTS_CHANNEL_ID, context.getString(R.string.step_alerts_channel), NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun linkIntent(link: AppLink, requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_APP_LINK, link.name),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun receiverIntent(action: String, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(context, StepNotificationReceiver::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun isNight(): Boolean =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private fun accentColor(): Int = if (isNight()) ACCENT_DARK else ACCENT_LIGHT

    /** Today's progress as a ring with the percentage in the middle, for the collapsed notification. */
    private fun ringBitmap(percent: Int): Bitmap {
        val size = 144
        val stroke = 18f
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = RectF(stroke / 2, stroke / 2, size - stroke / 2, size - stroke / 2)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        }
        paint.color = TRACK_COLOR
        canvas.drawArc(bounds, 0f, 360f, false, paint)
        paint.color = accentColor()
        canvas.drawArc(bounds, -90f, 360f * percent.coerceIn(minimumValue = 0, maximumValue = 100) / 100, false, paint)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isNight()) INK_DARK else INK_LIGHT
            textSize = 34f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(context.getString(R.string.steps_percent, percent), size / 2f, size / 2f - (text.descent() + text.ascent()) / 2, text)
        return bitmap
    }

    /** Steps by hour so far today, the busiest hour in the accent color. */
    private fun chartBitmap(hours: List<Int>, busiest: Int?): Bitmap {
        val width = 720
        val height = 88
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val max = hours.maxOrNull()?.takeIf { it > 0 } ?: 1
        val gap = 4f
        val barWidth = (width - gap * (hours.size - 1)) / hours.size.coerceAtLeast(minimumValue = 1)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        hours.forEachIndexed { hour, steps ->
            val barHeight = (steps.toFloat() / max * height).coerceAtLeast(minimumValue = 4f)
            val left = hour * (barWidth + gap)
            paint.color = if (hour == busiest) accentColor() else TRACK_COLOR
            canvas.drawRoundRect(RectF(left, height - barHeight, left + barWidth, height.toFloat()), 4f, 4f, paint)
        }
        return bitmap
    }

    /** "7 AM". */
    private fun hourLabel(hour: Int): String =
        String.format(Locale.US, "%d %s", if (hour % 12 == 0) 12 else hour % 12, if (hour < 12) "AM" else "PM")

    private fun Int.formatted(): String = NumberFormat.getIntegerInstance().format(this)

    companion object {
        /** Set on the intent that opens the app from a notification button: an [AppLink] name. */
        const val EXTRA_APP_LINK = "app_link"

        private const val STEPS_CHANNEL_ID = "steps"
        private const val ALERTS_CHANNEL_ID = "step_alerts"
        private const val STEPS_NOTIFICATION_ID = 43
        private const val GOAL_NOTIFICATION_ID = 44
        private const val REQUEST_OPEN = 10
        private const val REQUEST_HIDE = 11
        private const val REQUEST_WALK = 12
        private const val REQUEST_DISMISSED = 14
        private const val REQUEST_REFRESH = 15
        private const val REQUEST_GOAL = 16

        private const val ACCENT_LIGHT = 0xFF0E6E52.toInt()
        private const val ACCENT_DARK = 0xFF3FBF8F.toInt()
        private const val INK_LIGHT = 0xFF15171A.toInt()
        private const val INK_DARK = 0xFFEEEDE8.toInt()

        /** A grey that reads on both light and dark notification shades. */
        private const val TRACK_COLOR = 0x668A8D93
    }
}

/**
 * The steps notification's refresh alarm, Hide today, and swiping it away. Hiding lasts until tomorrow; the refresh
 * reads the step counter first, so steps taken with the app closed count.
 */
class StepNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                handle(context = context.applicationContext, action = intent.action)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, action: String?) {
        val koin = GlobalContext.getOrNull() ?: return
        val repository = koin.get<ActivityRepository>()
        when (action) {
            ACTION_HIDE_TODAY, ACTION_DISMISSED -> repository.setStepNotificationHiddenOn(date = getCurrentDate())
            ACTION_REFRESH -> {
                val sensor = koin.get<StepSensor>()
                if (sensor.isAvailable && context.canCountSteps() && repository.isStepCountingOn().first()) {
                    withTimeoutOrNull(timeMillis = SENSOR_READ_TIMEOUT_MILLIS) { sensor.counts().first() }?.let { count ->
                        repository.recordStepCount(count = count, atMillis = System.currentTimeMillis())
                    }
                }
            }
        }
        koin.get<StepNotifier>().refresh()
    }

    companion object {
        const val ACTION_REFRESH = "org.thelazybattley.macrotrack.steps.REFRESH"
        const val ACTION_HIDE_TODAY = "org.thelazybattley.macrotrack.steps.HIDE_TODAY"
        const val ACTION_DISMISSED = "org.thelazybattley.macrotrack.steps.DISMISSED"
    }
}

/** Activity recognition is a runtime permission from Android 10; before that it comes with the app. */
internal fun Context.canCountSteps(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || hasPermission(permission = Manifest.permission.ACTIVITY_RECOGNITION)
