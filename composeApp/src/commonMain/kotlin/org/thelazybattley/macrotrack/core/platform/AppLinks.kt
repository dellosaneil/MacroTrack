package org.thelazybattley.macrotrack.core.platform

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** A screen opened from outside the app, like a button on the steps notification. */
enum class AppLink {
    STEPS,
    RECORD_WALK
}

/** Hands links from the platform to the navigation, which opens them once first launch is out of the way. */
class AppLinks {

    private val _requests = Channel<AppLink>(capacity = Channel.CONFLATED)

    val requests: Flow<AppLink> = _requests.receiveAsFlow()

    fun open(link: AppLink) {
        _requests.trySend(element = link)
    }
}
