package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_chevron_up
import macrotrack.composeapp.generated.resources.ic_pause
import macrotrack.composeapp.generated.resources.ic_play
import macrotrack.composeapp.generated.resources.ic_stop
import macrotrack.composeapp.generated.resources.record_distance
import macrotrack.composeapp.generated.resources.record_finish
import macrotrack.composeapp.generated.resources.record_gps_good
import macrotrack.composeapp.generated.resources.record_gps_waiting
import macrotrack.composeapp.generated.resources.record_pace
import macrotrack.composeapp.generated.resources.record_pause
import macrotrack.composeapp.generated.resources.record_resume
import macrotrack.composeapp.generated.resources.record_steps
import macrotrack.composeapp.generated.resources.recording_goal_reached_on
import macrotrack.composeapp.generated.resources.recording_line
import macrotrack.composeapp.generated.resources.recording_open_map
import macrotrack.composeapp.generated.resources.recording_open_map_short
import macrotrack.composeapp.generated.resources.recording_pause_type
import macrotrack.composeapp.generated.resources.recording_paused_hint
import macrotrack.composeapp.generated.resources.recording_resume_type
import macrotrack.composeapp.generated.resources.recording_started
import macrotrack.composeapp.generated.resources.recording_status
import macrotrack.composeapp.generated.resources.recording_status_paused
import macrotrack.composeapp.generated.resources.recording_today_of
import macrotrack.composeapp.generated.resources.today_steps_goal_reached
import macrotrack.composeapp.generated.resources.today_steps_to_go
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.thelazybattley.macrotrack.domain.model.METERS_PER_KM
import org.thelazybattley.macrotrack.features.activity.ActivityRecorder
import org.thelazybattley.macrotrack.features.activity.Recording
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** The clock, ticking every second while [recording] runs, so elapsed time counts up. */
@OptIn(ExperimentalTime::class)
@Composable
internal fun rememberRecordingNow(recording: Recording?): Long {
    val now by produceState(initialValue = Clock.System.now().toEpochMilliseconds(), key1 = recording?.isPaused) {
        while (true) {
            value = Clock.System.now().toEpochMilliseconds()
            delay(timeMillis = 1_000)
        }
    }
    return now
}

/** "Recording walk" or "Paused · walk": the text always says which, so the dot is never the only signal. */
@Composable
private fun Recording.statusText(): String {
    val noun = stringResource(resource = type.title).lowercase()
    return stringResource(resource = if (isPaused) Res.string.recording_status_paused else Res.string.recording_status, noun)
}

/** A red dot that pulses while recording, and turns into a still amber square when paused. */
@Composable
internal fun RecordingDot(isPaused: Boolean, recordingColor: Color, pausedColor: Color) {
    if (isPaused) {
        Box(
            modifier = Modifier
                .size(size = 12.dp)
                .clip(shape = RoundedCornerShape(size = 3.dp))
                .background(color = pausedColor)
        )
        return
    }
    val pulse by rememberInfiniteTransition(label = "recording").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1_600, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .size(size = 12.dp)
            .drawBehind {
                val grown = (pulse / 0.7f).coerceAtMost(maximumValue = 1f)
                drawCircle(
                    color = recordingColor.copy(alpha = 0.55f * (1f - grown)),
                    radius = size.minDimension / 2 + 7.dp.toPx() * grown
                )
            }
            .clip(shape = CircleShape)
            .background(color = recordingColor)
    )
}

/**
 * Sits above the bottom navigation on every tab while a walk or run records: what's recording, time, distance and
 * steps, Pause / Resume, and a way back to the live map. Can't be dismissed; it goes when the recording ends.
 */
@Composable
fun RecordingBar(modifier: Modifier = Modifier) {
    val recorder = koinInject<ActivityRecorder>()
    val recording by recorder.state.collectAsStateWithLifecycle()
    recording?.let { current ->
        RecordingBar(
            modifier = modifier,
            recording = current,
            onTogglePause = recorder::togglePause,
            onOpen = { recorder.requestOpen() }
        )
    }
}

@Composable
private fun RecordingBar(
    modifier: Modifier = Modifier,
    recording: Recording,
    onTogglePause: () -> Unit,
    onOpen: () -> Unit
) {
    val now = rememberRecordingNow(recording = recording)
    // The bar is drawn in the page's ink, so it stands apart from the cards in both themes.
    val isLightBar = colors.ink.luminance() > 0.5f
    val content = colors.background
    val status = recording.statusText()
    val line = stringResource(
        resource = Res.string.recording_line,
        clockDuration(seconds = recording.movingMillis(now = now) / 1000),
        distanceText(meters = recording.distanceMeters),
        recording.steps.withThousands()
    )
    val noun = stringResource(resource = recording.type.title).lowercase()
    Box(modifier = modifier.fillMaxWidth().background(color = colors.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 56.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(size = 18.dp))
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.ink)
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            RecordingDot(
                isPaused = recording.isPaused,
                recordingColor = if (isLightBar) RecDotOnLight else RecDotOnDark,
                pausedColor = if (isLightBar) PausedOnLight else PausedOnDark
            )
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxHeight()
                    .clickable(role = Role.Button, onClick = onOpen)
                    .semantics(mergeDescendants = true) {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "$status, $line"
                    },
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = status.uppercase(), style = typography.bold11.copy(letterSpacing = 0.4.sp), color = content.copy(alpha = 0.72f))
                Text(text = line, style = typography.bold15, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            BarButton(
                icon = if (recording.isPaused) Res.drawable.ic_play else Res.drawable.ic_pause,
                description = stringResource(resource = if (recording.isPaused) Res.string.recording_resume_type else Res.string.recording_pause_type, noun),
                background = content.copy(alpha = 0.12f),
                tint = content,
                onClick = onTogglePause
            )
            BarButton(
                icon = Res.drawable.ic_chevron_up,
                description = stringResource(resource = Res.string.recording_open_map),
                background = Color.Transparent,
                tint = content,
                onClick = onOpen
            )
        }
    }
}

@Composable
private fun BarButton(icon: DrawableResource, description: String, background: Color, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 13.dp))
            .background(color = background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = description, tint = tint, modifier = Modifier.size(size = 18.dp))
    }
}

/**
 * Takes the steps card's place on the Log tab while a walk or run records: elapsed time, a mini map, this
 * session's distance, steps and pace, today's total, and Pause / Resume and Finish.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun LiveRecordingCard(
    modifier: Modifier = Modifier,
    recording: Recording,
    todaySteps: Int,
    goal: Int,
    onTogglePause: () -> Unit,
    onOpen: () -> Unit,
    onFinish: () -> Unit,
    onOpenSteps: () -> Unit
) {
    val now = rememberRecordingNow(recording = recording)
    val movingSeconds = recording.movingMillis(now = now) / 1000
    val distance = recording.distanceMeters
    val noun = stringResource(resource = recording.type.title).lowercase()
    val shape = RoundedCornerShape(size = 20.dp)
    val sub = if (recording.isPaused) {
        stringResource(resource = Res.string.recording_paused_hint)
    } else {
        val started = Instant.fromEpochMilliseconds(epochMilliseconds = recording.startMillis).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        stringResource(
            resource = Res.string.recording_started,
            clockTime(time = started),
            stringResource(resource = if (recording.hasGoodFix) Res.string.record_gps_good else Res.string.record_gps_waiting)
        )
    }
    val status = recording.statusText()
    val time = clockDuration(seconds = movingSeconds)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.5.dp, color = colors.accent, shape = shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { contentDescription = "$status, $time" }
                .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            RecordingDot(isPaused = recording.isPaused, recordingColor = colors.danger, pausedColor = colors.warmInk)
            Column(modifier = Modifier.weight(weight = 1f).clearAndSetSemantics {}) {
                Text(text = status.uppercase(), style = typography.bold12.copy(letterSpacing = 0.5.sp), color = colors.accentInk)
                Text(text = sub, style = typography.regular11, color = colors.muted)
            }
            Text(
                text = time,
                style = typography.bold24.copy(fontSize = 28.sp, lineHeight = 30.sp),
                color = if (recording.isPaused) colors.muted else colors.ink,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
        val openMap = stringResource(resource = Res.string.recording_open_map)
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
                .height(height = 96.dp)
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                .clickable(role = Role.Button, onClick = onOpen)
                .clearAndSetSemantics { contentDescription = openMap }
        ) {
            RouteSketch(modifier = Modifier.matchParentSize(), points = recording.points, lineWidth = 4.dp, padding = 16.dp)
            Row(
                modifier = Modifier
                    .align(alignment = Alignment.BottomEnd)
                    .padding(all = 8.dp)
                    .height(height = 26.dp)
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = colors.surface)
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(text = stringResource(resource = Res.string.recording_open_map_short), style = typography.bold12, color = colors.ink)
                Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 13.dp))
            }
        }
        val pace = if (distance >= 50) movingSeconds / (distance / METERS_PER_KM) else null
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            LiveStat(modifier = Modifier.weight(weight = 1f), label = stringResource(resource = Res.string.record_distance), value = distanceValue(meters = distance), unit = distanceUnit())
            LiveStat(modifier = Modifier.weight(weight = 1f), label = stringResource(resource = Res.string.record_steps), value = recording.steps.withThousands())
            LiveStat(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.record_pace),
                value = pace?.let { paceValue(secondsPerKm = it) } ?: "–",
                unit = paceUnit()
            )
        }
        val goalLine = when {
            todaySteps < goal -> stringResource(resource = Res.string.today_steps_to_go, (goal - todaySteps).withThousands())
            todaySteps - recording.steps < goal -> stringResource(resource = Res.string.recording_goal_reached_on, noun)
            else -> stringResource(resource = Res.string.today_steps_goal_reached)
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = colors.hero)
                .clickable(role = Role.Button, onClick = onOpenSteps)
                .semantics(mergeDescendants = true) {}
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            ProgressRing(modifier = Modifier.size(size = 32.dp), progress = todaySteps / goal.toFloat(), strokeWidth = 5.dp)
            Column(modifier = Modifier.weight(weight = 1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    Text(text = todaySteps.withThousands(), style = typography.bold14, color = colors.ink, modifier = Modifier.alignByBaseline())
                    Text(
                        text = stringResource(resource = Res.string.recording_today_of, goal.withThousands()),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Text(text = goalLine, style = typography.bold12, color = colors.accentInk)
            }
            Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 16.dp))
        }
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            CardButton(
                modifier = Modifier.weight(weight = 1f),
                icon = if (recording.isPaused) Res.drawable.ic_play else Res.drawable.ic_pause,
                text = stringResource(resource = if (recording.isPaused) Res.string.record_resume else Res.string.record_pause),
                background = colors.divider,
                content = colors.ink,
                onClick = onTogglePause
            )
            CardButton(
                modifier = Modifier.weight(weight = 1f),
                icon = Res.drawable.ic_stop,
                text = stringResource(resource = Res.string.record_finish),
                background = colors.accent,
                content = colors.onAccent,
                onClick = onFinish
            )
        }
    }
}

@Composable
private fun LiveStat(modifier: Modifier = Modifier, label: String, value: String, unit: String? = null) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Text(text = label, style = typography.medium11, color = colors.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            Text(text = value, style = typography.bold16.copy(fontSize = 17.sp), color = colors.ink, maxLines = 1, modifier = Modifier.alignByBaseline())
            unit?.let { Text(text = it, style = typography.regular11, color = colors.muted, modifier = Modifier.alignByBaseline()) }
        }
    }
}

@Composable
private fun CardButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    text: String,
    background: Color,
    content: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(height = 46.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = background)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = content, modifier = Modifier.size(size = 16.dp))
        Text(text = text, style = typography.bold14, color = content)
    }
}

/** The dot on the bar, bright enough to read on its dark ground (light theme) or dark enough on its light one. */
private val RecDotOnDark = Color(0xFFFF6B4A)
private val RecDotOnLight = Color(0xFFC2381B)
private val PausedOnDark = Color(0xFFF0C474)
private val PausedOnLight = Color(0xFF8A5A0B)
