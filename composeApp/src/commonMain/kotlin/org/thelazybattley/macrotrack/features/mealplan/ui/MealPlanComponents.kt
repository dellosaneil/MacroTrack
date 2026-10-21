package org.thelazybattley.macrotrack.features.mealplan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.copy_amount_quick_add
import macrotrack.composeapp.generated.resources.copy_amount_recipe
import macrotrack.composeapp.generated.resources.days_every_day
import macrotrack.composeapp.generated.resources.days_weekdays
import macrotrack.composeapp.generated.resources.days_weekends
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.features.mealplan.DayPattern
import org.thelazybattley.macrotrack.ui.common.formatPortion
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** "Every day", "Weekdays", "Weekends", "Sundays" for one day, or the days in week order: "Mon, Wed, Fri". */
@Composable
internal fun daysLabel(days: Set<DayOfWeek>): String = when (DayPattern.of(days = days)) {
    DayPattern.EVERY_DAY -> stringResource(resource = Res.string.days_every_day)
    DayPattern.WEEKDAYS -> stringResource(resource = Res.string.days_weekdays)
    DayPattern.WEEKENDS -> stringResource(resource = Res.string.days_weekends)
    null -> days.singleOrNull()?.let { it.fullName() + "s" }
        ?: DayOfWeek.entries.filter { it in days }.joinToString(separator = ", ") { it.shortName() }
}

internal fun DayOfWeek.fullName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

internal fun DayOfWeek.shortName(): String = fullName().take(n = 3)

/** A planned portion as on the food log: "200 g", or Recipe or Quick add when there's no amount to show. */
@Composable
internal fun PlanFood.amountText(foodUnit: Map<String, String>): String = when {
    isQuickAdd -> stringResource(resource = Res.string.copy_amount_quick_add)
    // Like a logged recipe, a planned one keeps a weight of 0.0.
    weight == 0.0 -> stringResource(resource = Res.string.copy_amount_recipe)
    else -> formatPortion(amount = weight, storedUnit = foodUnit[foodName].orEmpty())
}

/** The meal plan screens' header row: a square button, the title block, and an optional trailing button. */
@Composable
internal fun PlanHeader(
    leadingIcon: DrawableResource,
    leadingDescription: String,
    onLeading: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        SquareIconButton(icon = leadingIcon, description = leadingDescription, onClick = onLeading)
        content()
        trailing?.invoke()
    }
}

@Composable
internal fun SquareIconButton(icon: DrawableResource, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = description,
            tint = colors.ink,
            modifier = Modifier.size(size = 20.dp)
        )
    }
}

/** The small night-colored tag on planned food: "Planned", "↻ Weekdays". */
@Composable
internal fun PlanTag(text: String) {
    Text(
        text = text,
        style = typography.bold10,
        color = colors.nightInk,
        modifier = Modifier
            .background(color = colors.nightSoft, shape = RoundedCornerShape(size = 6.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}
