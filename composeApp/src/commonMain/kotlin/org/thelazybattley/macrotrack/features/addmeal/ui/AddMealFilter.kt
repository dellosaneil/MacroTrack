package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.all
import macrotrack.composeapp.generated.resources.favorites
import macrotrack.composeapp.generated.resources.ic_clock
import macrotrack.composeapp.generated.resources.ic_star_filled
import macrotrack.composeapp.generated.resources.recent
import macrotrack.composeapp.generated.resources.recipes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealFilter(
    modifier: Modifier = Modifier,
    selectedFilter: MealFilter,
    favoriteCount: Int,
    onFilterSelected: (MealFilter) -> Unit
) {
    TabRow(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .padding(all = 4.dp),
        spacing = 4.dp
    ) {
        MealFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val contentColor = if (isSelected) colors.ink else colors.muted
            Row(
                modifier = Modifier
                    .height(height = 38.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable(role = Role.Tab) { onFilterSelected(filter) }
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally)
            ) {
                filter.icon?.let { icon ->
                    Icon(
                        painter = painterResource(resource = icon),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(size = 14.dp)
                    )
                }
                Text(
                    text = stringResource(resource = filter.text),
                    style = typography.bold13,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false
                )
                if (filter == MealFilter.FAVORITES && favoriteCount > 0) {
                    Text(
                        text = favoriteCount.toString(),
                        style = typography.bold11,
                        color = if (isSelected) colors.warmInk else colors.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 18.dp)
                            .clip(shape = RoundedCornerShape(size = 9.dp))
                            .background(color = if (isSelected) colors.warmSoft else colors.line)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

enum class MealFilter(
    val text: StringResource,
    val icon: DrawableResource?
) {
    RECENT(text = Res.string.recent, icon = Res.drawable.ic_clock),
    FAVORITES(text = Res.string.favorites, icon = Res.drawable.ic_star_filled),
    ALL(text = Res.string.all, icon = null),
    RECIPES(text = Res.string.recipes, icon = null)
}

/**
 * Lays the tabs out in one row: each gets the width its label needs, and whatever room is left is shared
 * evenly, so a long label like Favorites with its count never gets squeezed. On a very narrow screen every
 * tab shrinks by the same share instead.
 */
@Composable
private fun TabRow(
    modifier: Modifier = Modifier,
    spacing: Dp,
    content: @Composable () -> Unit
) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val available = (constraints.maxWidth - gap * (measurables.size - 1)).coerceAtLeast(minimumValue = 0)
        val natural = measurables.map { it.maxIntrinsicWidth(height = constraints.maxHeight) }
        val total = natural.sum()
        val widths = if (total <= available) {
            val extra = (available - total) / measurables.size
            natural.map { it + extra }
        } else {
            natural.map { it * available / total.coerceAtLeast(minimumValue = 1) }
        }
        val placeables = measurables.mapIndexed { index, measurable ->
            measurable.measure(constraints = Constraints.fixedWidth(width = widths[index]).copy(maxHeight = constraints.maxHeight))
        }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width = constraints.maxWidth, height = height) {
            var x = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = x, y = (height - placeable.height) / 2)
                x += placeable.width + gap
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealFilter() {
    MacroTrackTheme {
        AddMealFilter(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            selectedFilter = MealFilter.FAVORITES,
            favoriteCount = 4,
            onFilterSelected = {}
        )
    }
}
