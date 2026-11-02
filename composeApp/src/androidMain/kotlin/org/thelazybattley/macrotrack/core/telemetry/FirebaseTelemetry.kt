package org.thelazybattley.macrotrack.core.telemetry

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseTelemetry(context: Context) : Telemetry {

    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    // Play's pre-launch report and Firebase Test Lab run the app on robot devices; their taps are not real usage.
    private val isTestLab = Settings.System.getString(context.contentResolver, "firebase.test.lab") == "true"

    init {
        if (isTestLab) analytics.setAnalyticsCollectionEnabled(false)
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        analytics.logEvent(name, params.toBundle())
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCollectionEnabled(crashReports: Boolean, analytics: Boolean) {
        this.analytics.setAnalyticsCollectionEnabled(analytics && !isTestLab)
        crashlytics.isCrashlyticsCollectionEnabled = crashReports
    }

    private fun Map<String, Any>.toBundle() = Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Float -> putDouble(key, value.toDouble())
                // Firebase has no boolean parameters, so they go as 1 or 0.
                is Boolean -> putLong(key, if (value) 1 else 0)
                else -> putString(key, value.toString())
            }
        }
    }

    companion object {
        /** Firebase starts itself only when the build had a google-services.json. */
        fun create(context: Context): Telemetry =
            if (FirebaseApp.getApps(context).isEmpty()) NoOpTelemetry else FirebaseTelemetry(context = context)
    }
}
