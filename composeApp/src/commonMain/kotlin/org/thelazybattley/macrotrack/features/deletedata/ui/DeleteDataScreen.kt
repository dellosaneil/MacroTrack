package org.thelazybattley.macrotrack.features.deletedata.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.delete_data_backups_note
import macrotrack.composeapp.generated.resources.delete_data_button
import macrotrack.composeapp.generated.resources.delete_data_button_locked
import macrotrack.composeapp.generated.resources.delete_data_copy_detail
import macrotrack.composeapp.generated.resources.delete_data_copy_title
import macrotrack.composeapp.generated.resources.delete_data_deleting_body
import macrotrack.composeapp.generated.resources.delete_data_deleting_title
import macrotrack.composeapp.generated.resources.delete_data_done_backups
import macrotrack.composeapp.generated.resources.delete_data_done_body
import macrotrack.composeapp.generated.resources.delete_data_done_title
import macrotrack.composeapp.generated.resources.delete_data_entries_since
import macrotrack.composeapp.generated.resources.delete_data_failed
import macrotrack.composeapp.generated.resources.delete_data_item_settings
import macrotrack.composeapp.generated.resources.delete_data_pro_body
import macrotrack.composeapp.generated.resources.delete_data_pro_title
import macrotrack.composeapp.generated.resources.delete_data_settings_detail
import macrotrack.composeapp.generated.resources.delete_data_start_over
import macrotrack.composeapp.generated.resources.delete_data_step_body
import macrotrack.composeapp.generated.resources.delete_data_step_food_log
import macrotrack.composeapp.generated.resources.delete_data_step_foods
import macrotrack.composeapp.generated.resources.delete_data_step_settings
import macrotrack.composeapp.generated.resources.delete_data_title
import macrotrack.composeapp.generated.resources.delete_data_type_to_confirm
import macrotrack.composeapp.generated.resources.delete_data_warning_body
import macrotrack.composeapp.generated.resources.delete_data_warning_title
import macrotrack.composeapp.generated.resources.delete_data_what
import macrotrack.composeapp.generated.resources.export_count_changes
import macrotrack.composeapp.generated.resources.export_count_entries
import macrotrack.composeapp.generated.resources.export_count_foods
import macrotrack.composeapp.generated.resources.export_count_recipes
import macrotrack.composeapp.generated.resources.export_foods_and_recipes
import macrotrack.composeapp.generated.resources.export_item_food_log
import macrotrack.composeapp.generated.resources.export_item_foods
import macrotrack.composeapp.generated.resources.export_item_goals
import macrotrack.composeapp.generated.resources.export_item_measurements
import macrotrack.composeapp.generated.resources.export_item_weigh_ins
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_download
import macrotrack.composeapp.generated.resources.ic_file
import macrotrack.composeapp.generated.resources.ic_info
import macrotrack.composeapp.generated.resources.ic_trash
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.DELETE_CONFIRMATION_WORD
import org.thelazybattley.macrotrack.domain.model.DeleteDataStep
import org.thelazybattley.macrotrack.features.backup.ui.IconTile
import org.thelazybattley.macrotrack.features.backup.ui.PrimaryButton
import org.thelazybattley.macrotrack.features.deletedata.DeleteDataCallbacks
import org.thelazybattley.macrotrack.features.deletedata.DeleteDataStage
import org.thelazybattley.macrotrack.features.deletedata.DeleteDataSummary
import org.thelazybattley.macrotrack.features.deletedata.DeleteDataViewModel
import org.thelazybattley.macrotrack.features.deletedata.DeleteDataViewState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.tour.ui.TourBackHandler
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun DeleteDataScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenBackup: () -> Unit,
    onStartOver: () -> Unit
) {
    val viewModel = koinViewModel<DeleteDataViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    when (viewState.stage) {
        // Leaving halfway would show screens with half their data gone, so back waits for the delete to finish.
        DeleteDataStage.DELETING -> TourBackHandler {}
        // There's nothing left to go back to.
        DeleteDataStage.DONE -> TourBackHandler(onBack = onStartOver)
        DeleteDataStage.FORM -> Unit
    }
    DeleteDataScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onOpenBackup = onOpenBackup,
        onStartOver = onStartOver
    )
}

@Composable
private fun DeleteDataScreen(
    modifier: Modifier = Modifier,
    viewState: DeleteDataViewState,
    callbacks: DeleteDataCallbacks,
    onBack: () -> Unit,
    onOpenBackup: () -> Unit,
    onStartOver: () -> Unit
) {
    when (viewState.stage) {
        DeleteDataStage.FORM -> Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = colors.background,
            topBar = { CommonScreenHeader(title = Res.string.delete_data_title, onBack = onBack) }
        ) { innerPadding ->
            DeleteDataForm(
                modifier = Modifier.padding(paddingValues = innerPadding),
                viewState = viewState,
                callbacks = callbacks,
                onCancel = onBack,
                onOpenBackup = onOpenBackup
            )
        }
        DeleteDataStage.DELETING -> DeletingContent(modifier = modifier, currentStep = viewState.currentStep)
        DeleteDataStage.DONE -> DoneContent(modifier = modifier, onStartOver = onStartOver)
    }
}

@Composable
private fun DeleteDataForm(
    modifier: Modifier = Modifier,
    viewState: DeleteDataViewState,
    callbacks: DeleteDataCallbacks,
    onCancel: () -> Unit,
    onOpenBackup: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 18.dp)
    ) {
        WarningCard()
        DeletedItems(summary = viewState.summary)
        InfoCard(icon = Res.drawable.ic_file, text = stringResource(resource = Res.string.delete_data_backups_note))
        ProNote()
        CopyFirstRow(onClick = onOpenBackup)
        ConfirmField(typed = viewState.typed, isConfirmed = viewState.canDelete, onTyped = callbacks::onTyped)
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            if (viewState.hasFailed) {
                Text(
                    text = stringResource(resource = Res.string.delete_data_failed),
                    style = typography.bold12,
                    color = colors.danger,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            DangerButton(
                text = if (viewState.canDelete) {
                    stringResource(resource = Res.string.delete_data_button)
                } else {
                    stringResource(resource = Res.string.delete_data_button_locked, DELETE_CONFIRMATION_WORD)
                },
                isEnabled = viewState.canDelete,
                onClick = callbacks::onDelete
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .clickable(role = Role.Button, onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = Res.string.cancel), style = typography.bold14, color = colors.muted)
            }
        }
    }
}

@Composable
private fun WarningCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.dangerSoft)
            .padding(all = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        IconTile(icon = Res.drawable.ic_trash, tint = colors.danger, background = colors.surface, size = 44)
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(text = stringResource(resource = Res.string.delete_data_warning_title), style = typography.bold16, color = colors.ink)
            Text(text = stringResource(resource = Res.string.delete_data_warning_body), style = typography.regular13, color = colors.muted)
        }
    }
}

@Composable
private fun DeletedItems(summary: DeleteDataSummary) {
    val logCount = pluralStringResource(
        resource = Res.plurals.export_count_entries,
        quantity = summary.logCount,
        summary.logCount.withThousands()
    )
    val rows = listOf(
        Res.string.export_item_food_log to (summary.firstLogDate?.let { firstDate ->
            stringResource(resource = Res.string.delete_data_entries_since, logCount, firstDate.toAbbreviatedMonthDay())
        } ?: logCount),
        Res.string.export_item_weigh_ins to summary.weighInCount.withThousands(),
        Res.string.export_item_measurements to summary.measurementCount.withThousands(),
        Res.string.export_item_foods to stringResource(
            resource = Res.string.export_foods_and_recipes,
            pluralStringResource(resource = Res.plurals.export_count_foods, quantity = summary.foodCount, summary.foodCount),
            pluralStringResource(resource = Res.plurals.export_count_recipes, quantity = summary.recipeCount, summary.recipeCount)
        ),
        Res.string.export_item_goals to pluralStringResource(
            resource = Res.plurals.export_count_changes,
            quantity = summary.goalChangeCount,
            summary.goalChangeCount
        ),
        Res.string.delete_data_item_settings to stringResource(resource = Res.string.delete_data_settings_detail)
    )
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = Res.string.delete_data_what), style = typography.bold12, color = colors.muted)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            rows.forEachIndexed { index, (name, detail) ->
                DeletedItemRow(name = name, detail = detail)
                if (index < rows.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            }
        }
    }
}

@Composable
private fun DeletedItemRow(name: StringResource, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_close),
            contentDescription = null,
            tint = colors.danger,
            modifier = Modifier.size(size = 16.dp)
        )
        Text(text = stringResource(resource = name), style = typography.medium13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
        Text(text = detail, style = typography.regular11, color = colors.muted, textAlign = TextAlign.End)
    }
}

@Composable
private fun InfoCard(icon: DrawableResource, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.padding(top = 1.dp).size(size = 18.dp)
        )
        Text(text = text, style = typography.regular13, color = colors.muted)
    }
}

@Composable
private fun ProNote() {
    val dashColor = colors.dashed
    val title = stringResource(resource = Res.string.delete_data_pro_title)
    val body = stringResource(resource = Res.string.delete_data_pro_body)
    val ink = colors.ink
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
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_info),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.padding(top = 1.dp).size(size = 18.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = ink, fontWeight = FontWeight.Bold)) { append(title) }
                append(" ")
                append(body)
            },
            style = typography.regular13,
            color = colors.muted
        )
    }
}

@Composable
private fun CopyFirstRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        IconTile(icon = Res.drawable.ic_download, tint = colors.accentInk, background = colors.accentSoft, size = 36)
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = stringResource(resource = Res.string.delete_data_copy_title), style = typography.bold13, color = colors.ink)
            Text(text = stringResource(resource = Res.string.delete_data_copy_detail), style = typography.regular11, color = colors.muted)
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

@Composable
private fun ConfirmField(typed: String, isConfirmed: Boolean, onTyped: (String) -> Unit) {
    val label = stringResource(resource = Res.string.delete_data_type_to_confirm, DELETE_CONFIRMATION_WORD)
    val wordStart = label.indexOf(string = DELETE_CONFIRMATION_WORD)
    val chip = colors.divider
    val wordStyle = typography.bold16.copy(fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp)
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = buildAnnotatedString {
                append(label)
                // The word to type stands out in a monospace chip, wherever a translation puts it.
                if (wordStart >= 0) {
                    addStyle(
                        style = SpanStyle(fontFamily = FontFamily.Monospace, background = chip),
                        start = wordStart,
                        end = wordStart + DELETE_CONFIRMATION_WORD.length
                    )
                }
            },
            style = typography.bold13,
            color = colors.ink
        )
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = DELETE_CONFIRMATION_WORD,
            textValue = typed,
            borderColor = if (isConfirmed) colors.danger else colors.line,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            textStyle = wordStyle,
            onValueChanged = onTyped
        )
    }
}

@Composable
private fun DangerButton(text: String, isEnabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 54.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = if (isEnabled) colors.danger else colors.divider)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold16, color = if (isEnabled) colors.onDanger else colors.faint)
    }
}

@Composable
private fun DeletingContent(modifier: Modifier = Modifier, currentStep: DeleteDataStep?) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 120.dp, bottom = 40.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(space = 22.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size = 44.dp),
            color = colors.danger,
            trackColor = colors.dangerTrack,
            strokeWidth = 4.dp
        )
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Text(text = stringResource(resource = Res.string.delete_data_deleting_title), style = typography.bold24, color = colors.ink)
            Text(text = stringResource(resource = Res.string.delete_data_deleting_body), style = typography.regular13, color = colors.muted)
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            val current = currentStep?.ordinal ?: 0
            DeleteDataStep.entries.forEach { step ->
                StepRow(label = step.label(), isDone = step.ordinal < current, isCurrent = step.ordinal == current)
            }
        }
    }
}

@Composable
private fun StepRow(label: StringResource, isDone: Boolean, isCurrent: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        when {
            isDone -> Box(
                modifier = Modifier.size(size = 22.dp).background(color = colors.accent, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(size = 12.dp)
                )
            }
            isCurrent -> CircularProgressIndicator(
                modifier = Modifier.size(size = 22.dp),
                color = colors.danger,
                trackColor = colors.dangerTrack,
                strokeWidth = 2.5.dp
            )
            else -> Box(modifier = Modifier.size(size = 22.dp).border(width = 2.dp, color = colors.dashed, shape = CircleShape))
        }
        Text(
            text = stringResource(resource = label),
            style = if (isCurrent) typography.bold14 else typography.regular13,
            color = when {
                isDone -> colors.muted
                isCurrent -> colors.ink
                else -> colors.faint
            }
        )
    }
}

private fun DeleteDataStep.label(): StringResource = when (this) {
    DeleteDataStep.FOOD_LOG -> Res.string.delete_data_step_food_log
    DeleteDataStep.BODY_RECORDS -> Res.string.delete_data_step_body
    DeleteDataStep.SAVED_FOODS -> Res.string.delete_data_step_foods
    DeleteDataStep.GOALS_AND_SETTINGS -> Res.string.delete_data_step_settings
}

@Composable
private fun DoneContent(modifier: Modifier = Modifier, onStartOver: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 120.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 22.dp)
    ) {
        IconTile(icon = Res.drawable.ic_check, tint = colors.accentInk, background = colors.accentSoft, size = 64)
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Text(text = stringResource(resource = Res.string.delete_data_done_title), style = typography.bold24, color = colors.ink)
            Text(text = stringResource(resource = Res.string.delete_data_done_body), style = typography.regular13, color = colors.muted)
        }
        InfoCard(icon = Res.drawable.ic_file, text = stringResource(resource = Res.string.delete_data_done_backups))
        Spacer(modifier = Modifier.weight(weight = 1f))
        PrimaryButton(text = stringResource(resource = Res.string.delete_data_start_over), onClick = onStartOver)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDeleteDataScreen() {
    MacroTrackTheme {
        DeleteDataScreen(
            viewState = DeleteDataViewState(
                summary = DeleteDataSummary(
                    logCount = 1284,
                    firstLogDate = LocalDate(2026, 7, 6),
                    weighInCount = 96,
                    measurementCount = 24,
                    foodCount = 38,
                    recipeCount = 6,
                    goalChangeCount = 3
                ),
                typed = DELETE_CONFIRMATION_WORD
            ),
            callbacks = DeleteDataCallbacks.default(),
            onBack = {},
            onOpenBackup = {},
            onStartOver = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDeletingContent() {
    MacroTrackTheme {
        DeletingContent(currentStep = DeleteDataStep.SAVED_FOODS)
    }
}
