package org.thelazybattley.macrotrack.core

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun getCurrentDate() = run {
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
}
