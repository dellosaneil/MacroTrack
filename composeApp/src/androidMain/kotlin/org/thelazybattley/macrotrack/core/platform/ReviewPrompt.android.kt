package org.thelazybattley.macrotrack.core.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory

@Composable
actual fun rememberReviewPrompt(): ReviewPrompt {
    val context = LocalContext.current
    return remember(context) { PlayReviewPrompt(context = context) }
}

private class PlayReviewPrompt(private val context: Context) : ReviewPrompt {

    override suspend fun request() {
        val activity = context.findActivity() ?: return
        // Play only shows the dialog for installs from the Play Store and within its own quota; anything else fails
        // quietly, which is fine since the rating is a nice-to-have.
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
            manager.launchReviewFlow(activity, reviewInfo)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
