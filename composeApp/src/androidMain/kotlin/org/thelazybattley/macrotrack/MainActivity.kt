package org.thelazybattley.macrotrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.android.ext.android.inject
import org.thelazybattley.macrotrack.core.platform.ActivityRecordingService
import org.thelazybattley.macrotrack.core.platform.AndroidStepNotifier
import org.thelazybattley.macrotrack.core.platform.AppLink
import org.thelazybattley.macrotrack.core.platform.AppLinks
import org.thelazybattley.macrotrack.features.activity.ActivityRecorder
import org.thelazybattley.macrotrack.features.navigation.AppBottomNav
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

class MainActivity : ComponentActivity() {

    private val recorder: ActivityRecorder by inject()
    private val appLinks: AppLinks by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MacroTrackTheme() {
                AppBottomNav()
            }
        }
        // Not again after a rotation, which hands back the same intent.
        if (savedInstanceState == null) openFrom(intent = intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openFrom(intent = intent)
    }

    private fun openFrom(intent: Intent) {
        openRecordingFrom(intent = intent)
        // Buttons on the steps notification and step alerts.
        intent.getStringExtra(AndroidStepNotifier.EXTRA_APP_LINK)
            ?.let { name -> AppLink.entries.find { it.name == name } }
            ?.let(appLinks::open)
    }

    /** Open or Finish on the recording notification shows the live map, with the save sheet up for Finish. */
    private fun openRecordingFrom(intent: Intent) {
        when (intent.getStringExtra(ActivityRecordingService.EXTRA_OPEN_RECORDING)) {
            ActivityRecordingService.OPEN_MAP -> recorder.requestOpen()
            ActivityRecordingService.OPEN_FINISH -> recorder.requestOpen(finish = true)
        }
    }
}
