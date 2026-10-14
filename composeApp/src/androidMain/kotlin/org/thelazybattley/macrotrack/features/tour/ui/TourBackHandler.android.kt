package org.thelazybattley.macrotrack.features.tour.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun TourBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
