package org.thelazybattley.macrotrack.features.checklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_all_done_today
import macrotrack.composeapp.generated.resources.checklist_n_of_m_done
import macrotrack.composeapp.generated.resources.checklist_next
import macrotrack.composeapp.generated.resources.checklist_row_aria
import macrotrack.composeapp.generated.resources.checklist_row_empty_line
import macrotrack.composeapp.generated.resources.checklist_row_label
import macrotrack.composeapp.generated.resources.checklist_subtitle
import macrotrack.composeapp.generated.resources.checklist_title
import macrotrack.composeapp.generated.resources.ic_checklist
import macrotrack.composeapp.generated.resources.ic_chevron_right
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.DayMark
import org.thelazybattley.macrotrack.domain.usecase.checklist.phrase
import org.thelazybattley.macrotrack.features.checklist.ChecklistViewModel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Most dots the row shows, one per item; past that it's only the count. */
private const val MAX_DOTS = 7

/** Today's checklist on the Today screen: done so far, what's next and a dot per item. Hidden when turned off in Manage. */
@Composable
fun ChecklistTile(modifier: Modifier = Modifier, onOpen: () -> Unit) {
    val viewModel = koinViewModel<ChecklistViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val checklist = viewState.checklist ?: return
    if (!checklist.settings.isShownOnToday) return
    val summary = checklist.summaryOn(date = checklist.today)
    val marks = summary.items.map { checklist.markOn(item = it, date = checklist.today) }.filter { it != DayMark.SKIPPED }
    val next = summary.next
    val label = if (next != null) stringResource(resource = Res.string.checklist_row_label, next.name.lowercase()) else stringResource(resource = Res.string.checklist_title)
    val line = when {
        checklist.items.isEmpty() -> stringResource(resource = Res.string.checklist_row_empty_line)
        summary.total == 0 -> stringResource(resource = Res.string.checklist_subtitle)
        summary.done >= summary.total -> stringResource(resource = Res.string.checklist_all_done_today)
        else -> stringResource(resource = Res.string.checklist_n_of_m_done, summary.done, summary.total)
    }
    val description = stringResource(
        resource = Res.string.checklist_row_aria,
        if (next != null) line + ". " + stringResource(resource = Res.string.checklist_next, next.name, stringResource(resource = next.time.phrase)) else line
    )
    val shape = RoundedCornerShape(size = 18.dp)
    Row(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onOpen)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(size = 34.dp).clip(shape = RoundedCornerShape(size = 10.dp)).background(color = colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = Res.drawable.ic_checklist), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 1.dp)) {
            Text(text = label, style = typography.bold12, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = line, style = typography.bold14, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (marks.size in 1..MAX_DOTS) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                marks.forEach { mark ->
                    Box(
                        modifier = Modifier
                            .size(size = 8.dp)
                            .clip(shape = CircleShape)
                            .then(
                                if (mark == DayMark.DONE) Modifier.background(color = colors.accent)
                                else Modifier.border(width = 1.5.dp, color = colors.dashed, shape = CircleShape)
                            )
                    )
                }
            }
        }
        Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.faint, modifier = Modifier.size(size = 14.dp))
    }
}
