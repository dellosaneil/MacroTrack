package org.thelazybattley.macrotrack.core.platform

/** The app's widgets on the phone's home screen, which redraw from the database when asked. */
interface HomeWidgetRefresher {

    /** False where the app has no widgets yet; Profile then leaves out the widgets screen. */
    val isSupported: Boolean

    /** Redraws every placed widget with what's logged now. */
    suspend fun refresh()
}
