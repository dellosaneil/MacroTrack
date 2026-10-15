package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.daysUntil
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_to_favorites
import macrotrack.composeapp.generated.resources.add_value
import macrotrack.composeapp.generated.resources.days_ago
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_star_filled
import macrotrack.composeapp.generated.resources.ic_star_outline
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.one_serving
import macrotrack.composeapp.generated.resources.p_c_f
import macrotrack.composeapp.generated.resources.percent_of_serving
import macrotrack.composeapp.generated.resources.recipe
import macrotrack.composeapp.generated.resources.remove_from_favorites
import macrotrack.composeapp.generated.resources.remove_value
import macrotrack.composeapp.generated.resources.scan_new_tag
import macrotrack.composeapp.generated.resources.today
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.yesterday
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.LastLogged
import org.thelazybattley.macrotrack.features.addmeal.MealListItem
import org.thelazybattley.macrotrack.features.addmeal.SelectedMealItem
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddMealListItem(
    modifier: Modifier = Modifier,
    item: MealListItem,
    selectedItem: SelectedMealItem?,
    isExpanded: Boolean,
    showLastLogged: Boolean,
    onToggle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onAmountChanged: (key: String, amount: Double) -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    val isSelected = selectedItem != null
    // On Recent, an unselected row shows the portion logged last time and when.
    val lastLogged = item.lastLogged?.takeIf { showLastLogged && !isSelected }
    val food = selectedItem?.food ?: lastLogged?.food ?: item.food
    // A food the scanner just saved gets a dashed outline until it's picked.
    val isDashed = item.isJustScanned && !isSelected
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .then(
                if (isDashed) {
                    Modifier.dashedBorder(color = colors.accent, cornerRadius = 16.dp)
                } else {
                    Modifier.border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Min)
                .combinedClickable(onClick = onClick, onLongClick = onLongPress),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(width = 5.dp)
                    .fillMaxHeight()
                    .background(color = food.dominantMacro.stripeColor())
            )
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(start = 4.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Text(
                        text = food.name,
                        style = typography.bold15,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(weight = 1f, fill = false)
                    )
                    if (item.isRecipe) {
                        Text(
                            text = stringResource(resource = Res.string.recipe).uppercase(),
                            style = typography.bold10,
                            color = colors.accentInk,
                            modifier = Modifier
                                .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                    if (item.isJustScanned) {
                        Text(
                            text = stringResource(resource = Res.string.scan_new_tag),
                            style = typography.bold10,
                            color = colors.warmInk,
                            maxLines = 1,
                            modifier = Modifier
                                .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                val recipeAmount = selectedItem?.amount ?: lastLogged?.amount
                val portion = when {
                    item.isRecipe && recipeAmount != null -> stringResource(
                        resource = Res.string.percent_of_serving,
                        recipeAmount.toCleanString()
                    )
                    item.isRecipe -> stringResource(resource = Res.string.one_serving)
                    else -> stringResource(resource = Res.string.value_unit, food.weight.toCleanString(), food.unit)
                }
                val detail = if (lastLogged != null) {
                    lastLogged.relativeDay()
                } else {
                    stringResource(
                        resource = Res.string.p_c_f,
                        food.macros.protein.toInt(),
                        food.macros.carbs.toInt(),
                        food.macros.fat.toInt()
                    )
                }
                Text(
                    text = "$portion · $detail",
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = food.macros.calories.withThousands(),
                    style = typography.bold15,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.kcal_text),
                    style = typography.regular10,
                    color = colors.muted
                )
            }
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .clickable(role = Role.Checkbox, onClick = onToggleFavorite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        resource = if (item.isFavorite) Res.drawable.ic_star_filled else Res.drawable.ic_star_outline
                    ),
                    contentDescription = stringResource(
                        resource = if (item.isFavorite) Res.string.remove_from_favorites else Res.string.add_to_favorites,
                        food.name
                    ),
                    tint = if (item.isFavorite) colors.carbs else colors.faint,
                    modifier = Modifier.size(size = 20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(size = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = if (isSelected) colors.accent else colors.accentSoft)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = if (isSelected) Res.drawable.ic_check else Res.drawable.ic_plus),
                    contentDescription = stringResource(
                        resource = if (isSelected) Res.string.remove_value else Res.string.add_value,
                        food.name
                    ),
                    tint = if (isSelected) colors.onAccent else colors.accentInk,
                    modifier = Modifier.size(size = 18.dp)
                )
            }
        }
        if (isExpanded && selectedItem != null) {
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            AddMealPortionEditor(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.background)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                item = selectedItem,
                onAmountChanged = onAmountChanged
            )
        }
    }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier = drawWithContent {
    drawContent()
    val stroke = 1.5.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(x = stroke / 2, y = stroke / 2),
        size = Size(width = size.width - stroke, height = size.height - stroke),
        cornerRadius = CornerRadius(x = cornerRadius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx())))
    )
}

@Composable
private fun LastLogged.relativeDay(): String =
    when (val days = date.daysUntil(other = getCurrentDate())) {
        0 -> stringResource(resource = Res.string.today)
        1 -> stringResource(resource = Res.string.yesterday)
        else -> stringResource(resource = Res.string.days_ago, days)
    }

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealListItem() {
    MacroTrackTheme {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            AddMealListItem(
                modifier = Modifier.fillMaxWidth(),
                item = MealListItem(food = dummyFood, isRecipe = false),
                selectedItem = null,
                isExpanded = false,
                showLastLogged = false,
                onToggle = {},
                onToggleFavorite = {},
                onClick = {},
                onLongPress = {},
                onAmountChanged = { _, _ -> }
            )
            AddMealListItem(
                modifier = Modifier.fillMaxWidth(),
                item = MealListItem(food = dummyFood.copy(name = "Chicken rice bowl"), isRecipe = true),
                selectedItem = SelectedMealItem(
                    key = "recipe:Chicken rice bowl",
                    isRecipe = true,
                    baseFood = dummyFood.copy(name = "Chicken rice bowl"),
                    amount = 100.0,
                    food = dummyFood.copy(name = "Chicken rice bowl")
                ),
                isExpanded = true,
                showLastLogged = false,
                onToggle = {},
                onToggleFavorite = {},
                onClick = {},
                onLongPress = {},
                onAmountChanged = { _, _ -> }
            )
        }
    }
}
