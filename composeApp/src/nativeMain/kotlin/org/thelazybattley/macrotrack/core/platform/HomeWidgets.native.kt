package org.thelazybattley.macrotrack.core.platform

/** iOS has no widgets yet; they need a WidgetKit extension in the Xcode project. */
object NoHomeWidgets : HomeWidgetRefresher {

    override val isSupported: Boolean = false

    override suspend fun refresh() = Unit
}
