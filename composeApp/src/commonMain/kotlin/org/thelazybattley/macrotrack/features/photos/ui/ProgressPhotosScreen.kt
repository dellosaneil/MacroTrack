package org.thelazybattley.macrotrack.features.photos.ui

import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.tourTarget
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_compare_split
import macrotrack.composeapp.generated.resources.ic_image
import macrotrack.composeapp.generated.resources.ic_lock
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.photo
import macrotrack.composeapp.generated.resources.photo_album_change_since_first
import macrotrack.composeapp.generated.resources.photo_album_count
import macrotrack.composeapp.generated.resources.photo_album_empty
import macrotrack.composeapp.generated.resources.photo_album_meta
import macrotrack.composeapp.generated.resources.photo_album_new
import macrotrack.composeapp.generated.resources.photo_albums
import macrotrack.composeapp.generated.resources.photo_compare_slider
import macrotrack.composeapp.generated.resources.photo_count
import macrotrack.composeapp.generated.resources.photo_days
import macrotrack.composeapp.generated.resources.photo_first
import macrotrack.composeapp.generated.resources.photo_first_vs_latest
import macrotrack.composeapp.generated.resources.photo_first_photo_prompt
import macrotrack.composeapp.generated.resources.photo_latest
import macrotrack.composeapp.generated.resources.photo_no_photos_yet
import macrotrack.composeapp.generated.resources.photo_privacy
import macrotrack.composeapp.generated.resources.photo_privacy_title
import macrotrack.composeapp.generated.resources.photo_range
import macrotrack.composeapp.generated.resources.photo_start_album
import macrotrack.composeapp.generated.resources.photo_start_album_body
import macrotrack.composeapp.generated.resources.progress_photos
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.photos.AlbumSummary
import org.thelazybattley.macrotrack.features.photos.ProgressPhotosViewModel
import org.thelazybattley.macrotrack.features.photos.ProgressPhotosViewState
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProgressPhotosScreen(
    isNewAlbumOpen: Boolean,
    onBack: () -> Unit,
    onOpenAlbum: (albumId: Long, isAddOpen: Boolean) -> Unit,
    onOpenCompare: (albumId: Long) -> Unit,
    onOpenCamera: (albumId: Long?) -> Unit,
    onOpenSave: () -> Unit
) {
    val viewModel = koinViewModel<ProgressPhotosViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    var isAlbumSheetOpen by rememberSaveable { mutableStateOf(value = isNewAlbumOpen) }
    var isAddSheetOpen by rememberSaveable { mutableStateOf(value = false) }
    val libraryImport = rememberLibraryImport(onImported = onOpenSave)
    LaunchedEffect(key1 = viewModel) {
        viewModel.createdAlbum.collect { id -> onOpenAlbum(id, false) }
    }
    Box(modifier = Modifier.fillMaxSize().background(color = colors.background)) {
        ProgressPhotosContent(
            viewState = viewState,
            onBack = onBack,
            onAddPhoto = { if (viewState.defaultAlbum == null) isAlbumSheetOpen = true else isAddSheetOpen = true },
            onOpenAlbum = { onOpenAlbum(it, false) },
            onOpenCompare = onOpenCompare,
            onNewAlbum = { isAlbumSheetOpen = true }
        )
        ImportingOverlay(isVisible = libraryImport.isImporting)
    }
    if (isAlbumSheetOpen) {
        AlbumSheet(
            album = null,
            onDismiss = { isAlbumSheetOpen = false },
            onSave = { album ->
                isAlbumSheetOpen = false
                viewModel.onCreateAlbum(album = album)
            }
        )
    }
    val target = viewState.defaultAlbum
    if (isAddSheetOpen && target != null) {
        AddPhotoSheet(
            albumName = target.album.name,
            onDismiss = { isAddSheetOpen = false },
            onCamera = {
                isAddSheetOpen = false
                onOpenCamera(target.album.id)
            },
            onLibrary = {
                isAddSheetOpen = false
                libraryImport.start(albumId = target.album.id, pose = target.usualPose)
            }
        )
    }
}

@Composable
private fun ProgressPhotosContent(
    viewState: ProgressPhotosViewState,
    onBack: () -> Unit,
    onAddPhoto: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenCompare: (Long) -> Unit,
    onNewAlbum: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            SquareIconButton(
                icon = { PhotoIcon(resource = Res.drawable.ic_chevron_left, size = 18) },
                description = stringResource(resource = Res.string.back),
                onClick = onBack
            )
            Text(
                text = stringResource(resource = Res.string.progress_photos),
                style = typography.bold18,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            AccentPillButton(text = stringResource(resource = Res.string.photo), onClick = onAddPhoto)
        }
        viewState.hero?.let { hero ->
            Box(modifier = Modifier.tourTarget(target = TourTarget.PHOTOS_HERO)) {
                HeroCard(summary = hero, onCompare = { onOpenCompare(hero.album.id) })
            }
        }
        if (viewState.isLoaded && viewState.albums.isEmpty()) {
            Box(modifier = Modifier.tourTarget(target = TourTarget.PHOTOS_ALBUMS)) {
                StartAlbumCard(onNewAlbum = onNewAlbum)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = Res.string.photo_albums),
                style = typography.bold16,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = pluralStringResource(resource = Res.plurals.photo_count, quantity = viewState.photoCount, viewState.photoCount),
                style = typography.regular11,
                color = colors.muted
            )
        }
        // Two to a row, with New album taking the next free spot.
        val tiles: List<AlbumSummary?> = viewState.albums + null
        Column(
            modifier = Modifier.tourTarget(target = TourTarget.PHOTOS_ALBUMS),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            tiles.chunked(size = 2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                    row.forEach { summary ->
                        if (summary == null) {
                            NewAlbumTile(modifier = Modifier.weight(weight = 1f), onClick = onNewAlbum)
                        } else {
                            AlbumTile(modifier = Modifier.weight(weight = 1f), summary = summary, onClick = { onOpenAlbum(summary.album.id) })
                        }
                    }
                    if (row.size == 1) Box(modifier = Modifier.weight(weight = 1f))
                }
            }
        }
        PrivacyNote()
    }
}

@Composable
private fun HeroCard(summary: AlbumSummary, onCompare: () -> Unit) {
    val first = summary.firstWeighed ?: return
    val latest = summary.latestWeighed ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(resource = Res.string.photo_first_vs_latest, summary.album.name),
                    style = typography.regular11,
                    color = colors.muted,
                    maxLines = 1
                )
                Text(text = (summary.changeKg ?: 0.0).formatSignedWeightChange(), style = typography.bold24, color = colors.ink)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(
                        resource = Res.string.photo_range,
                        first.photo.date.toAbbreviatedMonthDay(),
                        latest.photo.date.toAbbreviatedMonthDay()
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
                val days = (latest.photo.date.toEpochDays() - first.photo.date.toEpochDays()).toInt()
                Text(text = pluralStringResource(resource = Res.plurals.photo_days, quantity = days, days), style = typography.bold13, color = colors.ink)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            listOf(first to Res.string.photo_first, latest to Res.string.photo_latest).forEach { (item, tag) ->
                PhotoTile(
                    modifier = Modifier.weight(weight = 1f).aspectRatio(ratio = 3f / 4f),
                    fileName = item.photo.fileName,
                    date = item.photo.date,
                    weight = item.weight,
                    isOverlayShown = true,
                    cornerRadius = 16.dp,
                    textStyle = typography.bold12,
                    topStart = { PhotoTag(text = stringResource(resource = tag), isAccent = false) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.surface)
                .clickable(onClick = onCompare),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            PhotoIcon(resource = Res.drawable.ic_compare_split, size = 18)
            Text(text = stringResource(resource = Res.string.photo_compare_slider), style = typography.bold14, color = colors.ink)
        }
    }
}

@Composable
private fun AlbumTile(modifier: Modifier = Modifier, summary: AlbumSummary, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    val count = summary.photos.size
    val description = pluralStringResource(resource = Res.plurals.photo_album_count, quantity = count, summary.album.name, count)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        val latest = summary.latest
        if (latest == null) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(ratio = 4f / 5f).background(color = colors.line),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                    PhotoIcon(resource = Res.drawable.ic_image, size = 26, tint = colors.muted)
                    Text(text = stringResource(resource = Res.string.photo_no_photos_yet), style = typography.regular11, color = colors.muted)
                }
            }
        } else {
            PhotoTile(
                modifier = Modifier.fillMaxWidth().aspectRatio(ratio = 4f / 5f),
                fileName = latest.photo.fileName,
                date = latest.photo.date,
                weight = latest.weight,
                isOverlayShown = summary.album.isDateAndWeightShown,
                cornerRadius = 0.dp
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = summary.album.name, style = typography.bold14, color = colors.ink, maxLines = 2)
            Text(
                text = summary.photos.firstOrNull()?.let { first ->
                    pluralStringResource(resource = Res.plurals.photo_album_meta, quantity = count, count, first.photo.date.toAbbreviatedMonthDay())
                } ?: stringResource(resource = Res.string.photo_album_empty),
                style = typography.regular11,
                color = colors.muted
            )
            val change = summary.changeKg
            if (change != null) {
                Text(
                    text = stringResource(resource = Res.string.photo_album_change_since_first, change.formatSignedWeightChange()),
                    style = typography.bold11,
                    color = colors.accentInk
                )
            } else if (count == 0) {
                Text(text = stringResource(resource = Res.string.photo_first_photo_prompt), style = typography.bold11, color = colors.muted)
            }
        }
    }
}

@Composable
private fun NewAlbumTile(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .height(height = 240.dp)
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .dashedBorder(color = colors.dashed, width = 1.5.dp, cornerRadius = 18.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier.size(size = 44.dp).clip(shape = RoundedCornerShape(size = 14.dp)).background(color = colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            PhotoIcon(resource = Res.drawable.ic_plus, size = 20, tint = colors.accentInk)
        }
        Text(text = stringResource(resource = Res.string.photo_album_new), style = typography.bold14, color = colors.accentInk)
    }
}

@Composable
private fun StartAlbumCard(onNewAlbum: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Text(text = stringResource(resource = Res.string.photo_start_album), style = typography.bold18, color = colors.ink)
        Text(text = stringResource(resource = Res.string.photo_start_album_body), style = typography.regular13, color = colors.muted)
        AccentPillButton(text = stringResource(resource = Res.string.photo_album_new), onClick = onNewAlbum)
    }
}

@Composable
internal fun PrivacyNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier.size(size = 32.dp).clip(shape = RoundedCornerShape(size = 10.dp)).background(color = colors.nightSoft),
            contentAlignment = Alignment.Center
        ) {
            PhotoIcon(resource = Res.drawable.ic_lock, size = 16, tint = colors.nightInk)
        }
        Column {
            Text(text = stringResource(resource = Res.string.photo_privacy_title), style = typography.bold12, color = colors.ink)
            Text(text = stringResource(resource = Res.string.photo_privacy), style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
internal fun PhotoIcon(
    resource: org.jetbrains.compose.resources.DrawableResource,
    size: Int,
    tint: androidx.compose.ui.graphics.Color = colors.ink
) {
    Icon(painter = painterResource(resource = resource), contentDescription = null, tint = tint, modifier = Modifier.size(size = size.dp))
}

/** The design's 40 dp outlined square button, for Back and the album's options. */
@Composable
internal fun SquareIconButton(icon: @Composable () -> Unit, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = Modifier
            .size(size = 40.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/** "+ Photo": the accent button in a header. */
@Composable
internal fun AccentPillButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(height = 40.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        PhotoIcon(resource = Res.drawable.ic_plus, size = 16, tint = colors.onAccent)
        Text(text = text, style = typography.bold14, color = colors.onAccent)
    }
}
