package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.delete
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_compare_split
import macrotrack.composeapp.generated.resources.ic_image
import macrotrack.composeapp.generated.resources.ic_more_vertical
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.photo_add
import macrotrack.composeapp.generated.resources.photo_album_delete
import macrotrack.composeapp.generated.resources.photo_album_delete_body
import macrotrack.composeapp.generated.resources.photo_album_edit
import macrotrack.composeapp.generated.resources.photo_album_options
import macrotrack.composeapp.generated.resources.photo_album_range
import macrotrack.composeapp.generated.resources.photo_back_to_albums
import macrotrack.composeapp.generated.resources.photo_before_after
import macrotrack.composeapp.generated.resources.photo_change
import macrotrack.composeapp.generated.resources.photo_count
import macrotrack.composeapp.generated.resources.photo_days
import macrotrack.composeapp.generated.resources.photo_delete
import macrotrack.composeapp.generated.resources.photo_delete_body
import macrotrack.composeapp.generated.resources.photo_delta_since
import macrotrack.composeapp.generated.resources.photo_empty_album
import macrotrack.composeapp.generated.resources.photo_empty_album_body
import macrotrack.composeapp.generated.resources.photo_first
import macrotrack.composeapp.generated.resources.photo_first_photo
import macrotrack.composeapp.generated.resources.photo_grid_hint
import macrotrack.composeapp.generated.resources.photo_compare_picked
import macrotrack.composeapp.generated.resources.photo_picked
import macrotrack.composeapp.generated.resources.photo_picked_body
import macrotrack.composeapp.generated.resources.photo_select
import macrotrack.composeapp.generated.resources.photo_select_hint
import macrotrack.composeapp.generated.resources.photo_item_description
import macrotrack.composeapp.generated.resources.photo_latest
import macrotrack.composeapp.generated.resources.photo_new
import macrotrack.composeapp.generated.resources.photo_no_weight_logged
import macrotrack.composeapp.generated.resources.photo_saved_many
import macrotrack.composeapp.generated.resources.photo_saved_one
import macrotrack.composeapp.generated.resources.photo_view_grid
import macrotrack.composeapp.generated.resources.photo_view_timeline
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.photos.AlbumSummary
import org.thelazybattley.macrotrack.features.photos.AlbumView
import org.thelazybattley.macrotrack.features.photos.PhotoAlbumViewModel
import org.thelazybattley.macrotrack.features.photos.PhotoAlbumViewState
import org.thelazybattley.macrotrack.features.photos.PhotoItem
import org.thelazybattley.macrotrack.features.photos.SavedPhotos
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** How long the "Saved to …" line stays after saving. */
private const val SAVED_NOTE_MILLIS = 4_000L

@Composable
fun PhotoAlbumScreen(
    isAddOpen: Boolean,
    onBack: () -> Unit,
    onOpenCompare: (albumId: Long, beforeId: Long?, afterId: Long?, isSideBySide: Boolean) -> Unit,
    onOpenCamera: (albumId: Long) -> Unit,
    onOpenSave: () -> Unit
) {
    val viewModel = koinViewModel<PhotoAlbumViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    var isAddSheetOpen by rememberSaveable { mutableStateOf(value = isAddOpen) }
    var isEditOpen by rememberSaveable { mutableStateOf(value = false) }
    var isDeleteAlbumOpen by rememberSaveable { mutableStateOf(value = false) }
    var photoToDelete by remember { mutableStateOf<ProgressPhoto?>(value = null) }
    // Photos picked to see side by side, in the order tapped; null when not picking.
    var selection by remember { mutableStateOf<List<Long>?>(value = null) }
    val libraryImport = rememberLibraryImport(onImported = onOpenSave)
    val leave = {
        viewModel.onLeave()
        onBack()
    }
    LaunchedEffect(key1 = viewModel) {
        viewModel.closed.collect { onBack() }
    }
    // Deleted from somewhere else, or never there.
    LaunchedEffect(key1 = viewState.isLoaded, key2 = viewState.summary) {
        if (viewState.isLoaded && viewState.summary == null) onBack()
    }
    val summary = viewState.summary
    Box(modifier = Modifier.fillMaxSize().background(color = colors.background)) {
        if (summary != null) {
            PhotoAlbumContent(
                viewState = viewState,
                summary = summary,
                onBack = leave,
                onEdit = { isEditOpen = true },
                onDelete = { isDeleteAlbumOpen = true },
                onAddPhoto = { isAddSheetOpen = true },
                onViewSelected = viewModel::onViewSelected,
                onOpenCompare = { afterId -> onOpenCompare(viewModel.albumId, null, afterId, false) },
                onDeletePhoto = { photoToDelete = it },
                selection = selection,
                onSelectingChanged = { isSelecting ->
                    selection = if (isSelecting) emptyList() else null
                    if (isSelecting) viewModel.onViewSelected(view = AlbumView.GRID)
                },
                onToggleSelected = { id ->
                    val current = selection.orEmpty()
                    // A third pick replaces the first, so the last two tapped are always the pair.
                    selection = when {
                        id in current -> current - id
                        current.size >= COMPARE_PICKS -> current.drop(n = 1) + id
                        else -> current + id
                    }
                }
            )
            val picked = selection
            if (picked != null) {
                CompareSelectionBar(
                    modifier = Modifier.align(alignment = Alignment.BottomCenter),
                    count = picked.size,
                    onCompare = {
                        val pair = picked
                            .mapNotNull { id -> summary.photos.find { it.photo.id == id } }
                            .sortedWith(compareBy({ it.photo.date }, { it.photo.createdAtMillis }))
                        if (pair.size < COMPARE_PICKS) return@CompareSelectionBar
                        val (before, after) = pair
                        selection = null
                        onOpenCompare(viewModel.albumId, before.photo.id, after.photo.id, true)
                    }
                )
            }
        }
        ImportingOverlay(isVisible = libraryImport.isImporting)
    }
    if (summary != null && isAddSheetOpen) {
        AddPhotoSheet(
            albumName = summary.album.name,
            onDismiss = { isAddSheetOpen = false },
            onCamera = {
                isAddSheetOpen = false
                onOpenCamera(summary.album.id)
            },
            onLibrary = {
                isAddSheetOpen = false
                libraryImport.start(albumId = summary.album.id, pose = summary.usualPose)
            }
        )
    }
    if (summary != null && isEditOpen) {
        AlbumSheet(
            album = summary.album,
            onDismiss = { isEditOpen = false },
            onSave = { album ->
                isEditOpen = false
                viewModel.onSaveAlbum(album = album)
            }
        )
    }
    if (summary != null && isDeleteAlbumOpen) {
        ConfirmDeleteDialog(
            title = stringResource(resource = Res.string.photo_album_delete),
            body = pluralStringResource(
                resource = Res.plurals.photo_album_delete_body,
                quantity = summary.photos.size,
                summary.album.name,
                summary.photos.size
            ),
            onDismiss = { isDeleteAlbumOpen = false },
            onConfirm = {
                isDeleteAlbumOpen = false
                viewModel.onDeleteAlbum()
            }
        )
    }
    photoToDelete?.let { photo ->
        ConfirmDeleteDialog(
            title = stringResource(resource = Res.string.photo_delete),
            body = stringResource(resource = Res.string.photo_delete_body, photo.date.toLongPhotoDate()),
            onDismiss = { photoToDelete = null },
            onConfirm = {
                photoToDelete = null
                viewModel.onDeletePhoto(photo = photo)
            }
        )
    }
}

@Composable
private fun PhotoAlbumContent(
    viewState: PhotoAlbumViewState,
    summary: AlbumSummary,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddPhoto: () -> Unit,
    onViewSelected: (AlbumView) -> Unit,
    onOpenCompare: (afterId: Long?) -> Unit,
    onDeletePhoto: (ProgressPhoto) -> Unit,
    selection: List<Long>?,
    onSelectingChanged: (Boolean) -> Unit,
    onToggleSelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            // Room for the Compare bar while picking.
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = if (selection != null) 104.dp else 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        AlbumHeader(summary = summary, onBack = onBack, onEdit = onEdit, onDelete = onDelete)
        viewState.saved?.let { saved -> SavedNote(saved = saved, albumName = summary.album.name) }
        if (summary.photos.isEmpty()) {
            EmptyAlbum(onAddPhoto = onAddPhoto)
            return@Column
        }
        AlbumStats(summary = summary)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            WideButton(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(resource = Res.string.photo_before_after),
                icon = { PhotoIcon(resource = Res.drawable.ic_compare_split, size = 18, tint = colors.accentInk) },
                isPrimary = false,
                onClick = { onOpenCompare(null) }
            )
            WideButton(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(resource = Res.string.photo_add),
                icon = { PhotoIcon(resource = Res.drawable.ic_plus, size = 18, tint = colors.onAccent) },
                isPrimary = true,
                onClick = onAddPhoto
            )
        }
        PhotoSegmented(
            options = AlbumView.entries,
            selected = viewState.view,
            label = { stringResource(resource = if (it == AlbumView.GRID) Res.string.photo_view_grid else Res.string.photo_view_timeline) },
            onSelect = onViewSelected
        )
        when (viewState.view) {
            AlbumView.GRID -> AlbumGrid(
                summary = summary,
                selection = selection,
                onSelectingChanged = onSelectingChanged,
                onOpen = { if (selection != null) onToggleSelected(it.photo.id) else onOpenCompare(it.photo.id) },
                onLongPress = { if (selection == null) onDeletePhoto(it.photo) }
            )
            AlbumView.TIMELINE -> AlbumTimeline(
                summary = summary,
                onOpen = { onOpenCompare(it.photo.id) },
                onLongPress = { onDeletePhoto(it.photo) }
            )
        }
    }
}

@Composable
private fun AlbumHeader(summary: AlbumSummary, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var isMenuOpen by remember { mutableStateOf(value = false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        SquareIconButton(
            icon = { PhotoIcon(resource = Res.drawable.ic_chevron_left, size = 18) },
            description = stringResource(resource = Res.string.photo_back_to_albums),
            onClick = onBack
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            val first = summary.photos.firstOrNull()
            val latest = summary.photos.lastOrNull()
            if (first != null && latest != null) {
                Text(
                    text = pluralStringResource(
                        resource = Res.plurals.photo_album_range,
                        quantity = summary.photos.size,
                        summary.photos.size,
                        first.photo.date.toAbbreviatedMonthDay(),
                        latest.photo.date.toAbbreviatedMonthDay()
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            Text(
                text = summary.album.name,
                style = typography.bold18,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            SquareIconButton(
                icon = { PhotoIcon(resource = Res.drawable.ic_more_vertical, size = 18) },
                description = stringResource(resource = Res.string.photo_album_options),
                onClick = { isMenuOpen = true }
            )
            DropdownMenu(
                expanded = isMenuOpen,
                onDismissRequest = { isMenuOpen = false },
                containerColor = colors.surface
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(resource = Res.string.photo_album_edit), style = typography.bold14, color = colors.ink) },
                    onClick = {
                        isMenuOpen = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(resource = Res.string.photo_album_delete), style = typography.bold14, color = colors.danger) },
                    onClick = {
                        isMenuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun SavedNote(saved: SavedPhotos, albumName: String) {
    var isVisible by remember(saved) { mutableStateOf(value = true) }
    LaunchedEffect(key1 = saved) {
        delay(timeMillis = SAVED_NOTE_MILLIS)
        isVisible = false
    }
    if (!isVisible) return
    val text = if (saved.ids.size > 1) {
        stringResource(resource = Res.string.photo_saved_many, saved.ids.size, albumName)
    } else {
        stringResource(
            resource = Res.string.photo_saved_one,
            albumName,
            saved.firstDate.toAbbreviatedMonthDay(),
            photoWeightText(weight = saved.firstWeightKg?.let { Weight(date = saved.firstDate, weight = it) })
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.accentSoft)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        PhotoIcon(resource = Res.drawable.ic_check, size = 18, tint = colors.accentInk)
        Text(text = text, style = typography.bold13, color = colors.accentInk, modifier = Modifier.weight(weight = 1f))
    }
}

@Composable
private fun AlbumStats(summary: AlbumSummary) {
    val first = summary.firstWeighed
    val latest = summary.latestWeighed
    val change = summary.changeKg
    if (first == null || latest == null || change == null) return
    val days = (latest.photo.date.toEpochDays() - first.photo.date.toEpochDays()).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.hero)
            .padding(all = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.photo_first),
            value = photoWeightText(weight = first.weight),
            sub = first.photo.date.toAbbreviatedMonthDay()
        )
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.photo_latest),
            value = photoWeightText(weight = latest.weight),
            sub = latest.photo.date.toAbbreviatedMonthDay()
        )
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.photo_change),
            value = change.formatSignedWeightChange(),
            sub = pluralStringResource(resource = Res.plurals.photo_days, quantity = days, days)
        )
    }
}

@Composable
internal fun StatTile(modifier: Modifier = Modifier, label: String, value: String, sub: String, isOutlined: Boolean = false) {
    val shape = RoundedCornerShape(size = if (isOutlined) 16.dp else 14.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .then(if (isOutlined) Modifier.border(width = 1.dp, color = colors.line, shape = shape) else Modifier)
            .padding(all = 10.dp)
    ) {
        Text(text = label, style = typography.regular11, color = colors.muted, maxLines = 1)
        Text(text = value, style = typography.bold16, color = colors.ink, maxLines = 1)
        Text(text = sub, style = typography.regular11, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AlbumGrid(
    summary: AlbumSummary,
    selection: List<Long>?,
    onSelectingChanged: (Boolean) -> Unit,
    onOpen: (PhotoItem) -> Unit,
    onLongPress: (PhotoItem) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Text(
            text = stringResource(resource = if (selection != null) Res.string.photo_select_hint else Res.string.photo_grid_hint),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.weight(weight = 1f)
        )
        if (summary.photos.size >= COMPARE_PICKS) {
            Chip(
                text = stringResource(resource = if (selection != null) Res.string.cancel else Res.string.photo_select),
                isSelected = selection != null,
                onClick = { onSelectingChanged(selection == null) }
            )
        }
    }
    summary.photos.reversed()
        .groupBy { it.photo.date.year to it.photo.date.month }
        .forEach { (_, items) ->
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = items.first().photo.date.toMonthYear(),
                        style = typography.bold14,
                        color = colors.ink,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(
                        text = pluralStringResource(resource = Res.plurals.photo_count, quantity = items.size, items.size),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
                items.chunked(size = 3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                        row.forEach { item ->
                            GridPhoto(
                                modifier = Modifier.weight(weight = 1f),
                                item = item,
                                isOverlayShown = summary.album.isDateAndWeightShown,
                                pick = selection?.let { picked -> picked.indexOf(item.photo.id).takeIf { it >= 0 }?.plus(1) ?: 0 },
                                onOpen = { onOpen(item) },
                                onLongPress = { onLongPress(item) }
                            )
                        }
                        repeat(times = 3 - row.size) { Box(modifier = Modifier.weight(weight = 1f)) }
                    }
                }
            }
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridPhoto(
    modifier: Modifier = Modifier,
    item: PhotoItem,
    isOverlayShown: Boolean,
    /** While picking photos to compare: 0 for not picked, otherwise the order it was picked in. */
    pick: Int?,
    onOpen: () -> Unit,
    onLongPress: () -> Unit
) {
    val description = stringResource(
        resource = Res.string.photo_item_description,
        stringResource(resource = item.photo.pose.label),
        item.photo.date.toLongPhotoDate(),
        photoWeightText(weight = item.weight)
    )
    val deleteLabel = stringResource(resource = Res.string.photo_delete)
    PhotoTile(
        modifier = modifier
            .aspectRatio(ratio = 3f / 4f)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .then(
                if (pick != null && pick > 0) {
                    Modifier.border(width = 3.dp, color = colors.accent, shape = RoundedCornerShape(size = 12.dp))
                } else {
                    Modifier
                }
            )
            .combinedClickable(onClick = onOpen, onLongClick = onLongPress)
            .semantics {
                contentDescription = description
                onLongClick(label = deleteLabel) { onLongPress(); true }
            },
        fileName = item.photo.fileName,
        date = item.photo.date,
        weight = item.weight,
        isOverlayShown = isOverlayShown,
        topStart = when {
            pick != null -> {
                { PickBadge(number = pick) }
            }
            item.isNew -> {
                { PhotoTag(text = stringResource(resource = Res.string.photo_new), isAccent = true) }
            }
            else -> null
        }
    )
}

/** A numbered circle on a picked photo, an empty ring on the rest. */
@Composable
private fun PickBadge(number: Int) {
    Box(
        modifier = Modifier
            .size(size = 24.dp)
            .clip(shape = CircleShape)
            .background(color = if (number > 0) colors.accent else PhotoScrim)
            .border(width = 2.dp, color = if (number > 0) colors.accent else Color.White, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (number > 0) Text(text = number.toString(), style = typography.bold12, color = colors.onAccent)
    }
}

/** Shown while picking: how many are picked, and Compare once there are two. */
@Composable
private fun CompareSelectionBar(modifier: Modifier = Modifier, count: Int, onCompare: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.surface)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = Res.string.photo_picked, count, COMPARE_PICKS), style = typography.bold14, color = colors.ink)
            Text(text = stringResource(resource = Res.string.photo_picked_body), style = typography.regular11, color = colors.muted)
        }
        SheetButton(
            modifier = Modifier.width(width = 150.dp),
            text = stringResource(resource = Res.string.photo_compare_picked),
            isPrimary = true,
            isEnabled = count == COMPARE_PICKS,
            onClick = onCompare
        )
    }
}

/** Side by side takes two photos. */
private const val COMPARE_PICKS = 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumTimeline(summary: AlbumSummary, onOpen: (PhotoItem) -> Unit, onLongPress: (PhotoItem) -> Unit) {
    // Each row says how far the weight moved since the photo before it that had a weigh-in.
    val deltas = buildList {
        var previous: PhotoItem? = null
        summary.photos.forEach { item ->
            add(item to previous)
            if (item.weight != null) previous = item
        }
    }.reversed()
    val shape = RoundedCornerShape(size = 18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
    ) {
        deltas.forEachIndexed { index, (item, previous) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = { onOpen(item) }, onLongClick = { onLongPress(item) })
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                PhotoImage(
                    modifier = Modifier.size(width = 60.dp, height = 80.dp).clip(shape = RoundedCornerShape(size = 10.dp)),
                    fileName = item.photo.fileName,
                    isThumbnail = true
                )
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                    Text(text = item.photo.date.toLongPhotoDate(), style = typography.bold13, color = colors.ink)
                    Text(text = photoWeightText(weight = item.weight), style = typography.bold18, color = colors.ink)
                    val weight = item.weight
                    val before = previous?.weight
                    Text(
                        text = when {
                            weight == null -> stringResource(resource = Res.string.photo_no_weight_logged)
                            before == null -> stringResource(resource = Res.string.photo_first_photo)
                            else -> stringResource(
                                resource = Res.string.photo_delta_since,
                                (weight.weight - before.weight).formatArrowChange(),
                                previous.photo.date.toAbbreviatedMonthDay()
                            )
                        },
                        style = typography.bold11,
                        color = if (weight != null && before != null) colors.ink else colors.muted
                    )
                }
                Text(
                    text = stringResource(resource = item.photo.pose.label),
                    style = typography.bold11,
                    color = colors.muted,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .background(color = colors.background)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
                PhotoIcon(resource = Res.drawable.ic_chevron_right, size = 16, tint = colors.faint)
            }
            if (index < deltas.lastIndex) HorizontalDivider(color = colors.divider)
        }
    }
}

/** "↓ 0.4 kg", "↑ 0.2 lb" or "0.0 kg". */
@Composable
private fun Double.formatArrowChange(): String {
    val text = formatSignedWeightChange().removePrefix(prefix = "−").removePrefix(prefix = "+")
    return when {
        text.startsWith(prefix = "0.0") -> text
        this < 0 -> "↓ $text"
        else -> "↑ $text"
    }
}

@Composable
private fun EmptyAlbum(onAddPhoto: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp)
            .dashedBorder(color = colors.dashed, width = 1.5.dp, cornerRadius = 24.dp)
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 12.dp, alignment = Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier.size(size = 56.dp).clip(shape = RoundedCornerShape(size = 18.dp)).background(color = colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            PhotoIcon(resource = Res.drawable.ic_image, size = 26, tint = colors.accentInk)
        }
        Text(text = stringResource(resource = Res.string.photo_empty_album), style = typography.bold18, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.photo_empty_album_body),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
        WideButton(text = stringResource(resource = Res.string.photo_add), icon = null, isPrimary = true, onClick = onAddPhoto)
    }
}

@Composable
internal fun WideButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: (@Composable () -> Unit)?,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = if (isPrimary) colors.accent else colors.accentSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        icon?.invoke()
        Text(text = text, style = typography.bold14, color = if (isPrimary) colors.onAccent else colors.accentInk)
    }
}

@Composable
internal fun ConfirmDeleteDialog(title: String, body: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(text = title, style = typography.bold18, color = colors.ink) },
        text = { Text(text = body, style = typography.regular13, color = colors.muted) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(resource = Res.string.delete), style = typography.bold14, color = colors.danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(resource = Res.string.cancel), style = typography.bold14, color = colors.ink)
            }
        }
    )
}
