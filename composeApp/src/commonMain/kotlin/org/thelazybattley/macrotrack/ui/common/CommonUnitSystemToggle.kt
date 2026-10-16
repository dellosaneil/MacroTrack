package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.units_imperial
import macrotrack.composeapp.generated.resources.units_imperial_detail
import macrotrack.composeapp.generated.resources.units_metric
import macrotrack.composeapp.generated.resources.units_metric_detail
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Metric or imperial, as a two-part switch. Each part reads its units aloud ("Metric, kg · cm"). */
@Composable
fun CommonUnitSystemToggle(
    modifier: Modifier = Modifier,
    selected: UnitSystem,
    onSelected: (UnitSystem) -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            .padding(all = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        listOf(
            Triple(UnitSystem.METRIC, Res.string.units_metric, Res.string.units_metric_detail),
            Triple(UnitSystem.IMPERIAL, Res.string.units_imperial, Res.string.units_imperial_detail)
        ).forEach { (unitSystem, label, detail) ->
            val isSelected = unitSystem == selected
            val description = stringResource(resource = label) + ", " + stringResource(resource = detail)
            Box(
                modifier = Modifier
                    .height(height = 36.dp)
                    .clip(shape = RoundedCornerShape(size = 9.dp))
                    .background(color = if (isSelected) colors.accent else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelected(unitSystem) }
                    .semantics {
                        this.selected = isSelected
                        contentDescription = description
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = label),
                    style = typography.bold13,
                    color = if (isSelected) colors.onAccent else colors.muted
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCommonUnitSystemToggle() {
    MacroTrackTheme {
        CommonUnitSystemToggle(selected = UnitSystem.IMPERIAL, onSelected = {})
    }
}
