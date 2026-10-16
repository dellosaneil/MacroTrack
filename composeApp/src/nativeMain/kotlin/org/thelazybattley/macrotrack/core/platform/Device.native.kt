package org.thelazybattley.macrotrack.core.platform

import platform.UIKit.UIDevice

actual fun deviceName(): String = UIDevice.currentDevice.model.ifBlank { "iPhone" }
