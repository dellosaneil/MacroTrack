package org.thelazybattley.macrotrack.core.platform

/** The app's widgets on the phone's home screen, which redraw from the database when asked. */
interface HomeWidgetRefresher {

    /** False where the app has no widgets yet; Profile then leaves out the widgets screen. */
    val isSupported: Boolean

    /** Whose steps the widgets screen shows, and whether it offers the Lock Screen setting. */
    val platform: WidgetPlatform

    /** Redraws every placed widget with what's logged now. */
    suspend fun refresh()
}

enum class WidgetPlatform { ANDROID, IOS }
