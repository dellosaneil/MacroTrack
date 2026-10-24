package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.ic_car
import macrotrack.composeapp.generated.resources.ic_chevron_down
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_fit
import macrotrack.composeapp.generated.resources.ic_map_pin_off
import macrotrack.composeapp.generated.resources.ic_pause
import macrotrack.composeapp.generated.resources.ic_play
import macrotrack.composeapp.generated.resources.ic_shield
import macrotrack.composeapp.generated.resources.ic_signal
import macrotrack.composeapp.generated.resources.ic_sliders
import macrotrack.composeapp.generated.resources.ic_stop
import macrotrack.composeapp.generated.resources.ic_timer
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.record_burned
import macrotrack.composeapp.generated.resources.record_discard
import macrotrack.composeapp.generated.resources.record_distance
import macrotrack.composeapp.generated.resources.record_elapsed
import macrotrack.composeapp.generated.resources.record_finish
import macrotrack.composeapp.generated.resources.record_fit_route
import macrotrack.composeapp.generated.resources.record_gps_good
import macrotrack.composeapp.generated.resources.record_gps_waiting
import macrotrack.composeapp.generated.resources.record_keep_recording
import macrotrack.composeapp.generated.resources.record_map_description
import macrotrack.composeapp.generated.resources.record_minimize
import macrotrack.composeapp.generated.resources.record_pace
import macrotrack.composeapp.generated.resources.record_pause
import macrotrack.composeapp.generated.resources.record_paused
import macrotrack.composeapp.generated.resources.record_permission_body
import macrotrack.composeapp.generated.resources.record_permission_continue
import macrotrack.composeapp.generated.resources.record_permission_not_now
import macrotrack.composeapp.generated.resources.record_permission_settings
import macrotrack.composeapp.generated.resources.record_permission_title
import macrotrack.composeapp.generated.resources.record_perk_change_body
import macrotrack.composeapp.generated.resources.record_perk_change_title
import macrotrack.composeapp.generated.resources.record_perk_phone_body
import macrotrack.composeapp.generated.resources.record_perk_phone_title
import macrotrack.composeapp.generated.resources.record_perk_recording_body
import macrotrack.composeapp.generated.resources.record_perk_recording_title
import macrotrack.composeapp.generated.resources.record_recording
import macrotrack.composeapp.generated.resources.record_riding
import macrotrack.composeapp.generated.resources.record_riding_body
import macrotrack.composeapp.generated.resources.record_riding_title
import macrotrack.composeapp.generated.resources.record_location_off_action
import macrotrack.composeapp.generated.resources.record_location_off_body
import macrotrack.composeapp.generated.resources.record_location_off_title
import macrotrack.composeapp.generated.resources.record_resume
import macrotrack.composeapp.generated.resources.record_save
import macrotrack.composeapp.generated.resources.record_save_title
import macrotrack.composeapp.generated.resources.record_steps
import macrotrack.composeapp.generated.resources.record_today
import macrotrack.composeapp.generated.resources.record_today_of
import macrotrack.composeapp.generated.resources.save_summary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.rememberLocationPermissionState
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.ActivityCalories
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.METERS_PER_KM
import org.thelazybattley.macrotrack.features.activity.RecordCallbacks
import org.thelazybattley.macrotrack.features.activity.RecordViewModel
import org.thelazybattley.macrotrack.features.activity.RecordViewState
import org.thelazybattley.macrotrack.features.activity.Recording
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun RecordScreen(
    onBack: () -> Unit,
    onMinimize: () -> Unit,
    onSaved: (Long) -> Unit,
    /** "Not now" on the location screen: walks and runs need the map, so this goes back to Steps. */
    onLocationDeclined: () -> Unit
) {
    val viewModel = koinViewModel<RecordViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val location = rememberLocationPermissionState()
    LaunchedEffect(key1 = location.isGranted) {
        if (location.isGranted) viewModel.onStart()
    }
    // A recording picked up after a restart can come back without location: iOS forgets "Allow Once" when the app
    // closes. Its map stays up, and the system asks straight away rather than showing the location page first.
    val hasRecording = viewState.recording != null
    var hasAskedAgain by remember { mutableStateOf(value = false) }
    LaunchedEffect(key1 = hasRecording, key2 = location.isGranted) {
        if (hasRecording && !location.isGranted && !location.isPermanentlyDenied && !hasAskedAgain) {
            hasAskedAgain = true
            location.request()
        }
    }
    LaunchedEffect(key1 = viewState.savedId) {
        viewState.savedId?.let(onSaved)
    }
    LaunchedEffect(key1 = viewState.isDiscarded) {
        if (viewState.isDiscarded) onBack()
    }
    if (!location.isGranted && !hasRecording) {
        LocationPermissionContent(
            isPermanentlyDenied = location.isPermanentlyDenied,
            onContinue = location.request,
            onNotNow = onLocationDeclined
        )
    } else {
        LiveContent(
            viewState = viewState,
            callbacks = viewModel,
            onMinimize = onMinimize,
            onAllowLocation = location.request.takeUnless { location.isGranted }
        )
    }
}

@Composable
private fun LocationPermissionContent(
    isPermanentlyDenied: Boolean,
    onContinue: () -> Unit,
    onNotNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp)
    ) {
        SquareButton(icon = Res.drawable.ic_chevron_left, description = stringResource(resource = Res.string.back), onClick = onNotNow)
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = 20.dp)
        ) {
            RouteSketch(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 200.dp)
                    .clip(shape = RoundedCornerShape(size = 24.dp)),
                points = sampleLoop
            )
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(text = stringResource(resource = Res.string.record_permission_title), style = typography.bold24, color = colors.ink)
                Text(text = stringResource(resource = Res.string.record_permission_body), style = typography.regular13, color = colors.muted)
            }
            Perk(
                icon = Res.drawable.ic_timer,
                background = colors.accentSoft,
                tint = colors.accentInk,
                title = stringResource(resource = Res.string.record_perk_recording_title),
                body = stringResource(resource = Res.string.record_perk_recording_body)
            )
            Perk(
                icon = Res.drawable.ic_shield,
                background = colors.nightSoft,
                tint = colors.nightInk,
                title = stringResource(resource = Res.string.record_perk_phone_title),
                body = stringResource(resource = Res.string.record_perk_phone_body)
            )
            Perk(
                icon = Res.drawable.ic_sliders,
                background = colors.warmSoft,
                tint = colors.warmInk,
                title = stringResource(resource = Res.string.record_perk_change_title),
                body = stringResource(resource = Res.string.record_perk_change_body)
            )
        }
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(resource = if (isPermanentlyDenied) Res.string.record_permission_settings else Res.string.record_permission_continue),
            onClick = onContinue
        )
        SheetActionButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(resource = Res.string.record_permission_not_now),
            style = SheetButtonStyle.TEXT,
            onClick = onNotNow
        )
    }
}

@Composable
private fun Perk(icon: DrawableResource, background: Color, tint: Color, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = background),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = icon), contentDescription = null, tint = tint, modifier = Modifier.size(size = 18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = body, style = typography.regular13, color = colors.muted)
        }
    }
}

@Composable
private fun LiveContent(
    viewState: RecordViewState,
    callbacks: RecordCallbacks,
    onMinimize: () -> Unit,
    /** Set while location is off, to ask for it again. */
    onAllowLocation: (() -> Unit)?
) {
    val recording = viewState.recording
    val type = recording?.type ?: viewState.type
    val now = rememberRecordingNow(recording = recording)
    val mapState = rememberRouteMapState()
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    // The panel covers the bottom of the map, so the route is kept in the part above it.
    val density = LocalDensity.current
    var panelHeight by remember { mutableStateOf(value = 0.dp) }
    Box(modifier = Modifier.fillMaxSize().background(color = colors.divider)) {
        RouteMap(
            modifier = Modifier.fillMaxSize(),
            routes = listOfNotNull(recording?.points?.let { MapRoute(points = it) }),
            description = stringResource(resource = Res.string.record_map_description),
            state = mapState,
            live = recording?.livePoint ?: recording?.points?.lastOrNull(),
            padding = PaddingValues(start = 56.dp, top = statusBar + 72.dp, end = 56.dp, bottom = panelHeight + 40.dp),
            attributionAlignment = Alignment.TopEnd,
            attributionPadding = PaddingValues(top = statusBar + 72.dp, end = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            // Recording carries on while the rest of the app is used; the Log card and recording bar lead back here.
            FloatingButton(icon = Res.drawable.ic_chevron_down, description = stringResource(resource = Res.string.record_minimize), onClick = onMinimize)
            StatusPill(isPaused = recording?.isPaused == true, isRiding = recording?.isRiding == true)
            GpsPill(isGood = recording?.hasGoodFix == true)
            Box(modifier = Modifier.weight(weight = 1f))
            FloatingButton(icon = Res.drawable.ic_fit, description = stringResource(resource = Res.string.record_fit_route), onClick = mapState::fit)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 72.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            if (onAllowLocation != null) {
                MapNotice(
                    icon = Res.drawable.ic_map_pin_off,
                    tint = colors.warmInk,
                    background = colors.warmSoft,
                    title = stringResource(resource = Res.string.record_location_off_title),
                    body = stringResource(resource = Res.string.record_location_off_body),
                    action = stringResource(resource = Res.string.record_location_off_action),
                    onAction = onAllowLocation
                )
            }
            if (recording?.isRiding == true && !recording.isPaused) {
                MapNotice(
                    icon = Res.drawable.ic_car,
                    tint = colors.nightInk,
                    background = colors.nightSoft,
                    title = stringResource(resource = Res.string.record_riding_title),
                    body = stringResource(resource = Res.string.record_riding_body)
                )
            }
        }
        LivePanel(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .onSizeChanged { panelHeight = with(density) { it.height.toDp() } },
            viewState = viewState,
            type = type,
            recording = recording,
            now = now,
            callbacks = callbacks
        )
    }
    viewState.saveDraft?.let { draft ->
        val movingSeconds = (recording?.movingMillis(now = now) ?: 0L) / 1000
        ActivityNameSheet(
            title = stringResource(resource = Res.string.record_save_title, stringResource(resource = draft.type.title).lowercase()),
            summary = stringResource(
                resource = Res.string.save_summary,
                distanceText(meters = recording?.distanceMeters ?: 0.0),
                clockDuration(seconds = movingSeconds),
                (recording?.steps ?: 0).withThousands()
            ),
            name = draft.name,
            type = draft.type,
            fallbackName = stringResource(resource = draft.type.title) + " · " + getCurrentDate().toAbbreviatedMonthDay(),
            usedNames = viewState.usedNames[draft.type].orEmpty(),
            onNameChange = callbacks::onNameChanged,
            onTypeChange = callbacks::onTypeChanged,
            onDismiss = callbacks::onKeepRecording
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                SheetActionButton(text = stringResource(resource = Res.string.record_discard), style = SheetButtonStyle.DESTRUCTIVE, onClick = callbacks::onDiscard)
                SheetActionButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = Res.string.record_save, stringResource(resource = draft.type.title).lowercase()),
                    style = SheetButtonStyle.PRIMARY,
                    onClick = callbacks::onSave
                )
            }
            SheetActionButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(resource = Res.string.record_keep_recording),
                style = SheetButtonStyle.TEXT,
                onClick = callbacks::onKeepRecording
            )
        }
    }
}

@Composable
private fun LivePanel(
    modifier: Modifier = Modifier,
    viewState: RecordViewState,
    type: ActivityType,
    recording: Recording?,
    now: Long,
    callbacks: RecordCallbacks
) {
    val pro = LocalProAccess.current
    val isPaused = recording?.isPaused == true
    val movingSeconds = (recording?.movingMillis(now = now) ?: 0L) / 1000
    val distance = recording?.distanceMeters ?: 0.0
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(color = colors.background)
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(shape = CircleShape)
                .background(color = colors.line)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(resource = Res.string.record_elapsed, stringResource(resource = type.title)),
                    style = typography.medium11,
                    color = colors.muted
                )
                Text(
                    text = clockDuration(seconds = movingSeconds),
                    style = typography.bold36.copy(fontSize = 52.sp, lineHeight = 52.sp),
                    color = if (isPaused) colors.muted else colors.ink
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = stringResource(resource = Res.string.record_today), style = typography.medium11, color = colors.muted)
                Text(
                    text = stringResource(resource = Res.string.record_today_of, viewState.today.totalSteps.withThousands(), viewState.goal.withThousands()),
                    style = typography.bold15,
                    color = colors.ink,
                    textAlign = TextAlign.End
                )
            }
        }
        val pace = if (distance >= 50) movingSeconds / (distance / METERS_PER_KM) else null
        val profile = viewState.profile
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                StatTile(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = Res.string.record_distance),
                    value = distanceValue(meters = distance),
                    unit = distanceUnit()
                )
                StatTile(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = Res.string.record_steps),
                    value = (recording?.steps ?: 0).withThousands()
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                StatTile(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = Res.string.record_pace),
                    value = pace?.let { paceValue(secondsPerKm = it) } ?: "–",
                    unit = paceUnit()
                )
                StatTile(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = Res.string.record_burned),
                    value = profile?.let {
                        "≈" + ActivityCalories.sessionKcal(type = type, meters = distance, climbMeters = recording?.climbMeters ?: 0.0, weightKg = it.weightKg)
                    } ?: "–",
                    unit = stringResource(resource = Res.string.kcal_text),
                    isLocked = !pro.isActive,
                    onLockedClick = { pro.openPaywall(ProFeature.CALORIES_BURNED) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            PanelButton(
                modifier = Modifier.weight(weight = 1f),
                icon = if (isPaused) Res.drawable.ic_play else Res.drawable.ic_pause,
                text = stringResource(resource = if (isPaused) Res.string.record_resume else Res.string.record_pause),
                background = colors.divider,
                content = colors.ink,
                onClick = callbacks::onPauseToggled
            )
            PanelButton(
                modifier = Modifier.weight(weight = 1f),
                icon = Res.drawable.ic_stop,
                text = stringResource(resource = Res.string.record_finish),
                background = colors.accent,
                content = colors.onAccent,
                onClick = callbacks::onFinish
            )
        }
    }
}

@Composable
private fun PanelButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    text: String,
    background: Color,
    content: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(height = 56.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = background)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = content, modifier = Modifier.size(size = 18.dp))
        Text(text = text, style = typography.bold16, color = content)
    }
}

@Composable
private fun StatusPill(isPaused: Boolean, isRiding: Boolean) {
    val isOnHold = isRiding && !isPaused
    Row(
        modifier = Modifier
            .height(height = 32.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(size = 10.dp))
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = if (isPaused) colors.warmSoft else if (isOnHold) colors.nightSoft else colors.surface)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        if (isOnHold) {
            Icon(painter = painterResource(resource = Res.drawable.ic_car), contentDescription = null, tint = colors.nightInk, modifier = Modifier.size(size = 14.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(size = 8.dp)
                    .clip(shape = CircleShape)
                    .background(color = if (isPaused) colors.warmInk else colors.danger)
            )
        }
        Text(
            text = stringResource(
                resource = when {
                    isPaused -> Res.string.record_paused
                    isOnHold -> Res.string.record_riding
                    else -> Res.string.record_recording
                }
            ),
            style = typography.bold12,
            color = if (isPaused) colors.warmInk else if (isOnHold) colors.nightInk else colors.ink
        )
    }
}

/** A note over the map, about riding or location being off, with an optional button. */
@Composable
private fun MapNotice(
    icon: DrawableResource,
    tint: Color,
    background: Color,
    title: String,
    body: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(size = 14.dp))
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
            .padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 32.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(color = background),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = icon), contentDescription = null, tint = tint, modifier = Modifier.size(size = 16.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = title, style = typography.bold13, color = colors.ink)
            Text(text = body, style = typography.regular11, color = colors.muted)
        }
        if (action != null) {
            Box(
                modifier = Modifier
                    .height(height = 34.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = colors.accent)
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = action, style = typography.bold12, color = colors.onAccent)
            }
        }
    }
}

@Composable
private fun GpsPill(isGood: Boolean) {
    Row(
        modifier = Modifier
            .height(height = 32.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(size = 10.dp))
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = colors.surface)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_signal),
            contentDescription = null,
            tint = if (isGood) colors.accentInk else colors.warmInk,
            modifier = Modifier.size(size = 14.dp)
        )
        Text(
            text = stringResource(resource = if (isGood) Res.string.record_gps_good else Res.string.record_gps_waiting),
            style = typography.bold12,
            color = colors.ink
        )
    }
}

@Composable
private fun FloatingButton(icon: DrawableResource, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(size = 14.dp))
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = description, tint = colors.ink, modifier = Modifier.size(size = 20.dp))
    }
}

/** A made-up loop for the picture on the location page; it's never saved. */
private val sampleLoop = listOf(
    0.0 to 0.0, 0.0031 to -0.0002, 0.0042 to 0.0012, 0.0047 to 0.0035, 0.0038 to 0.0052,
    0.0021 to 0.0058, 0.0004 to 0.0056, 0.0 to 0.0040, 0.0 to 0.0
).mapIndexed { index, (lat, lon) ->
    org.thelazybattley.macrotrack.domain.model.RoutePoint(lat = lat, lon = lon, timeMillis = index.toLong())
}
