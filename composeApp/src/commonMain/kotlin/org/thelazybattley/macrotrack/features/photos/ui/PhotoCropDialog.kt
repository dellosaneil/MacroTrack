package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.photo_camera_ghost
import macrotrack.composeapp.generated.resources.photo_crop_hint
import macrotrack.composeapp.generated.resources.photo_crop_reset
import macrotrack.composeapp.generated.resources.photo_crop_title
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.thelazybattley.macrotrack.core.platform.CropRect
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import org.thelazybattley.macrotrack.features.activity.ui.ActivitySwitch
import org.thelazybattley.macrotrack.features.photos.CROP_ASPECT
import org.thelazybattley.macrotrack.features.photos.CropPlacement
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private val CropBackground = Color(0xFF0B0C0E)
private val CropMuted = Color(0xFFB9BCC2)

/**
 * Crops a photo to 3:4: pinch and drag the photo inside the frame, double-tap to start over. [ghostFileName], the
 * album's last photo, can be laid over it to match the framing.
 */
@Composable
internal fun PhotoCropDialog(
    fileName: String,
    ghostFileName: String?,
    onCancel: () -> Unit,
    onDone: (CropRect) -> Unit
) {
    val storage = koinInject<PhotoStorage>()
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = fileName) {
        value = withContext(context = Dispatchers.Default) {
            storage.read(fileName = fileName, isThumbnail = false)?.let { runCatching { it.decodeToImageBitmap() }.getOrNull() }
        }
    }
    var isGhostOn by rememberSaveable { mutableStateOf(value = true) }
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = CropBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.photo_crop_title),
                style = typography.bold18,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            val image = bitmap
            var placement by remember(image) { mutableStateOf<CropPlacement?>(value = null) }
            Box(
                modifier = Modifier.weight(weight = 1f).fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (image == null) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    CropFrame(
                        image = image,
                        ghostFileName = ghostFileName.takeIf { isGhostOn },
                        placement = placement,
                        onPlacement = { placement = it }
                    )
                }
            }
            Text(
                text = stringResource(resource = Res.string.photo_crop_hint),
                style = typography.regular11,
                color = CropMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
            if (ghostFileName != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Text(
                        text = stringResource(resource = Res.string.photo_camera_ghost),
                        style = typography.bold14,
                        color = Color.White,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    ActivitySwitch(
                        isOn = isGhostOn,
                        description = stringResource(resource = Res.string.photo_camera_ghost),
                        onToggle = { isGhostOn = !isGhostOn }
                    )
                }
            }
            Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                CropButton(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.cancel), isPrimary = false, onClick = onCancel)
                CropButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = Res.string.photo_crop_reset),
                    isPrimary = false,
                    onClick = { placement = placement?.copy(zoom = 1f, offsetX = 0f, offsetY = 0f) }
                )
                CropButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = Res.string.done),
                    isPrimary = true,
                    onClick = { placement?.let { onDone(it.crop()) } }
                )
            }
        }
    }
}

@Composable
private fun CropFrame(
    image: ImageBitmap,
    ghostFileName: String?,
    placement: CropPlacement?,
    onPlacement: (CropPlacement) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(ratio = CROP_ASPECT, matchHeightConstraintsFirst = true)
            .clip(shape = RoundedCornerShape(size = 4.dp))
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val frameWidth = with(density) { maxWidth.toPx() }
        val frameHeight = with(density) { maxHeight.toPx() }
        val current = placement ?: CropPlacement(
            imageWidth = image.width.toFloat(),
            imageHeight = image.height.toFloat(),
            frameWidth = frameWidth,
            frameHeight = frameHeight
        )
        // Done crops to what's shown even before the photo's been touched.
        LaunchedEffect(key1 = frameWidth, key2 = frameHeight) {
            if (placement == null) onPlacement(current)
        }
        val latest by rememberUpdatedState(newValue = current)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(key1 = Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onPlacement(latest.moved(factor = zoom, panX = pan.x, panY = pan.y))
                    }
                }
                .pointerInput(key1 = Unit) {
                    detectTapGestures(onDoubleTap = { onPlacement(latest.copy(zoom = 1f, offsetX = 0f, offsetY = 0f)) })
                }
        ) {
            val shownWidth = with(density) { current.shownWidth.toDp() }
            val shownHeight = with(density) { current.shownHeight.toDp() }
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .requiredSize(width = shownWidth, height = shownHeight)
                    .graphicsLayer {
                        translationX = current.offsetX
                        translationY = current.offsetY
                    }
            )
            ghostFileName?.let { GhostImage(fileName = it) }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val line = Color.White.copy(alpha = 0.35f)
                for (index in 1..2) {
                    val x = size.width * index / 3
                    val y = size.height * index / 3
                    drawLine(color = line, start = Offset(x = x, y = 0f), end = Offset(x = x, y = size.height), strokeWidth = 1.dp.toPx())
                    drawLine(color = line, start = Offset(x = 0f, y = y), end = Offset(x = size.width, y = y), strokeWidth = 1.dp.toPx())
                }
            }
        }
    }
}

@Composable
private fun CropButton(modifier: Modifier = Modifier, text: String, isPrimary: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = if (isPrimary) colors.accent else Color.White.copy(alpha = 0.14f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = if (isPrimary) colors.onAccent else Color.White)
    }
}
