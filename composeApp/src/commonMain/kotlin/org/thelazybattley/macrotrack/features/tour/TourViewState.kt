package org.thelazybattley.macrotrack.features.tour

enum class TourOutcome {
    FINISHED,
    SKIPPED
}

data class TourViewState(
    /** The step on screen, or null while no tour is running. */
    val step: TourStep? = null,
    /** How the last tour ended; shown as a short message with a Replay action. */
    val outcome: TourOutcome? = null
)
