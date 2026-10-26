package org.thelazybattley.macrotrack.features.photos.ui

import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.tourTarget
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.photo_close_full_screen
import macrotrack.composeapp.generated.resources.photo_full_screen
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_compare_split
import macrotrack.composeapp.generated.resources.photo_after
import macrotrack.composeapp.generated.resources.photo_back_to_album
import macrotrack.composeapp.generated.resources.photo_before
import macrotrack.composeapp.generated.resources.photo_before_after
import macrotrack.composeapp.generated.resources.photo_choose_after
import macrotrack.composeapp.generated.resources.photo_choose_before
import macrotrack.composeapp.generated.resources.photo_compare_drag
import macrotrack.composeapp.generated.resources.photo_compare_showing
import macrotrack.composeapp.generated.resources.photo_days
import macrotrack.composeapp.generated.resources.photo_day_description
import macrotrack.composeapp.generated.resources.photo_mode_side
import macrotrack.composeapp.generated.resources.photo_mode_slider
import macrotrack.composeapp.generated.resources.photo_picker_change
import macrotrack.composeapp.generated.resources.photo_range
import macrotrack.composeapp.generated.resources.photo_stat_time
import macrotrack.composeapp.generated.resources.photo_stat_waist
import macrotrack.composeapp.generated.resources.photo_stat_waist_measured
import macrotrack.composeapp.generated.resources.photo_stat_waist_none
import macrotrack.composeapp.generated.resources.photo_stat_weight
import macrotrack.composeapp.generated.resources.photo_stat_weight_sub
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.features.photos.CompareMode
import org.thelazybattley.macrotrack.features.photos.ComparePick
import org.thelazybattley.macrotrack.features.photos.PhotoCompareViewModel
import org.thelazybattley.macrotrack.features.photos.PhotoCompareViewState
import org.thelazybattley.macrotrack.features.photos.PhotoItem
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun PhotoCompareScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<PhotoCompareViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isLoaded, key2 = viewState.summary) {
        if (viewState.isLoaded && viewState.summary?.photos.isNullOrEmpty()) onBack()
    }
    val before = viewState.before
    val after = viewState.after
    var isFullScreen by rememberSaveable { mutableStateOf(value = false) }
    if (isFullScreen && before != null && after != null) {
        FullScreenSideBySide(before = before, after = after, onClose = { isFullScreen = false })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            SquareIconButton(
                icon = { PhotoIcon(resource = Res.drawable.ic_chevron_left, size = 18) },
                description = stringResource(resource = Res.string.photo_back_to_album),
                onClick = onBack
            )
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = viewState.summary?.album?.name.orEmpty(),
                    style = typography.regular11,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = stringResource(resource = Res.string.photo_before_after), style = typography.bold18, color = colors.ink)
            }
        }
        if (before == null || after == null) return@Column
        PhotoSegmented(
            options = CompareMode.entries,
            selected = viewState.mode,
            label = { stringResource(resource = if (it == CompareMode.SLIDER) Res.string.photo_mode_slider else Res.string.photo_mode_side) },
            onSelect = viewModel::onModeSelected
        )
        Box(modifier = Modifier.tourTarget(target = TourTarget.PHOTO_COMPARE)) {
            when (viewState.mode) {
                CompareMode.SLIDER -> CompareSlider(
                    before = before,
                    after = after,
                    split = viewState.split,
                    isOverlayShown = viewState.summary?.album?.isDateAndWeightShown ?: true,
                    onSplitChanged = viewModel::onSplitChanged
                )
                CompareMode.SIDE_BY_SIDE -> SideBySide(
                    before = before,
                    after = after,
                    isOverlayShown = viewState.summary?.album?.isDateAndWeightShown ?: true,
                    onExpand = { isFullScreen = true }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            listOf(ComparePick.BEFORE to before, ComparePick.AFTER to after).forEach { (pick, item) ->
                PickerCard(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = if (pick == ComparePick.BEFORE) Res.string.photo_before else Res.string.photo_after).uppercase(),
                    item = item,
                    isOpen = viewState.picking == pick,
                    onClick = { viewModel.onPickToggled(pick = pick) }
                )
            }
        }
        viewState.picking?.let { picking ->
            PhotoStrip(viewState = viewState, picking = picking, onPick = viewModel::onPhotoPicked)
        }
        CompareStats(viewState = viewState, before = before, after = after)
    }
}

/** Drag anywhere on the photo, or tap, to move the divider; the before photo shows to its left. */
@Composable
private fun CompareSlider(
    before: PhotoItem,
    after: PhotoItem,
    split: Float,
    isOverlayShown: Boolean,
    onSplitChanged: (Float) -> Unit
) {
    val latestSplit by rememberUpdatedState(newValue = split)
    val description = stringResource(resource = Res.string.photo_compare_drag)
    val state = stringResource(resource = Res.string.photo_compare_showing, ((1 - split) * 100).roundToInt())
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio = 3f / 4f)
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .pointerInput(key1 = Unit) {
                detectTapGestures { offset -> onSplitChanged(offset.x / size.width) }
            }
            .pointerInput(key1 = Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    onSplitChanged(latestSplit + dragAmount / size.width)
                }
            }
            .semantics {
                contentDescription = description
                stateDescription = state
                progressBarRangeInfo = ProgressBarRangeInfo(current = split, range = 0f..1f, steps = 19)
                setProgress { value ->
                    onSplitChanged(value)
                    true
                }
            }
    ) {
        PhotoImage(modifier = Modifier.fillMaxSize(), fileName = before.photo.fileName, isThumbnail = false)
        PhotoImage(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(left = size.width * split) { this@drawWithContent.drawContent() }
                },
            fileName = after.photo.fileName,
            isThumbnail = false
        )
        Box(modifier = Modifier.align(alignment = Alignment.TopStart).padding(all = 12.dp)) {
            PhotoTag(text = stringResource(resource = Res.string.photo_before), isAccent = false)
        }
        Box(modifier = Modifier.align(alignment = Alignment.TopEnd).padding(all = 12.dp)) {
            PhotoTag(text = stringResource(resource = Res.string.photo_after), isAccent = true)
        }
        if (isOverlayShown) {
            Row(modifier = Modifier.align(alignment = Alignment.BottomCenter).fillMaxWidth().background(color = PhotoScrim)) {
                SliderCaption(modifier = Modifier.weight(weight = 1f), item = before, isEnd = false)
                SliderCaption(modifier = Modifier.weight(weight = 1f), item = after, isEnd = true)
            }
        }
        val lineX = maxWidth * split
        Box(
            modifier = Modifier
                .offset(x = lineX - 1.5.dp)
                .width(width = 3.dp)
                .fillMaxHeight()
                .background(color = Color.White)
        )
        Box(
            modifier = Modifier
                .offset(x = lineX - 22.dp, y = maxHeight * 0.45f - 22.dp)
                .size(size = 44.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .background(color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            PhotoIcon(resource = Res.drawable.ic_compare_split, size = 20, tint = Color(0xFF15171A))
        }
    }
}

@Composable
private fun SliderCaption(modifier: Modifier = Modifier, item: PhotoItem, isEnd: Boolean) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = if (isEnd) Alignment.End else Alignment.Start
    ) {
        Text(text = item.photo.date.toLongPhotoDate(), style = typography.bold11, color = Color.White, maxLines = 1)
        Text(text = photoWeightText(weight = item.weight), style = typography.bold18, color = Color.White, maxLines = 1)
    }
}

/** The pair edge to edge, a little taller than the photos so they're as big as the screen allows; tap for full screen. */
@Composable
private fun SideBySide(before: PhotoItem, after: PhotoItem, isOverlayShown: Boolean, onExpand: () -> Unit) {
    val expandLabel = stringResource(resource = Res.string.photo_full_screen)
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Row(
            modifier = Modifier
                .bleed(horizontal = 20.dp)
                .clickable(onClickLabel = expandLabel, onClick = onExpand),
            horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            listOf(before to false, after to true).forEach { (item, isAfter) ->
                PhotoTile(
                    modifier = Modifier.weight(weight = 1f).aspectRatio(ratio = 2f / 3f),
                    fileName = item.photo.fileName,
                    date = item.photo.date,
                    weight = item.weight,
                    isOverlayShown = isOverlayShown,
                    cornerRadius = 0.dp,
                    isThumbnail = false,
                    textStyle = typography.bold12,
                    topStart = {
                        PhotoTag(
                            text = stringResource(resource = if (isAfter) Res.string.photo_after else Res.string.photo_before),
                            isAccent = isAfter
                        )
                    }
                )
            }
        }
        Text(text = expandLabel, style = typography.regular11, color = colors.muted)
    }
}

/** Ignores the screen's side padding, so a row can run edge to edge. */
private fun Modifier.bleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val extra = (horizontal * 2).roundToPx()
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints = constraints.copy(minWidth = width, maxWidth = width))
    layout(width = constraints.maxWidth, height = placeable.height) { placeable.place(x = -extra / 2, y = 0) }
}

/** Both photos the full height of the screen; pinching or dragging moves the two together. Double-tap resets. */
@Composable
private fun FullScreenSideBySide(before: PhotoItem, after: PhotoItem, onClose: () -> Unit) {
    var scale by remember { mutableStateOf(value = 1f) }
    var offset by remember { mutableStateOf(value = Offset.Zero) }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(color = Color.Black)) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(key1 = Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(minimumValue = 1f, maximumValue = 4f)
                            offset = if (scale == 1f) Offset.Zero else offset + pan
                        }
                    }
                    .pointerInput(key1 = Unit) {
                        detectTapGestures(onDoubleTap = {
                            scale = 1f
                            offset = Offset.Zero
                        })
                    },
                horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                listOf(before to false, after to true).forEach { (item, isAfter) ->
                    Box(modifier = Modifier.weight(weight = 1f).fillMaxHeight().clipToBounds()) {
                        PhotoImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offset.x
                                    translationY = offset.y
                                },
                            fileName = item.photo.fileName,
                            isThumbnail = false
                        )
                        Box(modifier = Modifier.align(alignment = Alignment.TopStart).statusBarsPadding().padding(all = 12.dp)) {
                            PhotoTag(
                                text = stringResource(resource = if (isAfter) Res.string.photo_after else Res.string.photo_before),
                                isAccent = isAfter
                            )
                        }
                        SliderCaption(
                            modifier = Modifier
                                .align(alignment = Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(color = PhotoScrim)
                                .navigationBarsPadding(),
                            item = item,
                            isEnd = isAfter
                        )
                    }
                }
            }
            val closeLabel = stringResource(resource = Res.string.photo_close_full_screen)
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(all = 12.dp)
                    .size(size = 44.dp)
                    .clip(shape = CircleShape)
                    .background(color = PhotoScrim)
                    .clickable(onClick = onClose)
                    .semantics { contentDescription = closeLabel },
                contentAlignment = Alignment.Center
            ) {
                PhotoIcon(resource = Res.drawable.ic_close, size = 18, tint = Color.White)
            }
        }
    }
}

@Composable
private fun PickerCard(modifier: Modifier = Modifier, label: String, item: PhotoItem, isOpen: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = if (isOpen) 2.dp else 1.dp, color = if (isOpen) colors.accent else colors.line, shape = shape)
            .clickable(onClick = onClick)
            .semantics { selected = isOpen }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 1.dp)
    ) {
        Text(text = label, style = typography.bold11, color = colors.muted)
        Text(text = item.photo.date.toShortDayMonthDay(), style = typography.bold14, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.photo_picker_change, photoWeightText(weight = item.weight)),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun PhotoStrip(viewState: PhotoCompareViewState, picking: ComparePick, onPick: (Long) -> Unit) {
    val photos = viewState.summary?.photos.orEmpty()
    val current = if (picking == ComparePick.BEFORE) viewState.before else viewState.after
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SheetLabel(text = stringResource(resource = if (picking == ComparePick.BEFORE) Res.string.photo_choose_before else Res.string.photo_choose_after))
        Row(
            modifier = Modifier.horizontalScroll(state = rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            photos.forEach { item ->
                val isSelected = item == current
                val description = stringResource(
                    resource = Res.string.photo_day_description,
                    item.photo.date.toLongPhotoDate(),
                    photoWeightText(weight = item.weight)
                )
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 88.dp)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .then(if (isSelected) Modifier.border(width = 3.dp, color = colors.accent, shape = RoundedCornerShape(size = 10.dp)) else Modifier)
                        .clickable { onPick(item.photo.id) }
                        .semantics {
                            contentDescription = description
                            selected = isSelected
                        }
                ) {
                    PhotoImage(modifier = Modifier.fillMaxSize(), fileName = item.photo.fileName, isThumbnail = true)
                    Column(
                        modifier = Modifier
                            .align(alignment = Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(color = PhotoScrim)
                            .padding(horizontal = 4.dp, vertical = 3.dp)
                    ) {
                        Text(text = item.photo.date.toAbbreviatedMonthDay(), style = typography.bold10, color = Color.White, maxLines = 1)
                        Text(text = photoWeightText(weight = item.weight), style = typography.bold10, color = Color.White, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareStats(viewState: PhotoCompareViewState, before: PhotoItem, after: PhotoItem) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.photo_stat_weight),
            value = viewState.weightChangeKg?.formatSignedWeightChange() ?: "—",
            sub = stringResource(resource = Res.string.photo_stat_weight_sub),
            isOutlined = true
        )
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.photo_stat_time),
            value = pluralStringResource(resource = Res.plurals.photo_days, quantity = viewState.days, viewState.days),
            sub = stringResource(
                resource = Res.string.photo_range,
                before.photo.date.toAbbreviatedMonthDay(),
                after.photo.date.toAbbreviatedMonthDay()
            ),
            isOutlined = true
        )
        val waistBefore = viewState.waistBefore
        val waistAfter = viewState.waistAfter
        val waistChange = viewState.waistChangeCm
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.photo_stat_waist),
            value = waistChange?.let { change ->
                val tenths = round(viewState.unit.fromCm(cm = change) * 10).toInt()
                val sign = if (tenths < 0) "−" else if (tenths > 0) "+" else ""
                stringResource(
                    resource = Res.string.value_unit,
                    "$sign${abs(tenths) / 10}.${abs(tenths) % 10}",
                    stringResource(resource = viewState.unit.label)
                )
            } ?: "—",
            sub = if (waistBefore != null && waistAfter != null) {
                stringResource(
                    resource = Res.string.photo_stat_waist_measured,
                    waistBefore.date.toAbbreviatedMonthDay(),
                    waistAfter.date.toAbbreviatedMonthDay()
                )
            } else {
                stringResource(resource = Res.string.photo_stat_waist_none)
            },
            isOutlined = true
        )
    }
}
