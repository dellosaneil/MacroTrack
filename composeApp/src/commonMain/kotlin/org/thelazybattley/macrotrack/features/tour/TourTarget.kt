package org.thelazybattley.macrotrack.features.tour

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/** Where each on-screen [TourTarget] sits, in root coordinates, so the overlay can cut a spotlight around it. */
@Stable
class TourTargetRegistry {
    var activeTarget: TourTarget? by mutableStateOf(value = null)
    private val bounds = mutableStateMapOf<TourTarget, Rect>()

    /** The visible bounds of [target], or null when it is not on screen. */
    fun boundsOf(target: TourTarget): Rect? = bounds[target]?.takeIf { it.width > 0f && it.height > 0f }

    internal fun update(target: TourTarget, rect: Rect) {
        if (bounds[target] != rect) bounds[target] = rect
    }

    internal fun remove(target: TourTarget) {
        bounds.remove(key = target)
    }
}

val LocalTourTargets = staticCompositionLocalOf<TourTargetRegistry?> { null }

/** Marks this element as [target]; while its step is active it is scrolled into view and spotlighted. */
fun Modifier.tourTarget(target: TourTarget): Modifier = composed {
    val registry = LocalTourTargets.current ?: return@composed Modifier
    val requester = remember { BringIntoViewRequester() }
    val isActive = registry.activeTarget == target
    LaunchedEffect(key1 = isActive) {
        if (isActive) requester.bringIntoView()
    }
    DisposableEffect(key1 = target) {
        onDispose { registry.remove(target = target) }
    }
    Modifier
        .bringIntoViewRequester(bringIntoViewRequester = requester)
        .onGloballyPositioned { coordinates -> registry.update(target = target, rect = coordinates.boundsInRoot()) }
}
