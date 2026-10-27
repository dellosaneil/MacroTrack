package org.thelazybattley.macrotrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import org.koin.android.ext.android.get
import org.thelazybattley.macrotrack.core.platform.EXTRA_ACTION
import org.thelazybattley.macrotrack.core.platform.EXTRA_REMINDER_NOTIFICATION
import org.thelazybattley.macrotrack.core.platform.EXTRA_REMINDER_ROUTE
import org.thelazybattley.macrotrack.core.platform.LogReminderAction
import org.thelazybattley.macrotrack.domain.usecase.reminder.LogReminderActions
import org.thelazybattley.macrotrack.features.navigation.AppBottomNav
import org.thelazybattley.macrotrack.features.reminders.NotificationRouteRequest
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // A recreated activity keeps the intent it started with; the reminder was already handled then.
        if (savedInstanceState == null) openReminder(intent = intent)
        setContent {
            MacroTrackTheme() {
                AppBottomNav()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openReminder(intent = intent)
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
}
