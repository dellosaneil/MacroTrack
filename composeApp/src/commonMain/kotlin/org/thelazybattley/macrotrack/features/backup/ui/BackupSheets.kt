package org.thelazybattley.macrotrack.features.backup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.export_adding
import macrotrack.composeapp.generated.resources.export_page
import macrotrack.composeapp.generated.resources.export_preparing
import macrotrack.composeapp.generated.resources.export_ready
import macrotrack.composeapp.generated.resources.export_rows
import macrotrack.composeapp.generated.resources.export_save
import macrotrack.composeapp.generated.resources.export_saved
import macrotrack.composeapp.generated.resources.export_sections
import macrotrack.composeapp.generated.resources.export_share
import macrotrack.composeapp.generated.resources.export_summary
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_download
import macrotrack.composeapp.generated.resources.ic_file_check
import macrotrack.composeapp.generated.resources.ic_share
import macrotrack.composeapp.generated.resources.ic_warning
import macrotrack.composeapp.generated.resources.ok
import macrotrack.composeapp.generated.resources.restore_backing_up
import macrotrack.composeapp.generated.resources.restore_backup_first
import macrotrack.composeapp.generated.resources.restore_backup_first_detail
import macrotrack.composeapp.generated.resources.restore_confirm_body
import macrotrack.composeapp.generated.resources.restore_confirm_title
import macrotrack.composeapp.generated.resources.restore_done_body
import macrotrack.composeapp.generated.resources.restore_done_body_saved
import macrotrack.composeapp.generated.resources.restore_done_title
import macrotrack.composeapp.generated.resources.restore_error_not_backup
import macrotrack.composeapp.generated.resources.restore_error_title
import macrotrack.composeapp.generated.resources.restore_error_too_new
import macrotrack.composeapp.generated.resources.restore_go_today
import macrotrack.composeapp.generated.resources.restore_keep_open
import macrotrack.composeapp.generated.resources.restore_replace
import macrotrack.composeapp.generated.resources.restore_restoring
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.platform.SharedFile
import org.thelazybattley.macrotrack.domain.model.BackupProblem
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.features.backup.BackupCallbacks
import org.thelazybattley.macrotrack.features.backup.ExportSheet
import org.thelazybattley.macrotrack.features.backup.RestoreDialog
import org.thelazybattley.macrotrack.features.backup.export.ExportEntryKind
import org.thelazybattley.macrotrack.features.backup.export.ExportRange
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Making an export, then the finished file with ways to save or share it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExportSheetHost(
    sheet: ExportSheet?,
    range: ExportRange,
    onSave: () -> Unit,
    onShare: (SharedFile) -> Unit,
    onClose: () -> Unit
) {
    sheet ?: return
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            when (sheet) {
                is ExportSheet.Preparing -> Column(
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                    ) {
                        Spinner(size = 22)
                        Text(text = stringResource(resource = Res.string.export_preparing), style = typography.bold18, color = colors.ink)
                    }
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(height = 8.dp).clip(shape = RoundedCornerShape(size = 4.dp)),
                        color = colors.accent,
                        trackColor = colors.heroTrack
                    )
                    sheet.item?.let { item ->
                        Text(
                            text = stringResource(resource = Res.string.export_adding, stringResource(resource = item.title()).lowercase()),
                            style = typography.regular13,
                            color = colors.muted
                        )
                    }
                    TextButton(text = stringResource(resource = Res.string.cancel), color = colors.accentInk, onClick = onClose)
                }
                is ExportSheet.Ready -> {
                    val file = sheet.file
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                    ) {
                        IconTile(icon = Res.drawable.ic_file_check, tint = colors.accentInk, background = colors.accentSoft, size = 48)
                        Column(modifier = Modifier.weight(weight = 1f)) {
                            Text(text = stringResource(resource = Res.string.export_ready), style = typography.bold18, color = colors.ink)
                            Text(
                                text = file.fileName,
                                style = typography.regular13,
                                color = colors.muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = RoundedCornerShape(size = 16.dp))
                            .background(color = colors.background)
                            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
                    ) {
                        file.entries.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                            ) {
                                Text(
                                    text = entry.name,
                                    style = typography.regular11.copy(fontFamily = FontFamily.Monospace),
                                    color = colors.ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(weight = 1f)
                                )
                                Text(
                                    text = when (entry.kind) {
                                        ExportEntryKind.ROWS -> pluralStringResource(
                                            resource = Res.plurals.export_rows,
                                            quantity = entry.value,
                                            entry.value.withThousands()
                                        )
                                        ExportEntryKind.PAGE -> stringResource(resource = Res.string.export_page, entry.value)
                                        ExportEntryKind.SECTIONS -> pluralStringResource(
                                            resource = Res.plurals.export_sections,
                                            quantity = entry.value,
                                            entry.value
                                        )
                                    },
                                    style = typography.regular11,
                                    color = colors.muted
                                )
                            }
                            if (index < file.entries.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                        }
                    }
                    val imperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
                    Text(
                        text = stringResource(
                            resource = Res.string.export_summary,
                            formatFileSize(bytes = file.bytes.size),
                            stringResource(resource = range.title()),
                            weightUnitLabel(),
                            if (imperial) UnitSystem.OUNCE else "g"
                        ),
                        style = typography.regular11,
                        color = colors.muted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                        SheetButton(
                            modifier = Modifier.weight(weight = 1f),
                            text = stringResource(resource = if (sheet.isSaved) Res.string.export_saved else Res.string.export_save),
                            icon = if (sheet.isSaved) Res.drawable.ic_check else Res.drawable.ic_download,
                            isPrimary = false,
                            onClick = onSave
                        )
                        SheetButton(
                            modifier = Modifier.weight(weight = 1f),
                            text = stringResource(resource = Res.string.export_share),
                            icon = Res.drawable.ic_share,
                            isPrimary = true,
                            onClick = { onShare(SharedFile(name = file.fileName, mimeType = file.mimeType, bytes = file.bytes)) }
                        )
                    }
                    TextButton(
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        text = stringResource(resource = Res.string.done),
                        color = colors.muted,
                        onClick = onClose
                    )
                }
            }
        }
    }
}

/** Confirming a restore, then its progress, then the result. */
@Composable
internal fun RestoreDialogHost(dialog: RestoreDialog?, callbacks: BackupCallbacks) {
    dialog ?: return
    val isWorking = dialog is RestoreDialog.Working
    Dialog(
        onDismissRequest = { if (!isWorking) callbacks.onCloseRestore() },
        properties = DialogProperties(dismissOnBackPress = !isWorking, dismissOnClickOutside = !isWorking)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(shape = RoundedCornerShape(size = 28.dp))
                .background(color = colors.surface)
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            when (dialog) {
                is RestoreDialog.Confirm -> {
                    IconTile(icon = Res.drawable.ic_warning, tint = colors.fat, background = colors.fat.copy(alpha = 0.14f), size = 44)
                    Text(text = stringResource(resource = Res.string.restore_confirm_title), style = typography.bold18, color = colors.ink)
                    Text(
                        text = stringResource(
                            resource = Res.string.restore_confirm_body,
                            formatBackupTime(millis = dialog.info.createdAtMillis),
                            dialog.info.device
                        ),
                        style = typography.regular13,
                        color = colors.muted
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = RoundedCornerShape(size = 14.dp))
                            .background(color = colors.background)
                            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                            .toggleable(value = dialog.backUpFirst, role = Role.Checkbox, onValueChange = { callbacks.onBackUpFirstToggled() })
                            .padding(all = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                    ) {
                        CheckBox(isChecked = dialog.backUpFirst)
                        Column(modifier = Modifier.weight(weight = 1f)) {
                            Text(text = stringResource(resource = Res.string.restore_backup_first), style = typography.bold13, color = colors.ink)
                            Text(text = stringResource(resource = Res.string.restore_backup_first_detail), style = typography.regular11, color = colors.muted)
                        }
                    }
                    DialogButtons(
                        dismiss = stringResource(resource = Res.string.cancel),
                        confirm = stringResource(resource = Res.string.restore_replace),
                        confirmColor = colors.fat,
                        onDismiss = callbacks::onCloseRestore,
                        onConfirm = callbacks::onConfirmRestore
                    )
                }
                is RestoreDialog.Working -> Column(
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                    ) {
                        Spinner(size = 22)
                        Text(
                            text = if (dialog.isBackingUp) {
                                stringResource(resource = Res.string.restore_backing_up)
                            } else {
                                stringResource(resource = Res.string.restore_restoring, formatBackupTime(millis = dialog.info.createdAtMillis))
                            },
                            style = typography.bold16,
                            color = colors.ink
                        )
                    }
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(height = 8.dp).clip(shape = RoundedCornerShape(size = 4.dp)),
                        color = colors.accent,
                        trackColor = colors.heroTrack
                    )
                    Text(text = stringResource(resource = Res.string.restore_keep_open), style = typography.regular13, color = colors.muted)
                }
                is RestoreDialog.Done -> {
                    IconTile(icon = Res.drawable.ic_check, tint = colors.accentInk, background = colors.accentSoft, size = 44)
                    Text(text = stringResource(resource = Res.string.restore_done_title), style = typography.bold18, color = colors.ink)
                    Text(
                        text = stringResource(
                            resource = if (dialog.savedFirst) Res.string.restore_done_body_saved else Res.string.restore_done_body,
                            formatBackupTime(millis = dialog.info.createdAtMillis)
                        ),
                        style = typography.regular13,
                        color = colors.muted
                    )
                    DialogButtons(
                        dismiss = stringResource(resource = Res.string.close),
                        confirm = stringResource(resource = Res.string.restore_go_today),
                        confirmColor = colors.accent,
                        onDismiss = callbacks::onCloseRestore,
                        onConfirm = callbacks::onGoToToday
                    )
                }
                is RestoreDialog.Failed -> {
                    IconTile(icon = Res.drawable.ic_warning, tint = colors.fat, background = colors.fat.copy(alpha = 0.14f), size = 44)
                    Text(text = stringResource(resource = Res.string.restore_error_title), style = typography.bold18, color = colors.ink)
                    Text(
                        text = stringResource(
                            resource = when (dialog.problem) {
                                BackupProblem.NOT_A_BACKUP -> Res.string.restore_error_not_backup
                                BackupProblem.TOO_NEW -> Res.string.restore_error_too_new
                            }
                        ),
                        style = typography.regular13,
                        color = colors.muted
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(text = stringResource(resource = Res.string.ok), color = colors.accentInk, onClick = callbacks::onCloseRestore)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogButtons(
    dismiss: String,
    confirm: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.End)
    ) {
        TextButton(text = dismiss, color = colors.muted, onClick = onDismiss)
        Box(
            modifier = Modifier
                .height(height = 40.dp)
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = confirmColor)
                .clickable(role = Role.Button, onClick = onConfirm)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = confirm, style = typography.bold14, color = colors.surface)
        }
    }
}

@Composable
private fun TextButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = color)
    }
}

@Composable
private fun SheetButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: DrawableResource,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    val contentColor = if (isPrimary) colors.onAccent else colors.ink
    Row(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = if (isPrimary) colors.accent else colors.surface)
            .then(if (isPrimary) Modifier else Modifier.border(width = 1.dp, color = colors.line, shape = shape))
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = contentColor, modifier = Modifier.size(size = 18.dp))
        Text(text = text, style = typography.bold14, color = contentColor)
    }
}

@Composable
private fun Spinner(size: Int) {
    CircularProgressIndicator(
        modifier = Modifier.size(size = size.dp),
        color = colors.accent,
        trackColor = colors.heroTrack,
        strokeWidth = 2.5.dp
    )
}
