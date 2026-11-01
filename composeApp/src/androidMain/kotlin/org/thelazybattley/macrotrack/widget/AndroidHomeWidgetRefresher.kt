package org.thelazybattley.macrotrack.widget

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import org.thelazybattley.macrotrack.core.platform.HomeWidgetRefresher
import org.thelazybattley.macrotrack.core.platform.WidgetPlatform

/** Every widget the app has, with its receiver. */
private val WIDGETS = listOf(
    TodayWidget() to TodayWidgetReceiver::class,
    WaterWidget() to WaterWidgetReceiver::class,
    WeightWidget() to WeightWidgetReceiver::class,
    StreakWidget() to StreakWidgetReceiver::class,
    QuickLogWidget() to QuickLogWidgetReceiver::class
)

class AndroidHomeWidgetRefresher(private val context: Context) : HomeWidgetRefresher {

    override val isSupported: Boolean = true

    override val platform: WidgetPlatform = WidgetPlatform.ANDROID

    override suspend fun refresh() {
        WIDGETS.forEach { (widget, _) -> widget.updateAll(context = context) }
        publishPreviews()
    }

    /**
     * Gives the widget list on Android 15 and later its pictures of each widget. The phone limits how often these
     * can be set, so they're set once for each version of the app.
     */
    private suspend fun publishPreviews() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val version = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        if (prefs.getLong(KEY_PREVIEWS_VERSION, -1) == version) return
        val manager = GlanceAppWidgetManager(context = context)
        val isPublished = WIDGETS.all { (_, receiver) ->
            runCatching { manager.setWidgetPreviews(receiver = receiver) }.getOrNull() == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
        }
        if (isPublished) prefs.edit { putLong(KEY_PREVIEWS_VERSION, version) }
    }

    private companion object {
        const val PREFS = "home_widgets"
        const val KEY_PREVIEWS_VERSION = "previews_version"
    }
}
