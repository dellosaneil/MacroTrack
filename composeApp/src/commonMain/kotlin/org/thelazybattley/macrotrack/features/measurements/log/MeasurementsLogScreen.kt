package org.thelazybattley.macrotrack.features.measurements.log

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.date
import macrotrack.composeapp.generated.resources.fill_only_measured
import macrotrack.composeapp.generated.resources.ic_nav_log
import macrotrack.composeapp.generated.resources.last_value_on
import macrotrack.composeapp.generated.resources.log_measurements
import macrotrack.composeapp.generated.resources.note_optional
import macrotrack.composeapp.generated.resources.note_placeholder
import macrotrack.composeapp.generated.resources.ok
import macrotrack.composeapp.generated.resources.save_measurements
import macrotrack.composeapp.generated.resources.today_value
import macrotrack.composeapp.generated.resources.value_with_unit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.MeasurementGroup
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.formatMeasure
import org.thelazybattley.macrotrack.features.measurements.ui.MeasurementUnitToggle
import org.thelazybattley.macrotrack.features.measurements.ui.toMeasurementDate
import org.thelazybattley.macrotrack.ui.common.CommonDateButton
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun MeasurementsLogScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<MeasurementsLogViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onPopBackStack()
    }
    MeasurementsLogScreen(
        modifier = modifier,
        viewState = viewState,
        onBack = onPopBackStack,
        onDateSelected = viewModel::onDateSelected,
        onUnitSelected = viewModel::onUnitSelected,
        onValueChanged = viewModel::onValueChanged,
        onNoteChanged = viewModel::onNoteChanged,
        onSave = viewModel::onSave
    )
}

@Composable
private fun MeasurementsLogScreen(
    modifier: Modifier = Modifier,
    viewState: MeasurementsLogViewState,
    onBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onUnitSelected: (MeasurementUnit) -> Unit,
    onValueChanged: (MeasurementType, String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(value = false) }
    val filled = viewState.validValues.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(title = Res.string.log_measurements, onBack = onBack)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(height = 54.dp)
                    .clip(shape = RoundedCornerShape(size = 16.dp))
                    .background(color = if (filled > 0) colors.accent else colors.line)
                    .clickable(enabled = filled > 0, onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pluralStringResource(resource = Res.plurals.save_measurements, quantity = filled, filled),
                    style = typography.bold16,
                    color = if (filled > 0) colors.onAccent else colors.faint
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                CommonDateButton(
                    modifier = Modifier.weight(weight = 1f),
                    date = viewState.date,
                    onClick = { showDatePicker = true }
                )
                MeasurementUnitToggle(
                    selected = viewState.unit,
                    background = colors.divider,
                    onSelected = onUnitSelected
                )
            }
            Text(
                text = stringResource(resource = Res.string.fill_only_measured),
                style = typography.regular11,
                color = colors.muted
            )
            MeasurementGroup.entries.forEach { group ->
                InputGroup(
                    group = group,
                    viewState = viewState,
                    onValueChanged = onValueChanged
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(
                    text = stringResource(resource = Res.string.note_optional),
                    style = typography.bold12,
                    color = colors.muted
                )
                InputBox(
                    modifier = Modifier.fillMaxWidth().height(height = 48.dp),
                    value = viewState.note,
                    placeholder = stringResource(resource = Res.string.note_placeholder),
                    textAlign = TextAlign.Start,
                    keyboardType = KeyboardType.Text,
                    isFilled = false,
                    onValueChange = onNoteChanged
                )
            }
        }
    }

    if (showDatePicker) {
        CommonDatePickerDialog(
            selected = viewState.date,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                showDatePicker = false
                onDateSelected(date)
            }
        )
    }
}

@Composable
private fun InputGroup(
    group: MeasurementGroup,
    viewState: MeasurementsLogViewState,
    onValueChanged: (MeasurementType, String) -> Unit
) {
    val types = MeasurementType.entries.filter { it.group == group }
    val unitLabel = stringResource(resource = viewState.unit.label)
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = stringResource(resource = group.title),
            style = typography.bold12,
            color = colors.muted
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            types.forEachIndexed { index, type ->
                val previous = viewState.previous[type]?.latest
                val previousValue = previous?.let { viewState.unit.fromCm(cm = it.valueCm).formatMeasure() }
                val value = viewState.inputs[type].orEmpty()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(
                            text = stringResource(resource = type.title),
                            style = typography.bold14,
                            color = colors.ink
                        )
                        if (previous != null && previousValue != null) {
                            Text(
                                text = stringResource(
                                    resource = Res.string.last_value_on,
                                    stringResource(resource = Res.string.value_with_unit, previousValue, unitLabel),
                                    previous.date.toAbbreviatedMonthDay()
                                ),
                                style = typography.regular11,
                                color = colors.muted
                            )
                        }
                    }
                    InputBox(
                        modifier = Modifier.width(width = 84.dp).height(height = 40.dp),
                        value = value,
                        placeholder = previousValue.orEmpty(),
                        textAlign = TextAlign.End,
                        keyboardType = KeyboardType.Decimal,
                        isFilled = value.toDoubleOrNull() != null,
                        onValueChange = { onValueChanged(type, it) }
                    )
                    Text(
                        text = unitLabel,
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.width(width = 22.dp)
                    )
                }
                if (index != types.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

@Composable
private fun InputBox(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    textAlign: TextAlign,
    keyboardType: KeyboardType,
    isFilled: Boolean,
    onValueChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(size = 10.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = typography.bold15.copy(color = colors.ink, textAlign = textAlign),
        cursorBrush = SolidColor(value = colors.accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .clip(shape = shape)
            .background(color = if (isFilled) colors.surface else colors.background)
            .border(
                width = if (isFilled) 1.5.dp else 1.dp,
                color = if (isFilled) colors.accent else colors.line,
                shape = shape
            ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                contentAlignment = if (textAlign == TextAlign.End) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = typography.regular13.copy(textAlign = textAlign),
                        color = colors.faint
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewMeasurementsLogScreen() {
    MacroTrackTheme {
        MeasurementsLogScreen(
            viewState = MeasurementsLogViewState(),
            onBack = {},
            onDateSelected = {},
            onUnitSelected = {},
            onValueChanged = { _, _ -> },
            onNoteChanged = {},
            onSave = {}
        )
    }
}
