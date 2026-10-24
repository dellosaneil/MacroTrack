package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_crop
import macrotrack.composeapp.generated.resources.photo_crop
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.photo_album
import macrotrack.composeapp.generated.resources.photo_album_for_all
import macrotrack.composeapp.generated.resources.photo_album_new
import macrotrack.composeapp.generated.resources.photo_calendar_taken
import macrotrack.composeapp.generated.resources.photo_calendar_weight
import macrotrack.composeapp.generated.resources.photo_count
import macrotrack.composeapp.generated.resources.photo_date
import macrotrack.composeapp.generated.resources.photo_date_change
import macrotrack.composeapp.generated.resources.photo_date_changed_from
import macrotrack.composeapp.generated.resources.photo_date_taken
import macrotrack.composeapp.generated.resources.photo_date_taken_now
import macrotrack.composeapp.generated.resources.photo_day_description
import macrotrack.composeapp.generated.resources.photo_dont_use_it
import macrotrack.composeapp.generated.resources.photo_log_weight_for
import macrotrack.composeapp.generated.resources.photo_next_month
import macrotrack.composeapp.generated.resources.photo_no_weight_on
import macrotrack.composeapp.generated.resources.photo_note
import macrotrack.composeapp.generated.resources.photo_note_hint
import macrotrack.composeapp.generated.resources.photo_pose
import macrotrack.composeapp.generated.resources.photo_previous_month
import macrotrack.composeapp.generated.resources.photo_queue_counter
import macrotrack.composeapp.generated.resources.photo_retake
import macrotrack.composeapp.generated.resources.photo_save_many
import macrotrack.composeapp.generated.resources.photo_save_many_to
import macrotrack.composeapp.generated.resources.photo_save_one
import macrotrack.composeapp.generated.resources.photo_save_to
import macrotrack.composeapp.generated.resources.photo_today
import macrotrack.composeapp.generated.resources.photo_use_nearest
import macrotrack.composeapp.generated.resources.photo_weight_from_nearest
import macrotrack.composeapp.generated.resources.photo_weight_logged_that_day
import macrotrack.composeapp.generated.resources.photo_weight_on_photo
import macrotrack.composeapp.generated.resources.photo_yesterday
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.features.photos.PhotoDraft
import org.thelazybattley.macrotrack.features.photos.PhotoSaveEvent
import org.thelazybattley.macrotrack.features.photos.PhotoSaveViewModel
import org.thelazybattley.macrotrack.features.photos.PhotoSaveViewState
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun PhotoSaveScreen(
    onBack: () -> Unit,
    onSaved: (albumId: Long) -> Unit,
    onLogWeight: (LocalDate) -> Unit
) {
    val viewModel = koinViewModel<PhotoSaveViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    var isAlbumSheetOpen by rememberSaveable { mutableStateOf(value = false) }
    var isCropOpen by rememberSaveable { mutableStateOf(value = false) }
    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PhotoSaveEvent.Saved -> onSaved(event.albumId)
                PhotoSaveEvent.Empty -> onBack()
            }
        }
    }
    val draft = viewState.draft
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            Box(
                modifier = Modifier
                    .height(height = 40.dp)
                    .width(width = 84.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = if (viewState.isFromLibrary) Res.string.back else Res.string.photo_retake),
                    style = typography.bold13,
                    color = colors.ink
                )
            }
            Text(
                text = if (viewState.drafts.size > 1) {
                    stringResource(resource = Res.string.photo_save_many, viewState.drafts.size)
                } else {
                    stringResource(resource = Res.string.photo_save_one)
                },
                style = typography.bold18,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(weight = 1f)
            )
            Box(modifier = Modifier.width(width = 84.dp))
        }
        if (draft == null) return@Column
        if (viewState.drafts.size > 1) Queue(viewState = viewState, onSelect = viewModel::onSelect)
        Row(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(space = 14.dp)) {
            PhotoTile(
                modifier = Modifier.size(width = 150.dp, height = 200.dp),
                fileName = draft.fileName,
                date = draft.date,
                weight = viewState.weight,
                isOverlayShown = true,
                cornerRadius = 18.dp,
                isThumbnail = false,
                textStyle = typography.bold12
            )
            Column(modifier = Modifier.weight(weight = 1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                DateCard(draft = draft, isFromLibrary = viewState.isFromLibrary, isOpen = viewState.isDatePickerOpen, onClick = viewModel::onDatePickerToggled)
                WeightCard(
                    modifier = Modifier.weight(weight = 1f),
                    viewState = viewState,
                    draft = draft,
                    onUseNearest = viewModel::onUseNearest,
                    onUndoNearest = viewModel::onUndoNearest,
                    onLogWeight = { onLogWeight(draft.date) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                .clickable { isCropOpen = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            PhotoIcon(resource = Res.drawable.ic_crop, size = 18, tint = colors.ink)
            Text(text = stringResource(resource = Res.string.photo_crop), style = typography.bold14, color = colors.ink)
        }
        if (viewState.isDatePickerOpen) {
            DatePicker(
                viewState = viewState,
                draft = draft,
                onMonthChanged = viewModel::onMonthChanged,
                onDateSelected = viewModel::onDateSelected,
                onDone = viewModel::onDatePickerToggled
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            SheetLabel(
                text = if (viewState.drafts.size > 1) {
                    stringResource(resource = Res.string.photo_album_for_all, viewState.drafts.size)
                } else {
                    stringResource(resource = Res.string.photo_album)
                }
            )
            if (viewState.albums.isNotEmpty()) {
                val shape = RoundedCornerShape(size = 18.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = shape)
                        .background(color = colors.surface)
                        .border(width = 1.dp, color = colors.line, shape = shape)
                ) {
                    viewState.albums.forEachIndexed { index, summary ->
                        val isSelected = summary.album.id == viewState.albumId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp)
                                .background(color = if (isSelected) colors.accentSoft else Color.Transparent)
                                .clickable(role = Role.RadioButton) { viewModel.onAlbumSelected(id = summary.album.id) }
                                .semantics { selected = isSelected }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                        ) {
                            val cover = summary.latest
                            if (cover != null) {
                                PhotoImage(
                                    modifier = Modifier.size(width = 34.dp, height = 44.dp).clip(shape = RoundedCornerShape(size = 8.dp)),
                                    fileName = cover.photo.fileName,
                                    isThumbnail = true
                                )
                            } else {
                                EmptyPhotoTile(modifier = Modifier.size(width = 34.dp, height = 44.dp).clip(shape = RoundedCornerShape(size = 8.dp)))
                            }
                            Column(modifier = Modifier.weight(weight = 1f)) {
                                Text(text = summary.album.name, style = typography.bold14, color = colors.ink)
                                Text(
                                    text = pluralStringResource(resource = Res.plurals.photo_count, quantity = summary.photos.size, summary.photos.size),
                                    style = typography.regular11,
                                    color = colors.muted
                                )
                            }
                            RadioDot(isSelected = isSelected)
                        }
                        if (index < viewState.albums.lastIndex) HorizontalDivider(color = colors.divider)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .dashedBorder(color = colors.dashed, width = 1.5.dp, cornerRadius = 14.dp)
                    .clickable { isAlbumSheetOpen = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally)
            ) {
                PhotoIcon(resource = Res.drawable.ic_plus, size = 16, tint = colors.accentInk)
                Text(text = stringResource(resource = Res.string.photo_album_new), style = typography.bold14, color = colors.accentInk)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            SheetLabel(text = stringResource(resource = Res.string.photo_pose))
            PhotoSegmented(
                options = PhotoPose.entries,
                selected = draft.pose,
                label = { stringResource(resource = it.label) },
                onSelect = viewModel::onPoseSelected
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            SheetLabel(text = stringResource(resource = Res.string.photo_note))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 48.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (draft.note.isEmpty()) {
                    Text(text = stringResource(resource = Res.string.photo_note_hint), style = typography.regular13, color = colors.muted)
                }
                BasicTextField(
                    value = draft.note,
                    onValueChange = viewModel::onNoteChanged,
                    singleLine = true,
                    textStyle = typography.regular13.merge(other = TextStyle(color = colors.ink)),
                    cursorBrush = SolidColor(value = colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        val albumName = viewState.album?.album?.name
        SheetButton(
            text = when {
                albumName == null -> stringResource(resource = Res.string.photo_album_new)
                viewState.drafts.size > 1 -> stringResource(resource = Res.string.photo_save_many_to, viewState.drafts.size, albumName)
                else -> stringResource(resource = Res.string.photo_save_to, albumName)
            },
            isPrimary = true,
            isEnabled = !viewState.isSaving,
            onClick = { if (albumName == null) isAlbumSheetOpen = true else viewModel.onSave() }
        )
    }
    val cropping = viewState.draft
    if (isCropOpen && cropping != null) {
        PhotoCropDialog(
            fileName = cropping.fileName,
            ghostFileName = viewState.album?.latest?.photo?.fileName,
            onCancel = { isCropOpen = false },
            onDone = { crop ->
                isCropOpen = false
                viewModel.onCropped(crop = crop)
            }
        )
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
}

@Composable
private fun Queue(viewState: PhotoSaveViewState, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            viewState.drafts.forEachIndexed { index, draft ->
                val isSelected = index == viewState.selected
                val weight = viewState.weightOf(draft = draft)
                val description = stringResource(
                    resource = Res.string.photo_day_description,
                    draft.date.toLongPhotoDate(),
                    photoWeightText(weight = weight)
                )
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 80.dp)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) colors.accent else colors.line,
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                        .clickable { onSelect(index) }
                        .semantics {
                            contentDescription = description
                            selected = isSelected
                        }
                ) {
                    PhotoImage(modifier = Modifier.fillMaxSize(), fileName = draft.fileName, isThumbnail = true)
                    Text(
                        text = draft.date.toAbbreviatedMonthDay(),
                        style = typography.bold10,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(alignment = Alignment.BottomCenter).fillMaxWidth().background(color = PhotoScrim).padding(vertical = 2.dp)
                    )
                    if (weight == null) {
                        Box(
                            modifier = Modifier
                                .align(alignment = Alignment.TopEnd)
                                .padding(all = 4.dp)
                                .size(size = 16.dp)
                                .clip(shape = CircleShape)
                                .background(color = colors.warmInk),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "!", style = typography.bold11, color = colors.surface)
                        }
                    }
                }
            }
        }
        Text(
            text = stringResource(resource = Res.string.photo_queue_counter, viewState.selected + 1, viewState.drafts.size),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun DateCard(draft: PhotoDraft, isFromLibrary: Boolean, isOpen: Boolean, onClick: () -> Unit) {
    val today = getCurrentDate()
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = if (isOpen) 2.dp else 1.dp, color = if (isOpen) colors.accent else colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 1.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(resource = Res.string.photo_date), style = typography.regular11, color = colors.muted, modifier = Modifier.weight(weight = 1f))
            Text(text = stringResource(resource = Res.string.photo_date_change), style = typography.bold12, color = colors.accentInk)
        }
        Text(
            text = when (draft.date) {
                today -> stringResource(resource = Res.string.photo_today)
                today.minus(period = DatePeriod(days = 1)) -> stringResource(resource = Res.string.photo_yesterday)
                else -> draft.date.dayOfWeek.displayName()
            },
            style = typography.bold14,
            color = colors.ink
        )
        Text(text = draft.date.toLongPhotoDate(), style = typography.regular11, color = colors.muted)
        val takenOn = draft.takenOn
        Text(
            text = when {
                !isFromLibrary -> stringResource(resource = Res.string.photo_date_taken_now)
                takenOn != null && takenOn != draft.date -> stringResource(resource = Res.string.photo_date_changed_from, takenOn.toShortDayMonthDay())
                else -> stringResource(resource = Res.string.photo_date_taken)
            },
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun WeightCard(
    modifier: Modifier = Modifier,
    viewState: PhotoSaveViewState,
    draft: PhotoDraft,
    onUseNearest: () -> Unit,
    onUndoNearest: () -> Unit,
    onLogWeight: () -> Unit
) {
    val weight = viewState.weight
    if (weight != null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = colors.hero)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Text(text = stringResource(resource = Res.string.photo_weight_on_photo), style = typography.regular11, color = colors.muted)
            Text(text = photoWeightText(weight = weight), style = typography.bold24, color = colors.ink)
            if (draft.weightDate != null) {
                Text(
                    text = stringResource(resource = Res.string.photo_weight_from_nearest, weight.date.toShortDayMonthDay()),
                    style = typography.regular11,
                    color = colors.muted
                )
                Text(
                    text = stringResource(resource = Res.string.photo_dont_use_it),
                    style = typography.bold12,
                    color = colors.accentInk,
                    modifier = Modifier.clickable(onClick = onUndoNearest).padding(vertical = 6.dp)
                )
            } else {
                Text(text = stringResource(resource = Res.string.photo_weight_logged_that_day), style = typography.regular11, color = colors.muted)
            }
        }
        return
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.warmSoft)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.photo_no_weight_on, draft.date.toAbbreviatedMonthDay()),
            style = typography.bold13,
            color = colors.warmInk
        )
        viewState.nearest?.let { nearest ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = colors.surface)
                    .clickable(onClick = onUseNearest)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = Res.string.photo_use_nearest, nearest.date.toShortDayMonthDay(), photoWeightText(weight = nearest)),
                    style = typography.bold12,
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            text = stringResource(resource = Res.string.photo_log_weight_for, draft.date.toAbbreviatedMonthDay()),
            style = typography.bold12,
            color = colors.warmInk,
            modifier = Modifier.clickable(onClick = onLogWeight).padding(vertical = 4.dp)
        )
    }
}

/** A Monday-first month: dots on days with a weigh-in, the day taken outlined, nothing after today. */
@Composable
private fun DatePicker(
    viewState: PhotoSaveViewState,
    draft: PhotoDraft,
    onMonthChanged: (isNext: Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDone: () -> Unit
) {
    val today = getCurrentDate()
    val month = viewState.calendarMonth
    val shape = RoundedCornerShape(size = 20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            MonthButton(
                isEnabled = viewState.canGoToPreviousMonth,
                description = stringResource(resource = Res.string.photo_previous_month),
                isNext = false,
                onClick = { onMonthChanged(false) }
            )
            Text(
                text = month.toMonthYear(),
                style = typography.bold14,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(weight = 1f)
            )
            MonthButton(
                isEnabled = viewState.canGoToNextMonth,
                description = stringResource(resource = Res.string.photo_next_month),
                isNext = true,
                onClick = { onMonthChanged(true) }
            )
        }
        Row {
            DayOfWeek.entries.forEach { day ->
                Text(
                    text = day.displayName().take(n = 1),
                    style = typography.bold11,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
        val lead = month.dayOfWeek.ordinal
        val days = (0 until 31).map { month.plus(period = DatePeriod(days = it)) }.filter { it.month == month.month }
        val cells: List<LocalDate?> = List(size = lead) { null } + days
        cells.chunked(size = 7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                week.forEach { date ->
                    if (date == null) {
                        Box(modifier = Modifier.weight(weight = 1f).height(height = 42.dp))
                    } else {
                        DayCell(
                            modifier = Modifier.weight(weight = 1f),
                            date = date,
                            isSelected = date == draft.date,
                            isTaken = date == draft.takenOn,
                            hasWeight = date in viewState.weightsByDate,
                            isFuture = date > today,
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
                repeat(times = 7 - week.size) { Box(modifier = Modifier.weight(weight = 1f)) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                Box(modifier = Modifier.size(size = 5.dp).clip(shape = CircleShape).background(color = colors.accent))
                Text(text = stringResource(resource = Res.string.photo_calendar_weight), style = typography.regular11, color = colors.muted)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                Box(modifier = Modifier.size(size = 10.dp).border(width = 1.5.dp, color = colors.ink, shape = RoundedCornerShape(size = 3.dp)))
                Text(text = stringResource(resource = Res.string.photo_calendar_taken), style = typography.regular11, color = colors.muted)
            }
            Box(modifier = Modifier.weight(weight = 1f))
            Box(
                modifier = Modifier
                    .height(height = 36.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = colors.accent)
                    .clickable(onClick = onDone)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = Res.string.done), style = typography.bold13, color = colors.onAccent)
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isSelected: Boolean,
    isTaken: Boolean,
    hasWeight: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 10.dp)
    val description = stringResource(resource = Res.string.photo_day_description, date.toLongPhotoDate(), "")
    Column(
        modifier = modifier
            .height(height = 42.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accent else Color.Transparent)
            .then(if (isTaken && !isSelected) Modifier.border(width = 1.5.dp, color = colors.ink, shape = shape) else Modifier)
            .clickable(enabled = !isFuture, onClick = onClick)
            .semantics {
                contentDescription = description
                selected = isSelected
            }
            .alpha(alpha = if (isFuture) 0.5f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 2.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            text = date.day.toString(),
            style = if (isSelected) typography.bold14 else typography.bold13,
            color = when {
                isSelected -> colors.onAccent
                isFuture -> colors.faint
                else -> colors.ink
            }
        )
        Box(
            modifier = Modifier
                .size(size = 5.dp)
                .clip(shape = CircleShape)
                .background(
                    color = when {
                        !hasWeight -> Color.Transparent
                        isSelected -> colors.onAccent
                        else -> colors.accent
                    }
                )
        )
    }
}

@Composable
private fun MonthButton(isEnabled: Boolean, description: String, isNext: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = Modifier
            .size(size = 40.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(enabled = isEnabled, onClick = onClick)
            .semantics { contentDescription = description }
            .alpha(alpha = if (isEnabled) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        PhotoIcon(
            resource = if (isNext) Res.drawable.ic_chevron_right else Res.drawable.ic_chevron_left,
            size = 16,
            tint = if (isEnabled) colors.ink else colors.faint
        )
    }
}

@Composable
private fun RadioDot(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(size = 22.dp)
            .border(width = 2.dp, color = if (isSelected) colors.accent else colors.dashed, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) Box(modifier = Modifier.size(size = 10.dp).clip(shape = CircleShape).background(color = colors.accent))
    }
}
