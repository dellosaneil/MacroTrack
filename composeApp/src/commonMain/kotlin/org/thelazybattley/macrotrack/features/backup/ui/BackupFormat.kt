package org.thelazybattley.macrotrack.features.backup.ui

import androidx.compose.runtime.Composable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.size_kb
import macrotrack.composeapp.generated.resources.size_mb
import macrotrack.composeapp.generated.resources.time_am
import macrotrack.composeapp.generated.resources.time_date_at
import macrotrack.composeapp.generated.resources.time_pm
import macrotrack.composeapp.generated.resources.time_today_at
import macrotrack.composeapp.generated.resources.time_yesterday_at
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** "Today, 9:12 AM", "Yesterday, 2:04 AM" or "Oct 14, 2:03 AM", in this phone's time zone. */
@OptIn(ExperimentalTime::class)
@Composable
internal fun formatBackupTime(millis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMilliseconds = millis).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
    val hour = dateTime.hour % 12
    val minute = dateTime.minute.toString().padStart(length = 2, padChar = '0')
    val time = stringResource(
        resource = if (dateTime.hour < 12) Res.string.time_am else Res.string.time_pm,
        if (hour == 0) 12 else hour,
        minute
    )
    val today = getCurrentDate()
    return when (dateTime.date) {
        today -> stringResource(resource = Res.string.time_today_at, time)
        today.minus(period = DatePeriod(days = 1)) -> stringResource(resource = Res.string.time_yesterday_at, time)
        else -> stringResource(resource = Res.string.time_date_at, dateTime.date.toAbbreviatedMonthDay(), time)
    }
}

/** "840 KB" or "3.2 MB". */
@Composable
internal fun formatFileSize(bytes: Int): String {
    val kilobytes = bytes / 1024.0
    return if (kilobytes < 1024) {
        stringResource(resource = Res.string.size_kb, kilobytes.roundToInt().coerceAtLeast(minimumValue = 1).toString())
    } else {
        stringResource(resource = Res.string.size_mb, ((kilobytes / 1024) * 10).roundToInt().div(10.0).toString())
    }
}
