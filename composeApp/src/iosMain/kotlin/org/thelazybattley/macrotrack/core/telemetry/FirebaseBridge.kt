package org.thelazybattley.macrotrack.core.telemetry

import kotlin.experimental.ExperimentalNativeApi

/**
 * Implemented in Swift on top of the Firebase iOS SDK, which Kotlin cannot call directly. Only plain types cross
 * over, so the Swift side never has to unpack Kotlin objects.
 */
interface FirebaseBridge {

    fun logEvent(name: String, params: Map<String, Any>)

    /** [stackTrace] holds one frame per line, as Kotlin prints them. */
    fun recordError(name: String, reason: String, stackTrace: List<String>)

    fun log(message: String)

    fun setCollectionEnabled(crashReports: Boolean, analytics: Boolean)
}

class BridgedTelemetry(private val bridge: FirebaseBridge) : Telemetry {

    override fun logEvent(name: String, params: Map<String, Any>) = bridge.logEvent(
        name = name,
        // Firebase has no boolean parameters, so they go as 1 or 0.
        params = params.mapValues { (_, value) -> if (value is Boolean) (if (value) 1 else 0) else value }
    )

    @OptIn(ExperimentalNativeApi::class)
    override fun recordException(throwable: Throwable) = bridge.recordError(
        name = throwable::class.qualifiedName ?: "Throwable",
        reason = throwable.message.orEmpty(),
        stackTrace = throwable.getStackTrace().toList()
    )

    override fun log(message: String) = bridge.log(message = message)

    override fun setCollectionEnabled(crashReports: Boolean, analytics: Boolean) =
        bridge.setCollectionEnabled(crashReports = crashReports, analytics = analytics)
}
