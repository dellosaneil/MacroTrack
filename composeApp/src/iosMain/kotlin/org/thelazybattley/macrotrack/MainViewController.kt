package org.thelazybattley.macrotrack

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.dsl.module
import org.thelazybattley.macrotrack.core.di.initKoin
import org.thelazybattley.macrotrack.core.telemetry.BridgedTelemetry
import org.thelazybattley.macrotrack.core.telemetry.FirebaseBridge
import org.thelazybattley.macrotrack.core.telemetry.NoOpTelemetry
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.features.navigation.AppBottomNav
import kotlin.experimental.ExperimentalNativeApi

/** [firebase] is null when the app was built without the Firebase SDK or its GoogleService-Info.plist. */
@OptIn(ExperimentalNativeApi::class)
fun MainViewController(firebase: FirebaseBridge?) = ComposeUIViewController(
    configure = {
        val telemetry = firebase?.let(::BridgedTelemetry) ?: NoOpTelemetry
        initKoin(extraModules = listOf(module { single<Telemetry> { telemetry } }))
        // An uncaught Kotlin exception ends the app as a bare native crash, so send its Kotlin stack trace first.
        setUnhandledExceptionHook { throwable ->
            telemetry.recordException(throwable = throwable)
            terminateWithUnhandledException(throwable)
        }
    }
) { AppBottomNav() }
