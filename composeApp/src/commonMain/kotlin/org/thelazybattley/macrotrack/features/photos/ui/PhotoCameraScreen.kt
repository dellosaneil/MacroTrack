package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.compose.koinInject
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_camera_flip
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_image
import macrotrack.composeapp.generated.resources.ic_timer
import macrotrack.composeapp.generated.resources.photo_camera_album
import macrotrack.composeapp.generated.resources.photo_camera_close
import macrotrack.composeapp.generated.resources.photo_camera_failed
import macrotrack.composeapp.generated.resources.photo_camera_flip
import macrotrack.composeapp.generated.resources.photo_camera_ghost
import macrotrack.composeapp.generated.resources.photo_camera_ghost_body
import macrotrack.composeapp.generated.resources.photo_camera_library
import macrotrack.composeapp.generated.resources.photo_camera_line_up
import macrotrack.composeapp.generated.resources.photo_camera_none
import macrotrack.composeapp.generated.resources.photo_camera_permission
import macrotrack.composeapp.generated.resources.photo_camera_permission_allow
import macrotrack.composeapp.generated.resources.photo_camera_permission_settings
import macrotrack.composeapp.generated.resources.photo_camera_shoot
import macrotrack.composeapp.generated.resources.photo_camera_today
import macrotrack.composeapp.generated.resources.photo_timer
import macrotrack.composeapp.generated.resources.photo_timer_off
import macrotrack.composeapp.generated.resources.photo_timer_seconds
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.features.activity.ui.ActivitySwitch
import org.thelazybattley.macrotrack.features.photos.PhotoCameraEvent
import org.thelazybattley.macrotrack.features.photos.PhotoCameraViewModel
import org.thelazybattley.macrotrack.features.photos.PhotoCameraViewState
import org.thelazybattley.macrotrack.features.photos.PhotoTimer
import org.thelazybattley.macrotrack.features.scanbarcode.ui.isCameraScanSupported
import org.thelazybattley.macrotrack.features.scanbarcode.ui.rememberCameraPermissionState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private val CameraBackground = Color(0xFF0B0C0E)
private val CameraControl = Color.White.copy(alpha = 0.14f)
private val CameraMuted = Color(0xFFB9BCC2)

@Composable
fun PhotoCameraScreen(
    onClose: () -> Unit,
    onOpenSave: () -> Unit
) {
    val viewModel = koinViewModel<PhotoCameraViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberCameraPermissionState()
    var isFailed by remember { mutableStateOf(value = false) }
    val libraryImport = rememberLibraryImport(onImported = onOpenSave)
    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PhotoCameraEvent.OPEN_SAVE -> onOpenSave()
                PhotoCameraEvent.FAILED -> isFailed = true
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        if (isCameraScanSupported && !permission.isGranted && !permission.isPermanentlyDenied) permission.request()
    }
    Box(modifier = Modifier.fillMaxSize().background(color = CameraBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            val canShoot = isCameraScanSupported && permission.isGranted
            val camera = if (canShoot) rememberPhotoCamera() else null
            var countdown by remember { mutableStateOf<Int?>(value = null) }
            val scope = rememberCoroutineScope()
            var countdownJob by remember { mutableStateOf<Job?>(value = null) }
            CameraTopBar(viewState = viewState, onClose = onClose, onNextAlbum = viewModel::onNextAlbum, onTimer = viewModel::onTimerTapped)
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(shape = RoundedCornerShape(size = 24.dp))
                    .background(color = Color(0xFF2A2C30))
            ) {
                if (camera != null) {
                    PhotoCameraPreview(camera = camera, modifier = Modifier.fillMaxSize())
                    CameraOverlay(viewState = viewState, countdown = countdown)
                } else {
                    NoCamera(
                        isSupported = isCameraScanSupported,
                        isPermanentlyDenied = permission.isPermanentlyDenied,
                        onAllow = permission.request
                    )
                }
            }
            if (isFailed) {
                Text(
                    text = stringResource(resource = Res.string.photo_camera_failed),
                    style = typography.bold13,
                    color = colors.fat,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
            }
            if (viewState.ghost != null && canShoot) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(text = stringResource(resource = Res.string.photo_camera_ghost), style = typography.bold14, color = Color.White)
                        Text(text = stringResource(resource = Res.string.photo_camera_ghost_body), style = typography.regular11, color = CameraMuted)
                    }
                    ActivitySwitch(
                        isOn = viewState.isGhostOn,
                        description = stringResource(resource = Res.string.photo_camera_ghost),
                        onToggle = viewModel::onGhostToggled
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally)
            ) {
                PhotoPose.entries.forEach { pose ->
                    val isSelected = pose == viewState.pose
                    Box(
                        modifier = Modifier
                            .height(height = 36.dp)
                            .clip(shape = RoundedCornerShape(size = 18.dp))
                            .background(color = if (isSelected) Color.White else CameraControl)
                            .clickable(role = Role.RadioButton) { viewModel.onPoseSelected(pose = pose) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(resource = pose.label),
                            style = typography.bold13,
                            color = if (isSelected) colors.black else Color.White
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CameraSideButton(
                    text = stringResource(resource = Res.string.photo_camera_library),
                    icon = { PhotoIcon(resource = Res.drawable.ic_image, size = 22, tint = Color.White) },
                    isRound = false,
                    onClick = {
                        countdownJob?.cancel()
                        countdown = null
                        libraryImport.start(albumId = viewState.album?.album?.id, pose = viewState.pose)
                    }
                )
                val shootLabel = stringResource(resource = Res.string.photo_camera_shoot)
                Box(
                    modifier = Modifier
                        .size(size = 78.dp)
                        .clip(shape = CircleShape)
                        .border(width = 4.dp, color = Color.White, shape = CircleShape)
                        .clickable(enabled = camera != null && !viewState.isSaving && countdown == null) {
                            isFailed = false
                            countdownJob = scope.launch {
                                for (second in viewState.timer.seconds downTo 1) {
                                    countdown = second
                                    delay(timeMillis = 1_000)
                                }
                                countdown = null
                                camera?.capture(onResult = viewModel::onCaptured)
                            }
                        }
                        .semantics { contentDescription = shootLabel }
                        .padding(all = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewState.isSaving) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(size = 32.dp))
                    } else {
                        Box(modifier = Modifier.fillMaxSize().clip(shape = CircleShape).background(color = Color.White))
                    }
                }
                CameraSideButton(
                    text = stringResource(resource = Res.string.photo_camera_flip),
                    icon = { PhotoIcon(resource = Res.drawable.ic_camera_flip, size = 20, tint = Color.White) },
                    isRound = true,
                    onClick = { camera?.let { it.isFrontFacing = !it.isFrontFacing } }
                )
            }
        }
        ImportingOverlay(isVisible = libraryImport.isImporting)
    }
}

@Composable
private fun CameraTopBar(
    viewState: PhotoCameraViewState,
    onClose: () -> Unit,
    onNextAlbum: () -> Unit,
    onTimer: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        val closeLabel = stringResource(resource = Res.string.photo_camera_close)
        Box(
            modifier = Modifier
                .size(size = 44.dp)
                .clip(shape = CircleShape)
                .background(color = CameraControl)
                .clickable(onClick = onClose)
                .semantics { contentDescription = closeLabel },
            contentAlignment = Alignment.Center
        ) {
            PhotoIcon(resource = Res.drawable.ic_close, size = 18, tint = Color.White)
        }
        val albumName = viewState.album?.album?.name.orEmpty()
        val albumLabel = stringResource(resource = Res.string.photo_camera_album, albumName)
        Row(
            modifier = Modifier
                .weight(weight = 1f)
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 22.dp))
                .background(color = CameraControl)
                .clickable(enabled = viewState.albums.size > 1, onClick = onNextAlbum)
                .semantics { contentDescription = albumLabel }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally)
        ) {
            Text(text = albumName, style = typography.bold14, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (viewState.albums.size > 1) {
                Box(modifier = Modifier.rotate(degrees = 90f)) { PhotoIcon(resource = Res.drawable.ic_chevron_right, size = 14, tint = Color.White) }
            }
        }
        val timerText = when (viewState.timer) {
            PhotoTimer.OFF -> stringResource(resource = Res.string.photo_timer_off)
            else -> stringResource(resource = Res.string.photo_timer_seconds, viewState.timer.seconds)
        }
        val timerLabel = stringResource(resource = Res.string.photo_timer, timerText)
        Row(
            modifier = Modifier
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 22.dp))
                .background(color = CameraControl)
                .clickable(onClick = onTimer)
                .semantics { contentDescription = timerLabel }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            PhotoIcon(resource = Res.drawable.ic_timer, size = 16, tint = Color.White)
            Text(text = timerText, style = typography.bold13, color = Color.White)
        }
    }
}

/** The ghost of the last photo, a rule-of-thirds grid, today's weight and the countdown. */
@Composable
private fun CameraOverlay(viewState: PhotoCameraViewState, countdown: Int?) {
    Box(modifier = Modifier.fillMaxSize()) {
        val ghost = viewState.ghost
        if (ghost != null && viewState.isGhostOn) {
            GhostImage(fileName = ghost.photo.fileName)
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val line = Color.White.copy(alpha = 0.18f)
            for (index in 1..2) {
                val x = size.width * index / 3
                val y = size.height * index / 3
                drawLine(color = line, start = Offset(x = x, y = 0f), end = Offset(x = x, y = size.height), strokeWidth = 1.dp.toPx())
                drawLine(color = line, start = Offset(x = 0f, y = y), end = Offset(x = size.width, y = y), strokeWidth = 1.dp.toPx())
            }
        }
        if (ghost != null && viewState.isGhostOn) {
            OverlayLabel(
                modifier = Modifier.align(alignment = Alignment.TopCenter).padding(top = 12.dp),
                text = stringResource(
                    resource = Res.string.photo_camera_line_up,
                    ghost.photo.date.toAbbreviatedMonthDay(),
                    photoWeightText(weight = ghost.weight)
                )
            )
        }
        OverlayLabel(
            modifier = Modifier.align(alignment = Alignment.BottomStart).padding(all = 12.dp),
            text = stringResource(resource = Res.string.photo_camera_today, photoWeightText(weight = viewState.todayWeight))
        )
        countdown?.let {
            Text(
                text = it.toString(),
                style = TextStyle(fontSize = 96.sp),
                color = Color.White,
                modifier = Modifier.align(alignment = Alignment.Center)
            )
        }
    }
}

/**
 * Just the person from the last photo, see-through over the live camera, so the room behind doesn't get in the way.
 * The whole photo, fainter, when no person could be picked out.
 */
@Composable
internal fun GhostImage(fileName: String) {
    val storage = koinInject<PhotoStorage>()
    // Keyed so a different photo doesn't show the last one's cutout while its own loads.
    val cutout = key(fileName) {
        produceState<GhostCutout>(initialValue = GhostCutout.Loading) {
            value = withContext(context = Dispatchers.Default) {
                storage.readCutout(fileName = fileName)
                    ?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
                    ?.let { GhostCutout.Person(bitmap = it) }
                    ?: GhostCutout.None
            }
        }.value
    }
    when (val current = cutout) {
        GhostCutout.Loading -> Unit
        GhostCutout.None -> PhotoImage(modifier = Modifier.fillMaxSize(), fileName = fileName, isThumbnail = false, alpha = 0.3f)
        is GhostCutout.Person -> Image(
            bitmap = current.bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.55f,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private sealed interface GhostCutout {
    data object Loading : GhostCutout
    data object None : GhostCutout
    data class Person(val bitmap: ImageBitmap) : GhostCutout
}

@Composable
private fun OverlayLabel(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        style = typography.bold12,
        color = Color.White,
        maxLines = 1,
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 15.dp))
            .background(color = Color(0xB30A0B0D))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun NoCamera(isSupported: Boolean, isPermanentlyDenied: Boolean, onAllow: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 14.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(resource = if (isSupported) Res.string.photo_camera_permission else Res.string.photo_camera_none),
            style = typography.bold14,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        if (isSupported) {
            Box(
                modifier = Modifier
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = Color.White)
                    .clickable(onClick = onAllow)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        resource = if (isPermanentlyDenied) Res.string.photo_camera_permission_settings else Res.string.photo_camera_permission_allow
                    ),
                    style = typography.bold14,
                    color = colors.black
                )
            }
        }
    }
}

@Composable
private fun CameraSideButton(text: String, icon: @Composable () -> Unit, isRound: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(width = 64.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 48.dp)
                .clip(shape = if (isRound) CircleShape else RoundedCornerShape(size = 14.dp))
                .background(color = CameraControl),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(text = text, style = typography.bold11, color = Color.White)
    }
}
