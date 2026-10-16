package org.thelazybattley.macrotrack.features.backup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.backup_backed_up
import macrotrack.composeapp.generated.resources.backup_choose_place
import macrotrack.composeapp.generated.resources.backup_export
import macrotrack.composeapp.generated.resources.backup_file_detail
import macrotrack.composeapp.generated.resources.backup_file_title
import macrotrack.composeapp.generated.resources.backup_footnote_pro
import macrotrack.composeapp.generated.resources.backup_footnote_trial
import macrotrack.composeapp.generated.resources.backup_included_with_pro
import macrotrack.composeapp.generated.resources.backup_last_detail
import macrotrack.composeapp.generated.resources.backup_locked_backup_detail
import macrotrack.composeapp.generated.resources.backup_locked_export
import macrotrack.composeapp.generated.resources.backup_locked_export_detail
import macrotrack.composeapp.generated.resources.backup_locked_note
import macrotrack.composeapp.generated.resources.backup_locked_restore_detail
import macrotrack.composeapp.generated.resources.backup_never_detail
import macrotrack.composeapp.generated.resources.backup_not_backed_up
import macrotrack.composeapp.generated.resources.backup_now
import macrotrack.composeapp.generated.resources.backup_packing
import macrotrack.composeapp.generated.resources.backup_paused_body
import macrotrack.composeapp.generated.resources.backup_paused_body_no_date
import macrotrack.composeapp.generated.resources.backup_paused_title
import macrotrack.composeapp.generated.resources.backup_restore
import macrotrack.composeapp.generated.resources.backup_restore_detail
import macrotrack.composeapp.generated.resources.backup_section
import macrotrack.composeapp.generated.resources.backup_trial_banner
import macrotrack.composeapp.generated.resources.backup_unlock
import macrotrack.composeapp.generated.resources.export_clear_all
import macrotrack.composeapp.generated.resources.export_count_changes
import macrotrack.composeapp.generated.resources.export_count_entries
import macrotrack.composeapp.generated.resources.export_count_foods
import macrotrack.composeapp.generated.resources.export_count_recipes
import macrotrack.composeapp.generated.resources.export_button
import macrotrack.composeapp.generated.resources.export_date_range
import macrotrack.composeapp.generated.resources.export_foods_and_recipes
import macrotrack.composeapp.generated.resources.export_format
import macrotrack.composeapp.generated.resources.export_format_csv
import macrotrack.composeapp.generated.resources.export_format_csv_desc
import macrotrack.composeapp.generated.resources.export_format_json
import macrotrack.composeapp.generated.resources.export_format_json_desc
import macrotrack.composeapp.generated.resources.export_format_pdf
import macrotrack.composeapp.generated.resources.export_format_pdf_desc
import macrotrack.composeapp.generated.resources.export_item_food_log
import macrotrack.composeapp.generated.resources.export_item_foods
import macrotrack.composeapp.generated.resources.export_item_goals
import macrotrack.composeapp.generated.resources.export_item_measurements
import macrotrack.composeapp.generated.resources.export_item_weigh_ins
import macrotrack.composeapp.generated.resources.export_pick_one
import macrotrack.composeapp.generated.resources.export_range_30
import macrotrack.composeapp.generated.resources.export_range_90
import macrotrack.composeapp.generated.resources.export_range_all
import macrotrack.composeapp.generated.resources.export_range_year
import macrotrack.composeapp.generated.resources.export_section
import macrotrack.composeapp.generated.resources.export_select_all
import macrotrack.composeapp.generated.resources.export_subtitle
import macrotrack.composeapp.generated.resources.export_title
import macrotrack.composeapp.generated.resources.export_units_note
import macrotrack.composeapp.generated.resources.export_what
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_cloud_alert
import macrotrack.composeapp.generated.resources.ic_cloud_check
import macrotrack.composeapp.generated.resources.ic_cloud_paused
import macrotrack.composeapp.generated.resources.ic_cloud_upload
import macrotrack.composeapp.generated.resources.ic_download
import macrotrack.composeapp.generated.resources.ic_file
import macrotrack.composeapp.generated.resources.ic_info
import macrotrack.composeapp.generated.resources.ic_restore
import macrotrack.composeapp.generated.resources.ic_shield_check
import macrotrack.composeapp.generated.resources.unit_cm
import macrotrack.composeapp.generated.resources.unit_in
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.platform.rememberFileActions
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.LastBackup
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.features.backup.BackupCallbacks
import org.thelazybattley.macrotrack.features.backup.BackupViewModel
import org.thelazybattley.macrotrack.features.backup.BackupViewState
import org.thelazybattley.macrotrack.features.backup.export.ExportFormat
import org.thelazybattley.macrotrack.features.backup.export.ExportItem
import org.thelazybattley.macrotrack.features.backup.export.ExportRange
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun BackupScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onGoToToday: () -> Unit
) {
    val viewModel = koinViewModel<BackupViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val fileActions = rememberFileActions()
    // The view model asks for the save dialog and file picker; the platform shows them and reports back.
    LaunchedEffect(key1 = viewState.saveRequest) {
        val request = viewState.saveRequest ?: return@LaunchedEffect
        viewModel.onSaveStarted()
        fileActions.save(file = request.file) { isSaved ->
            viewModel.onSaveFinished(reason = request.reason, isSaved = isSaved, sizeBytes = request.file.bytes.size)
        }
    }
    LaunchedEffect(key1 = viewState.isPickRequested) {
        if (!viewState.isPickRequested) return@LaunchedEffect
        viewModel.onPickStarted()
        fileActions.pick(onPicked = viewModel::onBackupPicked)
    }
    LaunchedEffect(key1 = viewState.goToToday) {
        if (viewState.goToToday) onGoToToday()
    }
    BackupScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack
    )
    ExportSheetHost(
        sheet = viewState.exportSheet,
        range = viewState.range,
        onSave = viewModel::onSaveExport,
        onShare = { file -> fileActions.share(file = file) },
        onClose = viewModel::onCloseExport
    )
    RestoreDialogHost(
        dialog = viewState.restoreDialog,
        callbacks = viewModel
    )
}

@Composable
private fun BackupScreen(
    modifier: Modifier = Modifier,
    viewState: BackupViewState,
    callbacks: BackupCallbacks,
    onBack: () -> Unit
) {
    val pro = LocalProAccess.current
    val status = pro.state.status
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = { CommonScreenHeader(title = Res.string.backup_export, onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            if (status.isTrialActive) {
                TrialBanner(daysLeft = status.trialDaysLeft, onClick = { pro.openPaywall(ProFeature.BACKUP) })
            }
            if (!pro.isActive) {
                LockedContent(
                    trialEnd = status.trialEnd?.let { "${it.toAbbreviatedMonthDay()}, ${it.year}" },
                    onUnlock = { pro.openPaywall(ProFeature.BACKUP) }
                )
            } else {
                BackupSection(viewState = viewState, callbacks = callbacks)
                ExportSection(viewState = viewState, callbacks = callbacks)
                DashedNote(
                    text = stringResource(
                        resource = if (status.isTrialActive) Res.string.backup_footnote_trial else Res.string.backup_footnote_pro
                    )
                )
            }
        }
    }
}

@Composable
private fun TrialBanner(daysLeft: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.warmSoft)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        ProBadge(modifier = Modifier.background(color = colors.surface, shape = RoundedCornerShape(size = 6.dp)))
        Text(
            text = pluralStringResource(resource = Res.plurals.backup_trial_banner, quantity = daysLeft, daysLeft),
            style = typography.bold13,
            color = colors.warmInk,
            modifier = Modifier.weight(weight = 1f)
        )
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.warmInk,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: StringResource) {
    Text(text = stringResource(resource = text), style = typography.bold12, color = colors.muted)
}

@Composable
private fun BackupSection(viewState: BackupViewState, callbacks: BackupCallbacks) {
    val lastBackup = viewState.lastBackup
    val isNever = lastBackup == null && !viewState.isBackingUp
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = Res.string.backup_section)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 24.dp))
                .background(color = if (isNever) colors.warmSoft else colors.hero)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                IconTile(
                    icon = if (isNever) Res.drawable.ic_cloud_alert else Res.drawable.ic_cloud_check,
                    tint = if (isNever) colors.warmInk else colors.accentInk,
                    background = colors.surface,
                    size = 44
                )
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                    Text(
                        text = stringResource(resource = if (lastBackup == null) Res.string.backup_not_backed_up else Res.string.backup_backed_up),
                        style = typography.bold16,
                        color = colors.ink
                    )
                    Text(
                        text = lastBackup?.let { lastBackupDetail(lastBackup = it) } ?: stringResource(resource = Res.string.backup_never_detail),
                        style = typography.regular13,
                        color = colors.muted
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_file),
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(size = 18.dp)
                )
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = stringResource(resource = Res.string.backup_file_title), style = typography.bold13, color = colors.ink)
                    Text(text = stringResource(resource = Res.string.backup_file_detail), style = typography.regular11, color = colors.muted)
                }
            }
            if (viewState.isBackingUp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .background(color = colors.surface)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                        .padding(all = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size = 16.dp),
                        color = colors.accent,
                        trackColor = colors.heroTrack,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(
                            resource = if (viewState.isChoosingPlace) Res.string.backup_choose_place else Res.string.backup_packing
                        ),
                        style = typography.bold13,
                        color = colors.ink
                    )
                }
            } else {
                PrimaryButton(
                    text = stringResource(resource = Res.string.backup_now),
                    icon = Res.drawable.ic_cloud_upload,
                    onClick = callbacks::onBackUpNow
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
                .clickable(role = Role.Button, onClick = callbacks::onRestoreClick)
                .heightIn(min = 64.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            IconTile(icon = Res.drawable.ic_restore, tint = colors.accentInk, background = colors.accentSoft, size = 36)
            Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                Text(text = stringResource(resource = Res.string.backup_restore), style = typography.medium13, color = colors.ink)
                Text(text = stringResource(resource = Res.string.backup_restore_detail), style = typography.regular11, color = colors.muted)
            }
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_right),
                contentDescription = null,
                tint = colors.faint,
                modifier = Modifier.size(size = 16.dp)
            )
        }
    }
}

@Composable
private fun lastBackupDetail(lastBackup: LastBackup): String = stringResource(
    resource = Res.string.backup_last_detail,
    formatBackupTime(millis = lastBackup.atMillis),
    formatFileSize(bytes = lastBackup.sizeBytes)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportSection(viewState: BackupViewState, callbacks: BackupCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = Res.string.export_section)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Text(text = stringResource(resource = Res.string.export_title), style = typography.bold16, color = colors.ink)
                Text(text = stringResource(resource = Res.string.export_subtitle), style = typography.regular13, color = colors.muted)
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(resource = Res.string.export_what),
                        style = typography.bold13,
                        color = colors.ink,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(
                        text = stringResource(resource = if (viewState.isAllSelected) Res.string.export_clear_all else Res.string.export_select_all),
                        style = typography.bold12,
                        color = colors.accentInk,
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .clickable(role = Role.Button, onClick = callbacks::onToggleAll)
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
                ExportItem.entries.forEachIndexed { index, item ->
                    CheckRow(
                        label = stringResource(resource = item.title()),
                        detail = itemCount(viewState = viewState, item = item),
                        isChecked = item in viewState.selectedItems,
                        showDivider = index < ExportItem.entries.lastIndex,
                        onClick = { callbacks.onItemToggled(item = item) }
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(text = stringResource(resource = Res.string.export_date_range), style = typography.bold13, color = colors.ink)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    ExportRange.entries.forEach { range ->
                        RangeChip(
                            label = stringResource(resource = range.title()),
                            isSelected = range == viewState.range,
                            onClick = { callbacks.onRangeSelected(range = range) }
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(text = stringResource(resource = Res.string.export_format), style = typography.bold13, color = colors.ink)
                ExportFormat.entries.forEach { format ->
                    val (title, description) = format.texts()
                    OptionCard(
                        title = stringResource(resource = title),
                        description = stringResource(resource = description),
                        trailing = format.name,
                        isSelected = format == viewState.format,
                        onClick = { callbacks.onFormatSelected(format = format) }
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_info),
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(size = 14.dp)
                )
                val imperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
                Text(
                    text = stringResource(
                        resource = Res.string.export_units_note,
                        weightUnitLabel(),
                        stringResource(resource = if (imperial) Res.string.unit_in else Res.string.unit_cm),
                        if (imperial) UnitSystem.OUNCE else "g"
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            val count = viewState.selectedItems.size
            PrimaryButton(
                text = if (viewState.canExport) {
                    pluralStringResource(resource = Res.plurals.export_button, quantity = count, count, viewState.format.name)
                } else {
                    stringResource(resource = Res.string.export_pick_one)
                },
                icon = if (viewState.canExport) Res.drawable.ic_download else null,
                isEnabled = viewState.canExport,
                onClick = callbacks::onExport
            )
        }
    }
}

@Composable
private fun itemCount(viewState: BackupViewState, item: ExportItem): String {
    val count = viewState.data.count(item = item)
    return when (item) {
        ExportItem.FOOD_LOG -> pluralStringResource(resource = Res.plurals.export_count_entries, quantity = count, count.withThousands())
        ExportItem.WEIGH_INS, ExportItem.MEASUREMENTS -> count.withThousands()
        ExportItem.FOODS -> stringResource(
            resource = Res.string.export_foods_and_recipes,
            pluralStringResource(resource = Res.plurals.export_count_foods, quantity = viewState.foodCount, viewState.foodCount),
            pluralStringResource(resource = Res.plurals.export_count_recipes, quantity = viewState.recipeCount, viewState.recipeCount)
        )
        ExportItem.GOALS -> pluralStringResource(resource = Res.plurals.export_count_changes, quantity = count, count)
    }
}

internal fun ExportItem.title(): StringResource = when (this) {
    ExportItem.FOOD_LOG -> Res.string.export_item_food_log
    ExportItem.WEIGH_INS -> Res.string.export_item_weigh_ins
    ExportItem.MEASUREMENTS -> Res.string.export_item_measurements
    ExportItem.FOODS -> Res.string.export_item_foods
    ExportItem.GOALS -> Res.string.export_item_goals
}

internal fun ExportRange.title(): StringResource = when (this) {
    ExportRange.LAST_30_DAYS -> Res.string.export_range_30
    ExportRange.LAST_90_DAYS -> Res.string.export_range_90
    ExportRange.THIS_YEAR -> Res.string.export_range_year
    ExportRange.ALL_TIME -> Res.string.export_range_all
}

private fun ExportFormat.texts(): Pair<StringResource, StringResource> = when (this) {
    ExportFormat.CSV -> Res.string.export_format_csv to Res.string.export_format_csv_desc
    ExportFormat.PDF -> Res.string.export_format_pdf to Res.string.export_format_pdf_desc
    ExportFormat.JSON -> Res.string.export_format_json to Res.string.export_format_json_desc
}

@Composable
private fun CheckRow(
    label: String,
    detail: String,
    isChecked: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(value = isChecked, role = Role.Checkbox, onValueChange = { onClick() })
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            CheckBox(isChecked = isChecked)
            Text(text = label, style = typography.medium13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(text = detail, style = typography.regular11, color = colors.muted)
        }
        if (showDivider) HorizontalDivider(thickness = 1.dp, color = colors.divider)
    }
}

@Composable
internal fun CheckBox(isChecked: Boolean) {
    val shape = RoundedCornerShape(size = 7.dp)
    Box(
        modifier = Modifier
            .size(size = 22.dp)
            .clip(shape = shape)
            .background(color = if (isChecked) colors.accent else colors.surface)
            .then(if (isChecked) Modifier else Modifier.border(width = 2.dp, color = colors.dashed, shape = shape)),
        contentAlignment = Alignment.Center
    ) {
        if (isChecked) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_check),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(size = 14.dp)
            )
        }
    }
}

@Composable
private fun RangeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    Box(
        modifier = Modifier
            .height(height = 36.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = typography.bold13, color = if (isSelected) colors.accentInk else colors.ink)
    }
}

@Composable
internal fun OptionCard(
    title: String,
    description: String,
    trailing: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = if (isSelected) 1.5.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 20.dp)
                .background(color = colors.surface, shape = CircleShape)
                .border(width = if (isSelected) 6.dp else 2.dp, color = if (isSelected) colors.accent else colors.dashed, shape = CircleShape)
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = title, style = typography.bold13, color = colors.ink)
            Text(text = description, style = typography.regular11, color = colors.muted)
        }
        if (trailing != null) Text(text = trailing, style = typography.bold11, color = colors.muted)
    }
}

@Composable
internal fun PrimaryButton(
    text: String,
    icon: DrawableResource? = null,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = if (isEnabled) colors.accent else colors.divider)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Text(text = text, style = typography.bold15, color = if (isEnabled) colors.onAccent else colors.faint)
    }
}

@Composable
internal fun IconTile(icon: DrawableResource, tint: Color, background: Color, size: Int) {
    Box(
        modifier = Modifier
            .size(size = size.dp)
            .background(color = background, shape = RoundedCornerShape(size = (size * 0.31f).dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size = (size / 2).dp)
        )
    }
}

@Composable
private fun DashedNote(text: String) {
    val dashColor = colors.dashed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(x = 16.dp.toPx(), y = 16.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6f, 6f)))
                )
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_shield_check),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.padding(top = 1.dp).size(size = 16.dp)
        )
        Text(text = text, style = typography.regular11, color = colors.muted)
    }
}

/** Shown on the free plan: what Backup & export does, with a way to unlock it. */
@Composable
private fun LockedContent(trialEnd: String?, onUnlock: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 18.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 24.dp))
                .background(color = colors.warmSoft)
                .padding(all = 18.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                IconTile(icon = Res.drawable.ic_cloud_paused, tint = colors.warmInk, background = colors.surface, size = 44)
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                    Text(text = stringResource(resource = Res.string.backup_paused_title), style = typography.bold16, color = colors.ink)
                    Text(
                        text = if (trialEnd != null) {
                            stringResource(resource = Res.string.backup_paused_body, trialEnd)
                        } else {
                            stringResource(resource = Res.string.backup_paused_body_no_date)
                        },
                        style = typography.regular13,
                        color = colors.muted
                    )
                }
            }
            PrimaryButton(text = stringResource(resource = Res.string.backup_unlock), onClick = onUnlock)
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            SectionLabel(text = Res.string.backup_included_with_pro)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            ) {
                val rows = listOf(
                    Triple(Res.string.backup_now, Res.string.backup_locked_backup_detail, Res.drawable.ic_cloud_upload),
                    Triple(Res.string.backup_restore, Res.string.backup_locked_restore_detail, Res.drawable.ic_restore),
                    Triple(Res.string.backup_locked_export, Res.string.backup_locked_export_detail, Res.drawable.ic_download)
                )
                rows.forEachIndexed { index, (title, detail, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button, onClick = onUnlock)
                            .heightIn(min = 64.dp)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                    ) {
                        IconTile(icon = icon, tint = colors.muted, background = colors.divider, size = 36)
                        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                            ) {
                                Text(text = stringResource(resource = title), style = typography.medium13, color = colors.ink)
                                ProBadge()
                            }
                            Text(text = stringResource(resource = detail), style = typography.regular11, color = colors.muted)
                        }
                        Icon(
                            painter = painterResource(resource = Res.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = colors.faint,
                            modifier = Modifier.size(size = 16.dp)
                        )
                    }
                    if (index < rows.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
        DashedNote(text = stringResource(resource = Res.string.backup_locked_note))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBackupScreen() {
    MacroTrackTheme {
        BackupScreen(
            viewState = BackupViewState(lastBackup = LastBackup(atMillis = 0, sizeBytes = 3_300_000)),
            callbacks = BackupCallbacks.default(),
            onBack = {}
        )
    }
}
