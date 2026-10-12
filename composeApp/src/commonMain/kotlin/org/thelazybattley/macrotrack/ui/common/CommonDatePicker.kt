package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.date
import macrotrack.composeapp.generated.resources.ic_nav_log
import macrotrack.composeapp.generated.resources.ok
import macrotrack.composeapp.generated.resources.today_value
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private const val MILLIS_PER_DAY = 86_400_000L

@Composable
fun CommonDateButton(
    modifier: Modifier = Modifier,
    date: LocalDate,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_nav_log),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.size(size = 18.dp)
        )
        Column {
            Text(text = stringResource(resource = Res.string.date), style = typography.regular10, color = colors.muted)
            Text(
                text = if (date == getCurrentDate()) {
                    stringResource(resource = Res.string.today_value, date.toFullDate())
                } else {
                    date.toFullDate()
                },
                style = typography.bold13,
                color = colors.ink
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonDatePickerDialog(
    selected: LocalDate,
    minDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val todayMillis = getCurrentDate().toEpochDays() * MILLIS_PER_DAY
    val minMillis = minDate?.let { it.toEpochDays() * MILLIS_PER_DAY } ?: Long.MIN_VALUE
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selected.toEpochDays() * MILLIS_PER_DAY,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minMillis..todayMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton onDismiss()
                    onConfirm(LocalDate.fromEpochDays(millis / MILLIS_PER_DAY))
                }
            ) {
                Text(text = stringResource(resource = Res.string.ok), color = colors.accentInk)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(resource = Res.string.cancel), color = colors.muted)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun LocalDate.toFullDate(): String = toAbbreviatedMonthDay() + ", " + year
