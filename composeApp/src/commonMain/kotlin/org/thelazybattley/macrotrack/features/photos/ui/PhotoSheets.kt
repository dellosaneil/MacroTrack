package org.thelazybattley.macrotrack.features.photos.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.ic_camera
import macrotrack.composeapp.generated.resources.ic_clock
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.photo_date_change
import macrotrack.composeapp.generated.resources.photo_reminder_time
import macrotrack.composeapp.generated.resources.photo_time_am
import macrotrack.composeapp.generated.resources.photo_time_pm
import macrotrack.composeapp.generated.resources.ic_image
import macrotrack.composeapp.generated.resources.photo_add_camera
import macrotrack.composeapp.generated.resources.photo_add_camera_body
import macrotrack.composeapp.generated.resources.photo_add_library
import macrotrack.composeapp.generated.resources.photo_add_library_body
import macrotrack.composeapp.generated.resources.photo_add_title
import macrotrack.composeapp.generated.resources.photo_add_to
import macrotrack.composeapp.generated.resources.photo_album_create
import macrotrack.composeapp.generated.resources.photo_album_edit
import macrotrack.composeapp.generated.resources.photo_album_ideas
import macrotrack.composeapp.generated.resources.photo_album_name
import macrotrack.composeapp.generated.resources.photo_album_name_hint
import macrotrack.composeapp.generated.resources.photo_album_new
import macrotrack.composeapp.generated.resources.photo_album_remind
import macrotrack.composeapp.generated.resources.photo_album_remind_none
import macrotrack.composeapp.generated.resources.photo_album_remind_on
import macrotrack.composeapp.generated.resources.photo_idea_front_side_back
import macrotrack.composeapp.generated.resources.photo_idea_leg
import macrotrack.composeapp.generated.resources.photo_idea_pull
import macrotrack.composeapp.generated.resources.photo_idea_push
import macrotrack.composeapp.generated.resources.photo_importing
import macrotrack.composeapp.generated.resources.photo_show_date_weight
import macrotrack.composeapp.generated.resources.photo_show_date_weight_body
import macrotrack.composeapp.generated.resources.save
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.thelazybattley.macrotrack.core.platform.rememberNotificationPermissionState
import org.thelazybattley.macrotrack.domain.model.DEFAULT_PHOTO_REMINDER_MINUTES
import org.thelazybattley.macrotrack.domain.model.PHOTO_ALBUM_NAME_MAX
import org.thelazybattley.macrotrack.domain.model.PHOTO_PICK_MAX
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.features.activity.ui.ActivitySwitch
import org.thelazybattley.macrotrack.features.photos.PhotoDrafts
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Creating an album, or changing one when [album] is given: its name, reminder days and what shows on its photos. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AlbumSheet(
    album: PhotoAlbum?,
    onDismiss: () -> Unit,
    onSave: (PhotoAlbum) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(value = album?.name.orEmpty()) }
    var days by remember { mutableStateOf(value = album?.reminderDays.orEmpty()) }
    var reminderMinutes by rememberSaveable { mutableStateOf(value = album?.reminderMinutes ?: DEFAULT_PHOTO_REMINDER_MINUTES) }
    var isTimePickerOpen by rememberSaveable { mutableStateOf(value = false) }
    var isOverlayShown by rememberSaveable { mutableStateOf(value = album?.isDateAndWeightShown ?: true) }
    val notificationPermission = rememberNotificationPermissionState()
    val ideas = listOf(
        stringResource(resource = Res.string.photo_idea_push),
        stringResource(resource = Res.string.photo_idea_pull),
        stringResource(resource = Res.string.photo_idea_leg),
        stringResource(resource = Res.string.photo_idea_front_side_back)
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Text(
                text = stringResource(resource = if (album == null) Res.string.photo_album_new else Res.string.photo_album_edit),
                style = typography.bold24,
                color = colors.ink
            )
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                SheetLabel(text = stringResource(resource = Res.string.photo_album_name))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 52.dp)
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .background(color = colors.background)
                        .border(width = 1.5.dp, color = colors.accent, shape = RoundedCornerShape(size = 14.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Box(modifier = Modifier.weight(weight = 1f)) {
                        if (name.isEmpty()) {
                            Text(text = stringResource(resource = Res.string.photo_album_name_hint), style = typography.bold16, color = colors.muted)
                        }
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it.take(n = PHOTO_ALBUM_NAME_MAX) },
                            singleLine = true,
                            textStyle = typography.bold16.merge(other = TextStyle(color = colors.ink)),
                            cursorBrush = SolidColor(value = colors.accent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(text = "${name.length}/$PHOTO_ALBUM_NAME_MAX", style = typography.regular11, color = colors.muted)
                }
            }
            if (album == null) {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    SheetLabel(text = stringResource(resource = Res.string.photo_album_ideas))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                    ) {
                        ideas.forEach { idea ->
                            Chip(text = idea, isSelected = name.trim() == idea, onClick = { name = idea })
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                SheetLabel(text = stringResource(resource = Res.string.photo_album_remind))
                Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                    DayOfWeek.entries.forEach { day ->
                        val isOn = day in days
                        Box(
                            modifier = Modifier
                                .weight(weight = 1f)
                                .height(height = 40.dp)
                                .clip(shape = RoundedCornerShape(size = 12.dp))
                                .background(color = if (isOn) colors.accent else colors.surface)
                                .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = RoundedCornerShape(size = 12.dp))
                                .clickable(role = Role.Checkbox) { days = if (isOn) days - day else days + day }
                                .semantics { contentDescription = day.displayName() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = day.displayName().take(n = 1), style = typography.bold13, color = if (isOn) colors.onAccent else colors.ink)
                        }
                    }
                }
                if (days.isNotEmpty()) {
                    ReminderTimeRow(minutes = reminderMinutes, onClick = { isTimePickerOpen = true })
                }
                Text(
                    text = if (days.isEmpty()) {
                        stringResource(resource = Res.string.photo_album_remind_none)
                    } else {
                        stringResource(
                            resource = Res.string.photo_album_remind_on,
                            formatReminderTime(minutes = reminderMinutes),
                            days.sortedBy { it.ordinal }.joinToString(separator = ", ") { it.displayName() }
                        )
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.background)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = stringResource(resource = Res.string.photo_show_date_weight), style = typography.bold14, color = colors.ink)
                    Text(text = stringResource(resource = Res.string.photo_show_date_weight_body), style = typography.regular11, color = colors.muted)
                }
                ActivitySwitch(
                    isOn = isOverlayShown,
                    description = stringResource(resource = Res.string.photo_show_date_weight),
                    onToggle = { isOverlayShown = !isOverlayShown }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                SheetButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = Res.string.cancel),
                    isPrimary = false,
                    onClick = onDismiss
                )
                SheetButton(
                    modifier = Modifier.weight(weight = 2f),
                    text = stringResource(resource = if (album == null) Res.string.photo_album_create else Res.string.save),
                    isPrimary = true,
                    isEnabled = name.isNotBlank(),
                    onClick = {
                        if (days.isNotEmpty() && !notificationPermission.isGranted) notificationPermission.request()
                        onSave(
                            (album ?: PhotoAlbum(name = name)).copy(
                                name = name.trim(),
                                reminderDays = days,
                                reminderMinutes = reminderMinutes,
                                isDateAndWeightShown = isOverlayShown
                            )
                        )
                    }
                )
            }
        }
    }
    if (isTimePickerOpen) {
        ReminderTimeDialog(
            minutes = reminderMinutes,
            onDismiss = { isTimePickerOpen = false },
            onPicked = {
                reminderMinutes = it
                isTimePickerOpen = false
            }
        )
    }
}

/** "Time · 6:00 PM", tapping it opens the clock. */
@Composable
private fun ReminderTimeRow(minutes: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        PhotoIcon(resource = Res.drawable.ic_clock, size = 18, tint = colors.accentInk)
        Text(
            text = stringResource(resource = Res.string.photo_reminder_time),
            style = typography.bold14,
            color = colors.ink,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(text = formatReminderTime(minutes = minutes), style = typography.bold14, color = colors.accentInk)
        Text(text = stringResource(resource = Res.string.photo_date_change), style = typography.bold12, color = colors.muted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(minutes: Int, onDismiss: () -> Unit, onPicked: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = minutes / 60, initialMinute = minutes % 60, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(text = stringResource(resource = Res.string.photo_reminder_time), style = typography.bold18, color = colors.ink) },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = colors.background,
                    selectorColor = colors.accent,
                    containerColor = colors.surface,
                    periodSelectorSelectedContainerColor = colors.accentSoft,
                    periodSelectorSelectedContentColor = colors.accentInk,
                    timeSelectorSelectedContainerColor = colors.accentSoft,
                    timeSelectorSelectedContentColor = colors.accentInk,
                    timeSelectorUnselectedContainerColor = colors.background,
                    timeSelectorUnselectedContentColor = colors.ink,
                    clockDialSelectedContentColor = colors.onAccent,
                    clockDialUnselectedContentColor = colors.ink
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onPicked(state.hour * 60 + state.minute) }) {
                Text(text = stringResource(resource = Res.string.done), style = typography.bold14, color = colors.accentInk)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(resource = Res.string.cancel), style = typography.bold14, color = colors.ink)
            }
        }
    )
}

/** Minutes after midnight as "6:00 PM". */
@Composable
internal fun formatReminderTime(minutes: Int): String {
    val hour = minutes / 60
    val minute = (minutes % 60).toString().padStart(length = 2, padChar = '0')
    return stringResource(
        resource = if (hour < 12) Res.string.photo_time_am else Res.string.photo_time_pm,
        if (hour % 12 == 0) 12 else hour % 12,
        minute
    )
}

/** Take a photo now, or bring in older ones from the photo library, to [albumName]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPhotoSheet(
    albumName: String,
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onLibrary: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Column {
                Text(text = stringResource(resource = Res.string.photo_add_title), style = typography.bold24, color = colors.ink)
                Text(text = stringResource(resource = Res.string.photo_add_to, albumName), style = typography.regular13, color = colors.muted)
            }
            AddChoice(
                icon = Res.drawable.ic_camera,
                title = stringResource(resource = Res.string.photo_add_camera),
                body = stringResource(resource = Res.string.photo_add_camera_body),
                isPrimary = true,
                onClick = onCamera
            )
            AddChoice(
                icon = Res.drawable.ic_image,
                title = stringResource(resource = Res.string.photo_add_library),
                body = stringResource(resource = Res.string.photo_add_library_body),
                isPrimary = false,
                onClick = onLibrary
            )
            SheetButton(text = stringResource(resource = Res.string.cancel), isPrimary = false, onClick = onDismiss)
        }
    }
}

@Composable
private fun AddChoice(
    icon: DrawableResource,
    title: String,
    body: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 44.dp)
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = if (isPrimary) colors.accent else colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = if (isPrimary) colors.onAccent else colors.accentInk,
                modifier = Modifier.size(size = 22.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = title, style = typography.bold15, color = colors.ink)
            Text(text = body, style = typography.regular11, color = colors.muted)
        }
    }
}

/** Opens the photo picker and keeps what's picked, then hands over to Save. */
internal class LibraryImport(
    private val picker: PhotoPicker,
    private val drafts: PhotoDrafts,
    private val scope: CoroutineScope,
    private val onImported: () -> Unit
) {
    var isImporting by mutableStateOf(value = false)
        private set

    fun start(albumId: Long?, pose: PhotoPose) {
        picker.pick(max = PHOTO_PICK_MAX) { images ->
            if (images.isEmpty()) return@pick
            isImporting = true
            scope.launch {
                val isReady = drafts.fromLibrary(images = images, albumId = albumId, pose = pose)
                isImporting = false
                if (isReady) onImported()
            }
        }
    }
}

@Composable
internal fun rememberLibraryImport(onImported: () -> Unit): LibraryImport {
    val picker = rememberPhotoPicker()
    val drafts = koinInject<PhotoDrafts>()
    val scope = rememberCoroutineScope()
    val latestOnImported by rememberUpdatedState(newValue = onImported)
    return remember(picker) { LibraryImport(picker = picker, drafts = drafts, scope = scope, onImported = { latestOnImported() }) }
}

/** Covers the screen while picked photos are copied in. */
@Composable
internal fun ImportingOverlay(isVisible: Boolean) {
    if (!isVisible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0x7A0A0B0D))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = colors.surface)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(size = 20.dp), color = colors.accent, strokeWidth = 2.5.dp)
            Text(text = stringResource(resource = Res.string.photo_importing), style = typography.bold14, color = colors.ink)
        }
    }
}

@Composable
internal fun SheetLabel(text: String) {
    Text(text = text, style = typography.bold12, color = colors.muted)
}

@Composable
internal fun Chip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    Box(
        modifier = Modifier
            .height(height = 36.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold13, color = if (isSelected) colors.accentInk else colors.ink)
    }
}

@Composable
internal fun SheetButton(
    modifier: Modifier = Modifier,
    text: String,
    isPrimary: Boolean,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    val background = when {
        !isEnabled -> colors.line
        isPrimary -> colors.accent
        else -> colors.surface
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 52.dp)
            .clip(shape = shape)
            .background(color = background)
            .then(if (isPrimary) Modifier else Modifier.border(width = 1.dp, color = colors.line, shape = shape))
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.bold15,
            color = when {
                !isEnabled -> colors.faint
                isPrimary -> colors.onAccent
                else -> colors.ink
            }
        )
    }
}

internal fun DayOfWeek.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }
