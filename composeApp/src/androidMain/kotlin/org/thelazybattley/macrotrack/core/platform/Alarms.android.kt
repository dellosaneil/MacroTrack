package org.thelazybattley.macrotrack.core.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build

/** Whether reminders can go off on the minute. Android 14 and later leave this off until it's allowed in settings. */
internal fun Context.canSetExactAlarms(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

/**
 * Sets [intent] to go off at [atMillis], even in Doze. An inexact alarm can come many minutes late, so it's exact
 * whenever the phone allows; otherwise the inexact one is still better than none.
 */
internal fun Context.setReminderAlarm(atMillis: Long, intent: PendingIntent) {
    val alarmManager = getSystemService(AlarmManager::class.java)
    if (canSetExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
    } else {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
    }
}
