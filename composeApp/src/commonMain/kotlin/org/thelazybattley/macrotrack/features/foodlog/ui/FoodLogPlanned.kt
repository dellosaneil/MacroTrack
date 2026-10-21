package org.thelazybattley.macrotrack.features.foodlog.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.planned_all_done
import macrotrack.composeapp.generated.resources.planned_ate_all
import macrotrack.composeapp.generated.resources.planned_ate_description
import macrotrack.composeapp.generated.resources.planned_ate_it
import macrotrack.composeapp.generated.resources.planned_badge
import macrotrack.composeapp.generated.resources.planned_logged_undo
import macrotrack.composeapp.generated.resources.planned_skip
import macrotrack.composeapp.generated.resources.planned_skip_description
import macrotrack.composeapp.generated.resources.planned_skipped_undo
import macrotrack.composeapp.generated.resources.planned_to_go
import macrotrack.composeapp.generated.resources.planned_undo_description
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.PlanStatus
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/**
 * Food planned for this meal today, from the meal plan or a repeating meal. It isn't counted until it's marked
 * eaten; each item waits for Ate it or Skip, and either can be undone.
 */
@Composable
internal fun PlannedSection(
    modifier: Modifier = Modifier,
    planned: List<PlannedFood>,
    onAteAll: () -> Unit,
    onAte: (PlannedFood) -> Unit,
    onSkip: (PlannedFood) -> Unit,
    onUndo: (PlannedFood) -> Unit
) {
    val pending = planned.filter { it.isPending }
    Column(modifier = modifier.background(color = colors.background)) {
        HorizontalDivider(thickness = 1.dp, color = colors.dashed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.planned_badge),
                style = typography.bold11,
                color = colors.nightInk,
                modifier = Modifier
                    .background(color = colors.nightSoft, shape = RoundedCornerShape(size = 7.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            )
            Text(
                text = if (pending.isEmpty()) {
                    stringResource(resource = Res.string.planned_all_done)
                } else {
                    stringResource(
                        resource = Res.string.planned_to_go,
                        pluralStringResource(resource = Res.plurals.items, quantity = pending.size, pending.size),
                        pending.sumOf { it.food.calories }.withThousands()
                    )
                },
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            if (pending.isNotEmpty()) {
                PlanButton(
                    text = stringResource(resource = Res.string.planned_ate_all),
                    background = colors.accent,
                    textColor = colors.onAccent,
                    onClick = onAteAll
                )
            }
        }
        planned.forEach { item ->
            PlannedItemRow(item = item, onAte = { onAte(item) }, onSkip = { onSkip(item) }, onUndo = { onUndo(item) })
        }
    }
}

@Composable
private fun PlannedItemRow(item: PlannedFood, onAte: () -> Unit, onSkip: () -> Unit, onUndo: () -> Unit) {
    val name = item.food.foodName
    val isSkipped = item.status == PlanStatus.SKIPPED
    val details = " · " + item.food.calories.withThousands() + " " + stringResource(resource = Res.string.kcal_text)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = if (isSkipped) colors.muted else colors.ink)) { append(text = name) }
                withStyle(style = SpanStyle(color = colors.muted)) { append(text = details) }
            },
            style = typography.medium13.copy(textDecoration = if (isSkipped) TextDecoration.LineThrough else null),
            modifier = Modifier.weight(weight = 1f)
        )
        if (item.isPending) {
            val skipDescription = stringResource(resource = Res.string.planned_skip_description, name)
            val ateDescription = stringResource(resource = Res.string.planned_ate_description, name)
            PlanButton(
                modifier = Modifier.semantics { contentDescription = skipDescription },
                text = stringResource(resource = Res.string.planned_skip),
                background = colors.surface,
                textColor = colors.muted,
                border = colors.line,
                onClick = onSkip
            )
            PlanButton(
                modifier = Modifier.semantics { contentDescription = ateDescription },
                text = stringResource(resource = Res.string.planned_ate_it),
                background = colors.accentSoft,
                textColor = colors.accentInk,
                onClick = onAte
            )
        } else {
            val undoDescription = stringResource(resource = Res.string.planned_undo_description, name)
            PlanButton(
                modifier = Modifier.semantics { contentDescription = undoDescription },
                text = stringResource(resource = if (isSkipped) Res.string.planned_skipped_undo else Res.string.planned_logged_undo),
                background = Color.Transparent,
                textColor = colors.muted,
                onClick = onUndo
            )
        }
    }
}

@Composable
private fun PlanButton(
    modifier: Modifier = Modifier,
    text: String,
    background: Color,
    textColor: Color,
    border: Color? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 10.dp)
    Box(
        modifier = modifier
            .height(height = 32.dp)
            .clip(shape = shape)
            .background(color = background)
            .then(if (border != null) Modifier.border(width = 1.dp, color = border, shape = shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold12, color = textColor)
    }
}
