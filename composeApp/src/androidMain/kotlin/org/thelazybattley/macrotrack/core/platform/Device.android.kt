package org.thelazybattley.macrotrack.core.platform

import android.os.Build

actual fun deviceName(): String = Build.MODEL.orEmpty().ifBlank { "Android" }
