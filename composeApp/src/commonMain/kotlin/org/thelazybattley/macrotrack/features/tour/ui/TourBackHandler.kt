package org.thelazybattley.macrotrack.features.tour.ui

import androidx.compose.runtime.Composable

/** Routes the system back gesture to the tour while its overlay is showing. */
@Composable
expect fun TourBackHandler(onBack: () -> Unit)
