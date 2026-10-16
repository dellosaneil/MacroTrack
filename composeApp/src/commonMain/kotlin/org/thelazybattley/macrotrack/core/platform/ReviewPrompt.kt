package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable

/** The store's own "rate this app" dialog, shown over the app without leaving it. */
interface ReviewPrompt {

    /** Asks the store to show its rating dialog. The store may skip it, for example if it was shown recently. */
    suspend fun request()
}

@Composable
expect fun rememberReviewPrompt(): ReviewPrompt
