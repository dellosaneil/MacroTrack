package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene

@Composable
actual fun rememberReviewPrompt(): ReviewPrompt = remember { IosReviewPrompt() }

private class IosReviewPrompt : ReviewPrompt {

    override suspend fun request() {
        // iOS shows the dialog at most three times a year, and never in TestFlight builds.
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: return
        SKStoreReviewController.requestReviewInScene(windowScene = scene)
    }
}
