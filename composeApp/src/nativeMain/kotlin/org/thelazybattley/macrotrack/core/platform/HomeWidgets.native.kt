package org.thelazybattley.macrotrack.core.platform

/** Before the app hands Kotlin its widget bridge, or in a build without the widget extension. */
object NoHomeWidgets : HomeWidgetRefresher {

    override val isSupported: Boolean = false

    override val platform: WidgetPlatform = WidgetPlatform.IOS

    override suspend fun refresh() = Unit
}
