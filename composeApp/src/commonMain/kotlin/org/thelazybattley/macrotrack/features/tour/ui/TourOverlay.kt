package org.thelazybattley.macrotrack.features.tour.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_star
import macrotrack.composeapp.generated.resources.tour_finish
import macrotrack.composeapp.generated.resources.tour_finished_message
import macrotrack.composeapp.generated.resources.tour_next
import macrotrack.composeapp.generated.resources.tour_not_now
import macrotrack.composeapp.generated.resources.tour_replay
import macrotrack.composeapp.generated.resources.tour_skip
import macrotrack.composeapp.generated.resources.tour_skipped_message
import macrotrack.composeapp.generated.resources.tour_start
import macrotrack.composeapp.generated.resources.tour_start_logging
import macrotrack.composeapp.generated.resources.tour_step_of
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.tour.TourCallbacks
import org.thelazybattley.macrotrack.features.tour.TourOutcome
import org.thelazybattley.macrotrack.features.tour.TourStep
import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.TourTargetRegistry
import org.thelazybattley.macrotrack.features.tour.TourViewState
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private val SpotPadding = 6.dp
private val CardMargin = 16.dp
private val CardGap = 16.dp
private val CaretSize = 16.dp
private const val OUTCOME_MESSAGE_MILLIS = 6_000L

/**
 * Draws the first-run tour over the whole app: a dimmed screen with a cut-out around the step's
 * target (see [TourTargetRegistry]), a card that points at it, and a Skip button. Once the tour ends it shows a
 * short message with a Replay action.
 */
@Composable
fun TourOverlay(
    modifier: Modifier = Modifier,
    viewState: TourViewState,
    targets: TourTargetRegistry,
    callbacks: TourCallbacks
) {
    Box(modifier = modifier.fillMaxSize()) {
        viewState.step?.let { step ->
            val target = targets.activeTarget
            TourSpotlight(
                step = step,
                target = target,
                targetBounds = target?.let { targets.boundsOf(target = it) },
                callbacks = callbacks
            )
        }
        AnimatedVisibility(
            visible = viewState.outcome != null,
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            viewState.outcome?.let { outcome ->
                OutcomeMessage(outcome = outcome, callbacks = callbacks)
            }
        }
    }
}

@Composable
private fun TourSpotlight(
    step: TourStep,
    target: TourTarget?,
    targetBounds: Rect?,
    callbacks: TourCallbacks
) {
    val density = LocalDensity.current
    val isDark = isSystemInDarkTheme()
    val scrimColor = if (isDark) Color.Black.copy(alpha = 0.72f) else Color(0xFF0C0E10).copy(alpha = 0.66f)
    val ringColor = if (isDark) colors.accentInk else Color.White
    val spot = targetBounds?.let { bounds ->
        with(density) { bounds.inflate(delta = SpotPadding.toPx()) }
    }
    val cornerRadius = target?.cornerRadius ?: 0.dp

    // Glide the cut-out from one target to the next instead of jumping.
    val animatedSpot = remember { Animatable(initialValue = Rect.Zero, typeConverter = Rect.VectorConverter) }
    LaunchedEffect(key1 = spot) {
        if (spot == null) return@LaunchedEffect
        if (animatedSpot.value == Rect.Zero) {
            animatedSpot.snapTo(targetValue = spot)
        } else {
            animatedSpot.animateTo(targetValue = spot, animationSpec = tween(durationMillis = 300))
        }
    }
    val spotAlpha by animateFloatAsState(targetValue = if (spot != null) 1f else 0f)

    TourBackHandler {
        if (step.isFirst) callbacks.onSkipClicked() else callbacks.onBackClicked()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // The app underneath stays visible but does not react to taps during the tour.
            .pointerInput(key1 = Unit) { detectTapGestures { } }
    ) {
        val isCardBelow = spot != null && spot.center.y < constraints.maxHeight / 2f
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            drawRect(color = scrimColor)
            if (spotAlpha > 0f) {
                val rect = animatedSpot.value
                val radius = CornerRadius(x = cornerRadius.toPx())
                drawRoundRect(
                    color = Color.Black,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = radius,
                    alpha = spotAlpha,
                    blendMode = BlendMode.Clear
                )
                drawRoundRect(
                    color = ringColor,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = radius,
                    alpha = spotAlpha,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        CardPlacement(spot = spot, isCardBelow = isCardBelow) {
            TourCard(
                step = step,
                caretTargetX = spot?.center?.x,
                isCaretOnTop = isCardBelow,
                callbacks = callbacks
            )
        }

        if (!step.isFirst && !step.isLast) {
            SkipButton(spot = spot, onClick = callbacks::onSkipClicked)
        }
    }
}

/** Puts the card below the spot, above it, or in the middle of the screen when there is no spot. */
@Composable
private fun CardPlacement(
    spot: Rect?,
    isCardBelow: Boolean,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val insets = WindowInsets.safeDrawing
    Layout(modifier = Modifier.fillMaxSize(), content = content) { measurables, constraints ->
        val margin = CardMargin.roundToPx()
        val gap = CardGap.roundToPx()
        val height = constraints.maxHeight
        val placeable = measurables.first().measure(
            constraints = Constraints(
                minWidth = constraints.maxWidth - margin * 2,
                maxWidth = constraints.maxWidth - margin * 2,
                maxHeight = height
            )
        )
        val minY = insets.getTop(density = density) + margin
        val maxY = (height - insets.getBottom(density = density) - margin - placeable.height).coerceAtLeast(minimumValue = minY)
        val y = when {
            spot == null -> (height - placeable.height) / 2
            isCardBelow -> (spot.bottom + gap).toInt()
            else -> (spot.top - gap).toInt() - placeable.height
        }.coerceIn(minimumValue = minY, maximumValue = maxY)
        layout(width = constraints.maxWidth, height = height) {
            placeable.place(x = margin, y = y)
        }
    }
}

/** [caretTargetX] is the spot's centre in root coordinates; the caret points at it from the card's top or bottom edge. */
@Composable
private fun TourCard(
    step: TourStep,
    caretTargetX: Float?,
    isCaretOnTop: Boolean,
    callbacks: TourCallbacks
) {
    val shape = RoundedCornerShape(size = 24.dp)
    val surface = colors.surface
    val title = stringResource(resource = step.title)
    val middleCount = TourStep.entries.size - 2
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val targetX = caretTargetX ?: return@drawBehind
                val caretSize = CaretSize.toPx()
                val inset = 24.dp.toPx()
                val center = Offset(
                    x = (targetX - CardMargin.toPx()).coerceIn(minimumValue = inset, maximumValue = size.width - inset),
                    y = if (isCaretOnTop) 0f else size.height
                )
                rotate(degrees = 45f, pivot = center) {
                    drawRoundRect(
                        color = surface,
                        topLeft = Offset(x = center.x - caretSize / 2, y = center.y - caretSize / 2),
                        size = Size(width = caretSize, height = caretSize),
                        cornerRadius = CornerRadius(x = 3.dp.toPx())
                    )
                }
            }
            .shadow(elevation = 16.dp, shape = shape, clip = false)
            .clip(shape = shape)
            .background(color = surface)
            .semantics {
                paneTitle = title
                liveRegion = LiveRegionMode.Polite
            }
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        if (step.isFirst || step.isLast) {
            Box(
                modifier = Modifier
                    .size(size = 52.dp)
                    .background(
                        color = if (step.isLast) colors.accent else colors.accentSoft,
                        shape = RoundedCornerShape(size = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = if (step.isLast) Res.drawable.ic_check else Res.drawable.ic_star),
                    contentDescription = null,
                    tint = if (step.isLast) colors.onAccent else colors.accentInk,
                    modifier = Modifier.size(size = 26.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = step.screenLabel).uppercase(),
                style = typography.bold12,
                color = colors.accentInk,
                modifier = Modifier.weight(weight = 1f)
            )
            if (!step.isFirst && !step.isLast) {
                Text(
                    text = stringResource(resource = Res.string.tour_step_of, step.ordinal, middleCount),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        Text(text = title, style = typography.bold24, color = colors.ink)
        Text(text = stringResource(resource = step.body), style = typography.regular13, color = colors.muted)
        StepDots(current = step)
        Row(
            modifier = Modifier.padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            if (!step.isFirst && !step.isLast) {
                SecondaryButton(text = stringResource(resource = Res.string.back), isOutlined = true, onClick = callbacks::onBackClicked)
            }
            if (step.isFirst) {
                SecondaryButton(text = stringResource(resource = Res.string.tour_not_now), isOutlined = false, onClick = callbacks::onSkipClicked)
            }
            CommonPrimaryButton(
                modifier = Modifier.weight(weight = 1f),
                text = when {
                    step.isFirst -> Res.string.tour_start
                    step.isLast -> Res.string.tour_start_logging
                    step.ordinal == TourStep.entries.lastIndex - 1 -> Res.string.tour_finish
                    else -> Res.string.tour_next
                },
                onClick = callbacks::onNextClicked
            )
        }
    }
}

@Composable
private fun StepDots(current: TourStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        TourStep.entries.forEach { step ->
            val weight by animateFloatAsState(targetValue = if (step == current) 3f else 1f)
            Box(
                modifier = Modifier
                    .weight(weight = weight)
                    .height(height = 6.dp)
                    .background(
                        color = if (step.ordinal <= current.ordinal) colors.accent else colors.divider,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    isOutlined: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Box(
        modifier = Modifier
            .height(height = 54.dp)
            .clip(shape = shape)
            .then(
                if (isOutlined) {
                    Modifier
                        .background(color = colors.surface)
                        .border(width = 1.dp, color = colors.line, shape = shape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.bold15,
            color = if (isOutlined) colors.ink else colors.muted
        )
    }
}

/** Top-right, unless the spotlight sits near the top; then bottom-right. */
@Composable
private fun SkipButton(spot: Rect?, onClick: () -> Unit) {
    val density = LocalDensity.current
    Box(modifier = Modifier.fillMaxSize()) {
        val coversTopRight = spot != null && with(density) { spot.top < 140.dp.toPx() }
        val shape = RoundedCornerShape(size = 22.dp)
        Row(
            modifier = Modifier
                .align(alignment = if (coversTopRight) Alignment.BottomEnd else Alignment.TopEnd)
                .then(if (coversTopRight) Modifier.navigationBarsPadding() else Modifier.statusBarsPadding())
                .padding(all = 12.dp)
                .height(height = 44.dp)
                .clip(shape = shape)
                .background(color = Color(0xFF141619).copy(alpha = 0.55f))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.35f), shape = shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Text(text = stringResource(resource = Res.string.tour_skip), style = typography.bold14, color = Color.White)
            Icon(
                painter = painterResource(resource = Res.drawable.ic_close),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size = 14.dp)
            )
        }
    }
}

@Composable
private fun OutcomeMessage(outcome: TourOutcome, callbacks: TourCallbacks) {
    LaunchedEffect(key1 = outcome) {
        delay(timeMillis = OUTCOME_MESSAGE_MILLIS)
        callbacks.onOutcomeDismissed()
    }
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 92.dp)
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.ink)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(
                resource = when (outcome) {
                    TourOutcome.FINISHED -> Res.string.tour_finished_message
                    TourOutcome.SKIPPED -> Res.string.tour_skipped_message
                }
            ),
            style = typography.regular13,
            color = colors.background,
            modifier = Modifier.weight(weight = 1f).padding(vertical = 10.dp)
        )
        Box(
            modifier = Modifier
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .clickable(onClick = callbacks::onReplayClicked)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(resource = Res.string.tour_replay), style = typography.bold14, color = colors.accentSoft)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTourOverlay() {
    MacroTrackTheme {
        TourOverlay(
            viewState = TourViewState(step = TourStep.WELCOME),
            targets = TourTargetRegistry(),
            callbacks = TourCallbacks.default()
        )
    }
}
