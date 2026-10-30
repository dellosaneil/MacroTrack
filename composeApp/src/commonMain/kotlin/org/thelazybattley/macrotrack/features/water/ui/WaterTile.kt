package org.thelazybattley.macrotrack.features.water.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_droplet
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.water_of_goal
import macrotrack.composeapp.generated.resources.water_tile
import macrotrack.composeapp.generated.resources.water_tile_add
import macrotrack.composeapp.generated.resources.water_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WaterSummary
import org.thelazybattley.macrotrack.features.water.WaterViewModel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Today's water on the Today screen, with a one-tap glass; hidden when turned off in its settings. */
@Composable
fun WaterTile(modifier: Modifier = Modifier, onOpen: () -> Unit) {
    val viewModel = koinViewModel<WaterViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val summary = viewState.summary ?: return
    if (!summary.settings.isShownOnToday) return
    WaterTile(modifier = modifier, summary = summary, onOpen = onOpen, onAddGlass = { viewModel.onAdd(kind = WaterKind.GLASS) })
}

@Composable
private fun WaterTile(modifier: Modifier, summary: WaterSummary, onOpen: () -> Unit, onAddGlass: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    val total = waterText(ml = summary.totalMl)
    val goal = waterText(ml = summary.goalMl)
    val description = stringResource(resource = Res.string.water_tile, total, goal)
    Row(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .clickable(onClick = onOpen)
                .clearAndSetSemantics {
                    contentDescription = description
                    role = Role.Button
                }
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Icon(painter = painterResource(resource = Res.drawable.ic_droplet), contentDescription = null, tint = colors.water, modifier = Modifier.size(size = 12.dp))
                Text(text = stringResource(resource = Res.string.water_title), style = typography.bold12, color = colors.muted)
            }
            Column {
                Text(text = total, style = typography.bold18, color = if (summary.isGoalMet) colors.waterInk else colors.ink, maxLines = 1)
                Text(text = stringResource(resource = Res.string.water_of_goal, goal), style = typography.regular11, color = colors.muted, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 6.dp)
                    .clip(shape = RoundedCornerShape(size = 3.dp))
                    .background(color = colors.waterSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = summary.progress)
                        .height(height = 6.dp)
                        .clip(shape = RoundedCornerShape(size = 3.dp))
                        .background(color = colors.water)
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.waterSoft)
                .clickable(role = Role.Button, onClick = onAddGlass),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_plus),
                contentDescription = stringResource(resource = Res.string.water_tile_add, waterText(ml = summary.settings.glassMl)),
                tint = colors.waterInk,
                modifier = Modifier.size(size = 18.dp)
            )
        }
    }
}
