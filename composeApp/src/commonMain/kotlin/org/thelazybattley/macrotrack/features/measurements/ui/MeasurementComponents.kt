package org.thelazybattley.macrotrack.features.measurements.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun MeasurementUnitToggle(
    modifier: Modifier = Modifier,
    selected: MeasurementUnit,
    background: Color = colors.surface,
    onSelected: (MeasurementUnit) -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = background)
            .padding(all = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 3.dp)
    ) {
        MeasurementUnit.entries.forEach { unit ->
            val isSelected = unit == selected
            Box(
                modifier = Modifier
                    .height(height = 30.dp)
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = if (isSelected) colors.accent else Color.Transparent)
                    .clickable { onSelected(unit) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = unit.label),
                    style = typography.bold13,
                    color = if (isSelected) colors.onAccent else colors.muted
                )
            }
        }
    }
}

internal fun LocalDate.toMeasurementDate(): String = toAbbreviatedMonthDay() + ", " + year
