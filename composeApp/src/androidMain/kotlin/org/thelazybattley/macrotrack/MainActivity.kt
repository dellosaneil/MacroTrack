package org.thelazybattley.macrotrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.ShortcutManagerCompat
import org.koin.android.ext.android.get
import org.thelazybattley.macrotrack.core.platform.AppShortcut
import org.thelazybattley.macrotrack.core.platform.EXTRA_ACTION
import org.thelazybattley.macrotrack.core.platform.EXTRA_APP_SHORTCUT
import org.thelazybattley.macrotrack.core.platform.EXTRA_REMINDER_NOTIFICATION
import org.thelazybattley.macrotrack.core.platform.EXTRA_REMINDER_ROUTE
import org.thelazybattley.macrotrack.core.platform.LogReminderAction
import org.thelazybattley.macrotrack.domain.usecase.reminder.LogReminderActions
import org.thelazybattley.macrotrack.features.navigation.AppBottomNav
import org.thelazybattley.macrotrack.features.reminders.NotificationRouteRequest
import org.thelazybattley.macrotrack.features.shortcuts.appShortcutRoute
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.widget.EXTRA_WIDGET_ROUTE

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // A recreated activity keeps the intent it started with; the reminder or shortcut was already handled then.
        if (savedInstanceState == null) {
            openReminder(intent = intent)
            openShortcut(intent = intent)
            openWidgetRoute(intent = intent)
        }
        setContent {
            MacroTrackTheme() {
                AppBottomNav()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openReminder(intent = intent)
        openShortcut(intent = intent)
        openWidgetRoute(intent = intent)
    }

    /** A tapped missed-log reminder: takes it away and asks for the screen it leads to. */
    private fun openReminder(intent: Intent) {
        val route = intent.getStringExtra(EXTRA_REMINDER_ROUTE) ?: return
        NotificationManagerCompat.from(this).cancel(intent.getIntExtra(EXTRA_REMINDER_NOTIFICATION, 0))
        intent.removeExtra(EXTRA_REMINDER_ROUTE)
        intent.getStringExtra(EXTRA_ACTION)?.let { name -> LogReminderAction.entries.find { it.name == name } }?.let { action ->
            get<LogReminderActions>().opened(action = action)
        }
        get<NotificationRouteRequest>().request(route = route)
    }

    /** A shortcut tapped on the app icon: asks for its screen, worked out for the time it was tapped. */
    private fun openShortcut(intent: Intent) {
        val shortcut = AppShortcut.of(id = intent.getStringExtra(EXTRA_APP_SHORTCUT)) ?: return
        intent.removeExtra(EXTRA_APP_SHORTCUT)
        ShortcutManagerCompat.reportShortcutUsed(this, shortcut.name)
        get<NotificationRouteRequest>().request(route = appShortcutRoute(shortcut = shortcut))
    }

    /** A tapped meal on the Today widget: asks for Add meal for it. */
    private fun openWidgetRoute(intent: Intent) {
        val route = intent.getStringExtra(EXTRA_WIDGET_ROUTE) ?: return
        intent.removeExtra(EXTRA_WIDGET_ROUTE)
        get<NotificationRouteRequest>().request(route = route)
    }
}
