package org.thelazybattley.macrotrack.features.tour.ui

import androidx.compose.runtime.Composable

/** iOS has no system back button; the tour's own Back button covers it. */
@Composable
actual fun TourBackHandler(onBack: () -> Unit) = Unit
