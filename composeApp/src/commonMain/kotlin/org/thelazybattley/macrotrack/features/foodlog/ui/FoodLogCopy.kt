package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.copied_to
import macrotrack.composeapp.generated.resources.copy_menu_title
import macrotrack.composeapp.generated.resources.copy_menu_today_lower
import macrotrack.composeapp.generated.resources.copy_pick_days_and_meal
import macrotrack.composeapp.generated.resources.copy_summary
import macrotrack.composeapp.generated.resources.copy_target_days
import macrotrack.composeapp.generated.resources.copy_target_today
import macrotrack.composeapp.generated.resources.copy_to_another_day
import macrotrack.composeapp.generated.resources.copy_to_earlier_day
import macrotrack.composeapp.generated.resources.copy_to_today
import macrotrack.composeapp.generated.resources.copy_to_today_hint
import macrotrack.composeapp.generated.resources.copy_whole_day
import macrotrack.composeapp.generated.resources.copy_whole_day_hint
import macrotrack.composeapp.generated.resources.ic_calendar
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_copy
import macrotrack.composeapp.generated.resources.ic_repeat
import macrotrack.composeapp.generated.resources.ic_whole_day
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.repeat_this_meal
import macrotrack.composeapp.generated.resources.repeat_this_meal_hint
import macrotrack.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.FoodLogCopy
import org.thelazybattley.macrotrack.features.foodlog.FoodLogFoodListByMealType
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/**
 * The ⋮ menu on a meal with food: copy it to today (from a past day), to other days, copy the whole day, or repeat
 * it on a schedule.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CopyMealSheet(
    meal: FoodLogFoodListByMealType,
    date: LocalDate,
    isToday: Boolean,
    dayItemCount: Int,
    onCopyToToday: () -> Unit,
    onCopyToOtherDays: () -> Unit,
    onCopyWholeDay: () -> Unit,
    onRepeat: () -> Unit,
    onDismiss: () -> Unit
) {
    val mealName = stringResource(resource = meal.mealType.title)
    val itemsText = pluralStringResource(resource = Res.plurals.items, quantity = meal.foodList.size, meal.foodList.size)
    val dayName = if (isToday) stringResource(resource = Res.string.copy_menu_today_lower) else date.toShortDayMonthDay()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                Text(
                    text = stringResource(resource = Res.string.copy_menu_title, mealName, dayName),
                    style = typography.bold18,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.copy_summary, itemsText, meal.calories.withThousands()),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            if (!isToday) {
                MenuRow(
                    icon = Res.drawable.ic_copy,
                    title = stringResource(resource = Res.string.copy_to_today),
                    subtitle = stringResource(
                        resource = Res.string.copy_to_today_hint,
                        itemsText,
                        meal.calories.withThousands(),
                        mealName.lowercase()
                    ),
                    onClick = onCopyToToday
                )
            }
            MenuRow(
                icon = Res.drawable.ic_calendar,
                title = stringResource(resource = if (isToday) Res.string.copy_to_earlier_day else Res.string.copy_to_another_day),
                subtitle = stringResource(resource = Res.string.copy_pick_days_and_meal),
                onClick = onCopyToOtherDays
            )
            MenuRow(
                icon = Res.drawable.ic_whole_day,
                title = stringResource(resource = Res.string.copy_whole_day),
                subtitle = stringResource(
                    resource = Res.string.copy_whole_day_hint,
                    pluralStringResource(resource = Res.plurals.items, quantity = dayItemCount, dayItemCount),
                    dayName
                ),
                onClick = onCopyWholeDay
            )
            MenuRow(
                icon = Res.drawable.ic_repeat,
                title = stringResource(resource = Res.string.repeat_this_meal),
                subtitle = stringResource(resource = Res.string.repeat_this_meal_hint),
                onClick = onRepeat
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .height(height = 48.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.background)
                    .clickable(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = Res.string.cancel), style = typography.bold14, color = colors.ink)
            }
        }
    }
}

@Composable
private fun MenuRow(icon: DrawableResource, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 38.dp)
                .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = colors.accentInk,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = subtitle, style = typography.regular11, color = colors.muted)
        }
    }
}

/** Shown after every copy, with Undo, until it times out or is dismissed. */
@Composable
internal fun CopiedToast(
    modifier: Modifier = Modifier,
    copy: FoodLogCopy,
    today: LocalDate,
    onUndo: () -> Unit,
    onTimeout: () -> Unit
) {
    val target = copy.targetDates.singleOrNull()?.let {
        if (it == today) stringResource(resource = Res.string.copy_target_today) else it.toShortDayMonthDay()
    } ?: stringResource(resource = Res.string.copy_target_days, copy.targetDates.size)
    UndoToast(
        modifier = modifier,
        key = copy,
        text = stringResource(
            resource = Res.string.copied_to,
            pluralStringResource(resource = Res.plurals.items, quantity = copy.itemCount, copy.itemCount),
            target
        ),
        onUndo = onUndo,
        onTimeout = onTimeout
    )
}

/** A dark toast with a check, [text] and Undo, that goes after a few seconds; a new [key] restarts the wait. */
@Composable
internal fun UndoToast(
    modifier: Modifier = Modifier,
    key: Any,
    text: String,
    onUndo: () -> Unit,
    onTimeout: () -> Unit
) {
    LaunchedEffect(key1 = key) {
        delay(timeMillis = UNDO_TOAST_MILLIS)
        onTimeout()
    }
    Row(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(size = 14.dp))
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.ink)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_check),
            contentDescription = null,
            tint = colors.background,
            modifier = Modifier.size(size = 18.dp)
        )
        Text(
            text = text,
            style = typography.bold14,
            color = colors.background,
            modifier = Modifier.weight(weight = 1f)
        )
        Box(
            modifier = Modifier
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onUndo)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(resource = Res.string.undo), style = typography.bold14, color = colors.accentSoft)
        }
    }
}

private const val UNDO_TOAST_MILLIS = 6_000L
