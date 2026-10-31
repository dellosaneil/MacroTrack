package org.thelazybattley.macrotrack.features.mealplan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.delete
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_more_vertical
import macrotrack.composeapp.generated.resources.ic_pause
import macrotrack.composeapp.generated.resources.ic_pencil
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_repeat
import macrotrack.composeapp.generated.resources.ic_trash
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.repeat_delete_body
import macrotrack.composeapp.generated.resources.repeat_delete_title
import macrotrack.composeapp.generated.resources.repeating_deleted
import macrotrack.composeapp.generated.resources.repeating_empty
import macrotrack.composeapp.generated.resources.repeating_empty_body
import macrotrack.composeapp.generated.resources.repeating_empty_hint
import macrotrack.composeapp.generated.resources.repeating_ended
import macrotrack.composeapp.generated.resources.repeating_hint
import macrotrack.composeapp.generated.resources.repeating_meals_intro
import macrotrack.composeapp.generated.resources.repeating_meals_title
import macrotrack.composeapp.generated.resources.repeating_menu_delete_hint
import macrotrack.composeapp.generated.resources.repeating_menu_edit
import macrotrack.composeapp.generated.resources.repeating_menu_edit_hint
import macrotrack.composeapp.generated.resources.repeating_menu_pause
import macrotrack.composeapp.generated.resources.repeating_menu_pause_hint
import macrotrack.composeapp.generated.resources.repeating_menu_resume
import macrotrack.composeapp.generated.resources.repeating_menu_resume_hint
import macrotrack.composeapp.generated.resources.repeating_more
import macrotrack.composeapp.generated.resources.repeating_new
import macrotrack.composeapp.generated.resources.repeating_next
import macrotrack.composeapp.generated.resources.repeating_next_today
import macrotrack.composeapp.generated.resources.repeating_next_tomorrow
import macrotrack.composeapp.generated.resources.repeating_pause
import macrotrack.composeapp.generated.resources.repeating_paused
import macrotrack.composeapp.generated.resources.repeating_resume
import macrotrack.composeapp.generated.resources.repeating_tag_auto
import macrotrack.composeapp.generated.resources.repeating_tag_plan
import macrotrack.composeapp.generated.resources.repeating_what
import macrotrack.composeapp.generated.resources.repeating_when
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal
import org.thelazybattley.macrotrack.features.foodlog.ui.UndoToast
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.mealplan.RepeatingMealsCallbacks
import org.thelazybattley.macrotrack.features.mealplan.RepeatingMealsViewModel
import org.thelazybattley.macrotrack.features.mealplan.RepeatingMealsViewState
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun RepeatingMealsScreen(
    onBack: () -> Unit,
    onOpen: (RepeatingMeal) -> Unit,
    onNew: () -> Unit,
    /** One Edit repeating meal asked to delete; it's deleted here so the list can offer Undo. */
    deleteRequestId: Long?,
    onDeleteRequestHandled: () -> Unit
) {
    val viewModel = koinViewModel<RepeatingMealsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = deleteRequestId) {
        if (deleteRequestId != null) {
            viewModel.onDelete(id = deleteRequestId)
            onDeleteRequestHandled()
        }
    }
    RepeatingMealsScreen(viewState = viewState, callbacks = viewModel, onBack = onBack, onOpen = onOpen, onNew = onNew)
}

@Composable
private fun RepeatingMealsScreen(
    viewState: RepeatingMealsViewState,
    callbacks: RepeatingMealsCallbacks,
    onBack: () -> Unit,
    onOpen: (RepeatingMeal) -> Unit,
    onNew: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PlanHeader(
                leadingIcon = Res.drawable.ic_chevron_left,
                leadingDescription = stringResource(resource = Res.string.back),
                onLeading = onBack,
                trailing = { NewIconButton(onClick = onNew) }
            ) {
                Text(
                    text = stringResource(resource = Res.string.repeating_meals_title),
                    style = typography.bold24,
                    color = colors.ink,
                    modifier = Modifier.weight(weight = 1f)
                )
            }
            if (viewState.isLoaded && viewState.repeatingMeals.isEmpty()) {
                EmptyState(onNew = onNew)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(state = rememberScrollState())
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 14.dp)
                ) {
                    Text(text = stringResource(resource = Res.string.repeating_meals_intro), style = typography.regular13, color = colors.muted)
                    viewState.repeatingMeals.forEach { meal ->
                        RepeatingMealCard(
                            meal = meal,
                            viewState = viewState,
                            onOpen = { onOpen(meal) },
                            onToggle = { callbacks.onActiveToggled(repeatingMeal = meal) },
                            onMenu = { callbacks.onMenuOpened(repeatingMeal = meal) }
                        )
                    }
                    NewRepeatingMealButton(onClick = onNew)
                    Text(text = stringResource(resource = Res.string.repeating_hint), style = typography.regular11, color = colors.muted)
                    // Keeps the last card clear of the toast.
                    if (viewState.deleted != null) Box(modifier = Modifier.height(height = 64.dp))
                }
            }
        }
        viewState.deleted?.let { deleted ->
            UndoToast(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                key = deleted,
                text = stringResource(resource = Res.string.repeating_deleted, deleted.name),
                onUndo = callbacks::onUndoDelete,
                onTimeout = callbacks::onDeletedToastDismissed
            )
        }
    }
    viewState.menuMeal?.let { meal ->
        RepeatingMealMenu(
            meal = meal,
            isConfirmingDelete = viewState.isConfirmingDelete,
            onEdit = {
                callbacks.onMenuDismissed()
                onOpen(meal)
            },
            callbacks = callbacks
        )
    }
}

@Composable
private fun NewIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.accent)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_plus),
            contentDescription = stringResource(resource = Res.string.repeating_new),
            tint = colors.onAccent,
            modifier = Modifier.size(size = 20.dp)
        )
    }
}

@Composable
private fun EmptyState(onNew: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 32.dp, end = 32.dp, top = 80.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 72.dp)
                .background(color = colors.nightSoft, shape = RoundedCornerShape(size = 24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_repeat),
                contentDescription = null,
                tint = colors.nightInk,
                modifier = Modifier.size(size = 34.dp)
            )
        }
        Text(
            text = stringResource(resource = Res.string.repeating_empty),
            style = typography.bold18,
            color = colors.ink,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = stringResource(resource = Res.string.repeating_empty_body),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(height = 52.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = colors.accent)
                .clickable(role = Role.Button, onClick = onNew)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_plus),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(size = 18.dp)
            )
            Text(text = stringResource(resource = Res.string.repeating_new), style = typography.bold14, color = colors.onAccent)
        }
        Text(
            text = stringResource(resource = Res.string.repeating_empty_hint),
            style = typography.regular11,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** The ⋮ menu on a card: Edit, Pause or Resume, and Delete, which asks first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatingMealMenu(
    meal: RepeatingMeal,
    isConfirmingDelete: Boolean,
    onEdit: () -> Unit,
    callbacks: RepeatingMealsCallbacks
) {
    ModalBottomSheet(
        onDismissRequest = callbacks::onMenuDismissed,
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
            if (isConfirmingDelete) {
                DeleteConfirm(name = meal.name, onDelete = { callbacks.onDelete(id = meal.id) }, onCancel = callbacks::onMenuDismissed)
                return@Column
            }
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 10.dp)) {
                Text(text = meal.name, style = typography.bold18, color = colors.ink)
                Text(
                    text = stringResource(resource = Res.string.repeating_when, daysLabel(days = meal.days), stringResource(resource = meal.mealType.title)) +
                        " · " + meal.calories.withThousands() + " " + stringResource(resource = Res.string.kcal_text),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            MenuRow(
                icon = Res.drawable.ic_pencil,
                iconBackground = colors.accentSoft,
                iconTint = colors.accentInk,
                title = stringResource(resource = Res.string.repeating_menu_edit),
                titleColor = colors.ink,
                subtitle = stringResource(resource = Res.string.repeating_menu_edit_hint),
                onClick = onEdit
            )
            MenuRow(
                icon = if (meal.isActive) Res.drawable.ic_pause else Res.drawable.ic_repeat,
                iconBackground = colors.nightSoft,
                iconTint = colors.nightInk,
                title = stringResource(resource = if (meal.isActive) Res.string.repeating_menu_pause else Res.string.repeating_menu_resume),
                titleColor = colors.ink,
                subtitle = stringResource(
                    resource = if (meal.isActive) Res.string.repeating_menu_pause_hint else Res.string.repeating_menu_resume_hint
                ),
                onClick = { callbacks.onActiveToggled(repeatingMeal = meal) }
            )
            MenuRow(
                icon = Res.drawable.ic_trash,
                iconBackground = colors.dangerSoft,
                iconTint = colors.danger,
                title = stringResource(resource = Res.string.delete),
                titleColor = colors.danger,
                subtitle = stringResource(resource = Res.string.repeating_menu_delete_hint),
                onClick = callbacks::onDeleteAsked
            )
            SheetButton(
                modifier = Modifier.padding(top = 6.dp),
                text = stringResource(resource = Res.string.cancel),
                onClick = callbacks::onMenuDismissed
            )
        }
    }
}

/** Asks before deleting [name]; [extra] goes between Delete and Cancel. */
@Composable
internal fun DeleteConfirm(name: String, onDelete: () -> Unit, onCancel: () -> Unit, extra: (@Composable () -> Unit)? = null) {
    Column(modifier = Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Box(
            modifier = Modifier
                .size(size = 44.dp)
                .background(color = colors.dangerSoft, shape = RoundedCornerShape(size = 14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_trash),
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(size = 20.dp)
            )
        }
        Text(text = stringResource(resource = Res.string.repeat_delete_title, name), style = typography.bold18, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.repeat_delete_body),
            style = typography.regular13,
            color = colors.muted,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 52.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = colors.danger)
                .clickable(role = Role.Button, onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(resource = Res.string.delete), style = typography.bold16, color = colors.onDanger)
        }
        extra?.invoke()
        SheetButton(text = stringResource(resource = Res.string.cancel), onClick = onCancel)
    }
}

/** A sheet's quiet full-width button, like Cancel. */
@Composable
internal fun SheetButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = colors.ink)
    }
}

@Composable
private fun MenuRow(
    icon: DrawableResource,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    titleColor: Color,
    subtitle: String,
    onClick: () -> Unit
) {
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
                .background(color = iconBackground, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = titleColor)
            Text(text = subtitle, style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
private fun RepeatingMealCard(
    meal: RepeatingMeal,
    viewState: RepeatingMealsViewState,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onMenu: () -> Unit
) {
    val shape = RoundedCornerShape(size = 18.dp)
    val today = viewState.today
    val next = meal.nextDates(from = today, count = 1).firstOrNull()
    val nextText = when {
        !meal.isActive -> stringResource(resource = Res.string.repeating_paused)
        next == null -> stringResource(resource = Res.string.repeating_ended)
        next == today -> stringResource(resource = Res.string.repeating_next_today, next.toShortDayMonthDay())
        next == today.plus(period = DatePeriod(days = 1)) ->
            stringResource(resource = Res.string.repeating_next_tomorrow, next.toShortDayMonthDay())
        else -> stringResource(resource = Res.string.repeating_next, next.toShortDayMonthDay())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .alpha(alpha = if (meal.isActive) 1f else 0.7f)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onOpen),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(text = meal.name, style = typography.bold15, color = colors.ink, modifier = Modifier.weight(weight = 1f, fill = false))
                if (meal.isAutoLog) {
                    Text(
                        text = stringResource(resource = Res.string.repeating_tag_auto),
                        style = typography.bold10,
                        color = colors.accentInk,
                        modifier = Modifier
                            .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 6.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                } else {
                    PlanTag(text = stringResource(resource = Res.string.repeating_tag_plan))
                }
            }
            Text(
                text = stringResource(resource = Res.string.repeating_when, daysLabel(days = meal.days), stringResource(resource = meal.mealType.title)),
                style = typography.regular13,
                color = colors.ink
            )
            Text(
                text = stringResource(
                    resource = Res.string.repeating_what,
                    meal.foods.joinToString(separator = ", ") { it.foodName },
                    meal.calories.withThousands()
                ),
                style = typography.regular11,
                color = colors.muted
            )
            Text(text = nextText, style = typography.bold12, color = colors.nightInk)
        }
        ActiveSwitch(
            isOn = meal.isActive,
            description = stringResource(resource = if (meal.isActive) Res.string.repeating_pause else Res.string.repeating_resume, meal.name),
            onToggle = onToggle
        )
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onMenu),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_more_vertical),
                contentDescription = stringResource(resource = Res.string.repeating_more, meal.name),
                tint = colors.muted,
                modifier = Modifier.size(size = 18.dp)
            )
        }
    }
}

/** The design's on/off switch: 50 × 30, accent when on. */
@Composable
private fun ActiveSwitch(isOn: Boolean, description: String, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 30.dp)
            .clip(shape = CircleShape)
            .background(color = if (isOn) colors.accent else colors.background)
            .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = CircleShape)
            .toggleable(value = isOn, role = Role.Switch, onValueChange = { onToggle() })
            .semantics { contentDescription = description }
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (isOn) 23.dp else 3.dp, y = 3.dp)
                .size(size = 24.dp)
                .shadow(elevation = 1.dp, shape = CircleShape)
                .background(color = if (isOn) colors.onAccent else colors.surface, shape = CircleShape)
        )
    }
}

@Composable
private fun NewRepeatingMealButton(onClick: () -> Unit) {
    val dashed = colors.dashed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 52.dp)
            .dashedBorder(color = dashed, width = 1.5.dp, cornerRadius = 16.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_plus),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.size(size = 18.dp)
        )
        Text(text = stringResource(resource = Res.string.repeating_new), style = typography.bold14, color = colors.accentInk)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewRepeatingMealsScreen() {
    val today = getCurrentDate()
    MacroTrackTheme {
        RepeatingMealsScreen(
            viewState = RepeatingMealsViewState(
                today = today,
                repeatingMeals = listOf(
                    RepeatingMeal(
                        id = 1,
                        name = "Weekday breakfast",
                        mealType = MealType.BREAKFAST,
                        days = RepeatingMeal.WEEKDAYS,
                        foods = listOf(
                            PlanFood(
                                foodName = "Greek yogurt",
                                calories = 190,
                                protein = 20.0,
                                carbs = 8.0,
                                fat = 10.0,
                                weight = 200.0,
                                dominantMacro = MacroType.PROTEIN
                            )
                        ),
                        startDate = today
                    ),
                    RepeatingMeal(
                        id = 2,
                        name = "Sunday pancakes",
                        mealType = MealType.BREAKFAST,
                        days = setOf(DayOfWeek.SUNDAY),
                        foods = emptyList(),
                        startDate = today,
                        isAutoLog = true,
                        isActive = false
                    )
                )
            ),
            callbacks = RepeatingMealsCallbacks.default(),
            onBack = {},
            onOpen = {},
            onNew = {}
        )
    }
}
