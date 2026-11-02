package org.thelazybattley.macrotrack.features.checklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.checklist_after_refill
import macrotrack.composeapp.generated.resources.checklist_bottle_size
import macrotrack.composeapp.generated.resources.checklist_close
import macrotrack.composeapp.generated.resources.checklist_lasts
import macrotrack.composeapp.generated.resources.checklist_left
import macrotrack.composeapp.generated.resources.checklist_minutes
import macrotrack.composeapp.generated.resources.checklist_new_bottle
import macrotrack.composeapp.generated.resources.checklist_part_afternoon
import macrotrack.composeapp.generated.resources.checklist_part_any_time
import macrotrack.composeapp.generated.resources.checklist_part_evening
import macrotrack.composeapp.generated.resources.checklist_part_morning
import macrotrack.composeapp.generated.resources.checklist_refill_add
import macrotrack.composeapp.generated.resources.checklist_refill_sub
import macrotrack.composeapp.generated.resources.checklist_refill_title
import macrotrack.composeapp.generated.resources.checklist_reminder_time
import macrotrack.composeapp.generated.resources.checklist_time_any_time
import macrotrack.composeapp.generated.resources.checklist_time_bedtime
import macrotrack.composeapp.generated.resources.checklist_time_breakfast
import macrotrack.composeapp.generated.resources.checklist_time_dinner
import macrotrack.composeapp.generated.resources.checklist_time_evening
import macrotrack.composeapp.generated.resources.checklist_time_lunch
import macrotrack.composeapp.generated.resources.checklist_time_morning
import macrotrack.composeapp.generated.resources.checklist_times
import macrotrack.composeapp.generated.resources.checklist_warn_aria
import macrotrack.composeapp.generated.resources.checklist_warn_field
import macrotrack.composeapp.generated.resources.checklist_warn_low
import macrotrack.composeapp.generated.resources.checklist_warn_low_body
import macrotrack.composeapp.generated.resources.checklist_warn_value
import macrotrack.composeapp.generated.resources.checklist_about_days
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.ic_book
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_droplet
import macrotrack.composeapp.generated.resources.ic_fish
import macrotrack.composeapp.generated.resources.ic_leaf
import macrotrack.composeapp.generated.resources.ic_moon
import macrotrack.composeapp.generated.resources.ic_pill
import macrotrack.composeapp.generated.resources.ic_run
import macrotrack.composeapp.generated.resources.ic_sun
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_REFILL_SIZES
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_STOCK_MAX
import org.thelazybattley.macrotrack.domain.model.ChecklistIcon
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.domain.model.DayPart
import org.thelazybattley.macrotrack.domain.model.HabitGoal
import org.thelazybattley.macrotrack.domain.model.SupplementUnit
import org.thelazybattley.macrotrack.domain.usecase.checklist.countPlural
import org.thelazybattley.macrotrack.domain.usecase.checklist.formatChecklistAmount
import org.thelazybattley.macrotrack.domain.usecase.checklist.label
import org.thelazybattley.macrotrack.domain.usecase.checklist.stockPlural
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.water.ui.clockText
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

val ChecklistIcon.drawable: DrawableResource
    get() = when (this) {
        ChecklistIcon.PILL -> Res.drawable.ic_pill
        ChecklistIcon.LEAF -> Res.drawable.ic_leaf
        ChecklistIcon.SUN -> Res.drawable.ic_sun
        ChecklistIcon.FISH -> Res.drawable.ic_fish
        ChecklistIcon.DROP -> Res.drawable.ic_droplet
        ChecklistIcon.RUN -> Res.drawable.ic_run
        ChecklistIcon.MOON -> Res.drawable.ic_moon
        ChecklistIcon.BOOK -> Res.drawable.ic_book
    }

/** The icon's tile and stroke colours. */
@Composable
private fun ChecklistIcon.tone(): Pair<Color, Color> = when (this) {
    ChecklistIcon.PILL, ChecklistIcon.LEAF, ChecklistIcon.RUN -> colors.accentSoft to colors.accentInk
    ChecklistIcon.SUN -> colors.warmSoft to colors.warmInk
    ChecklistIcon.FISH, ChecklistIcon.DROP -> colors.divider to colors.protein
    ChecklistIcon.MOON, ChecklistIcon.BOOK -> colors.nightSoft to colors.nightInk
}

@Composable
fun ChecklistItemIcon(icon: ChecklistIcon, modifier: Modifier = Modifier, size: Dp = 38.dp, isFaded: Boolean = false) {
    val (background, ink) = icon.tone()
    Box(
        modifier = modifier
            .size(size = size)
            .alpha(alpha = if (isFaded) 0.55f else 1f)
            .clip(shape = RoundedCornerShape(size = 11.dp))
            .background(color = background),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon.drawable), contentDescription = null, tint = ink, modifier = Modifier.size(size = size * 0.48f))
    }
}

val DayPart.label: StringResource
    get() = when (this) {
        DayPart.MORNING -> Res.string.checklist_part_morning
        DayPart.AFTERNOON -> Res.string.checklist_part_afternoon
        DayPart.EVENING -> Res.string.checklist_part_evening
        DayPart.ANY_TIME -> Res.string.checklist_part_any_time
    }

val ChecklistTime.label: StringResource
    get() = when (this) {
        ChecklistTime.MORNING -> Res.string.checklist_time_morning
        ChecklistTime.BREAKFAST -> Res.string.checklist_time_breakfast
        ChecklistTime.LUNCH -> Res.string.checklist_time_lunch
        ChecklistTime.DINNER -> Res.string.checklist_time_dinner
        ChecklistTime.EVENING -> Res.string.checklist_time_evening
        ChecklistTime.BEDTIME -> Res.string.checklist_time_bedtime
        ChecklistTime.ANY_TIME -> Res.string.checklist_time_any_time
    }

/** "2 capsules", "5 g", "10 minutes", "3 times", or empty for a habit that's just ticked. */
@Composable
fun checklistDose(item: ChecklistItem): String = when (item.kind) {
    ChecklistKind.SUPPLEMENT -> supplementDose(amount = item.amount, unit = item.unit)
    ChecklistKind.HABIT -> when (item.goal) {
        HabitGoal.TICK -> ""
        HabitGoal.MINUTES -> pluralStringResource(resource = Res.plurals.checklist_minutes, quantity = item.goalValue, item.goalValue)
        HabitGoal.TIMES -> pluralStringResource(resource = Res.plurals.checklist_times, quantity = item.goalValue, item.goalValue)
    }
}

@Composable
fun supplementDose(amount: Double, unit: SupplementUnit): String {
    val shown = formatChecklistAmount(amount = amount)
    return unit.countPlural?.let { pluralStringResource(resource = it, quantity = amount.toInt(), shown) }
        ?: stringResource(resource = Res.string.value_unit, shown, stringResource(resource = unit.label))
}

/** "6 capsules" or "6 doses" of [item]'s stock. */
@Composable
fun checklistStock(item: ChecklistItem, count: Int): String =
    pluralStringResource(resource = item.stockPlural, quantity = count, count.withThousands())

/** "2 capsules · With dinner": what and when, as on each row. */
@Composable
fun checklistDoseAndTime(item: ChecklistItem): String =
    listOf(checklistDose(item = item), stringResource(resource = item.time.label)).filter { it.isNotEmpty() }.joinToString(separator = " · ")

/** The time of day [millis] falls at, as "7:12 AM". */
@OptIn(ExperimentalTime::class)
@Composable
fun clockTextAt(millis: Long): String {
    val time = Instant.fromEpochMilliseconds(epochMilliseconds = millis).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
    return clockText(minutes = time.hour * 60 + time.minute)
}

/** The back (or, for a form, close) button, title and subtitle, with any buttons on the right. */
@Composable
fun ChecklistHeader(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    isClose: Boolean = false,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        ChecklistIconButton(
            icon = if (isClose) Res.drawable.ic_close else Res.drawable.ic_chevron_left,
            description = stringResource(resource = if (isClose) Res.string.checklist_close else Res.string.back),
            onClick = onBack
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold24, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let { Text(text = it, style = typography.regular11, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
        trailing()
    }
}

@Composable
fun ChecklistIconButton(icon: DrawableResource, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 19.dp))
    }
}

/** A white card with rows divided by lines. */
@Composable
fun ChecklistCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape),
        content = content
    )
}

@Composable
fun ChecklistDivider() {
    HorizontalDivider(thickness = 1.dp, color = colors.divider)
}

@Composable
fun ChecklistLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = typography.bold12, color = colors.muted, modifier = modifier)
}

@Composable
fun ChecklistChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp = 36.dp) {
    val shape = RoundedCornerShape(size = 18.dp)
    Box(
        modifier = modifier
            .height(height = height)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = typography.bold13, color = if (isSelected) colors.accentInk else colors.ink, maxLines = 1)
    }
}

/** Equal-width options in a grey track; the chosen one is raised. */
@Composable
fun ChecklistSegmented(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isOn = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isOn) colors.surface else Color.Transparent)
                    .selectable(selected = isOn, role = Role.Tab) { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, style = typography.bold14, color = if (isOn) colors.ink else colors.muted)
            }
        }
    }
}

/** A small bordered button showing a value, like "9:30 PM" or "60 left", that opens a way to change it. */
@Composable
fun ChecklistValueButton(text: String, description: String, onClick: () -> Unit, isEnabled: Boolean = true) {
    val shape = RoundedCornerShape(size = 10.dp)
    Box(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = colors.ink)
    }
}

/** Title and line under it, an optional value button, and a switch. */
@Composable
fun ChecklistSwitchRow(
    title: String,
    body: String,
    isOn: Boolean,
    onToggle: () -> Unit,
    value: String? = null,
    valueDescription: String = "",
    onValue: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = body, style = typography.regular11, color = colors.muted)
        }
        if (value != null && isOn) ChecklistValueButton(text = value, description = valueDescription, onClick = onValue)
        CommonSwitch(isOn = isOn, description = title, onToggle = onToggle)
    }
}

@Composable
fun ChecklistPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 54.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = if (isEnabled) colors.accent else colors.line)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold16, color = if (isEnabled) colors.onAccent else colors.muted)
    }
}

@Composable
fun ChecklistOutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = colors.ink) {
    val shape = RoundedCornerShape(size = 16.dp)
    Box(
        modifier = modifier
            .height(height = 50.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = color)
    }
}

/** The clock for a reminder time. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistTimeDialog(minutes: Int, title: StringResource = Res.string.checklist_reminder_time, onDismiss: () -> Unit, onPicked: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = minutes / 60 % 24, initialMinute = minutes % 60, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(text = stringResource(resource = title), style = typography.bold18, color = colors.ink) },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = colors.background,
                    selectorColor = colors.accent,
                    containerColor = colors.surface,
                    periodSelectorSelectedContainerColor = colors.accentSoft,
                    periodSelectorSelectedContentColor = colors.accentInk,
                    timeSelectorSelectedContainerColor = colors.accentSoft,
                    timeSelectorSelectedContentColor = colors.accentInk,
                    timeSelectorUnselectedContainerColor = colors.background,
                    timeSelectorUnselectedContentColor = colors.ink,
                    clockDialSelectedContentColor = colors.onAccent,
                    clockDialUnselectedContentColor = colors.ink
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onPicked(state.hour * 60 + state.minute) }) {
                Text(text = stringResource(resource = Res.string.done), style = typography.bold14, color = colors.accentInk)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(resource = Res.string.cancel), style = typography.bold14, color = colors.ink)
            }
        }
    )
}

/** The grabber at the top of a sheet is drawn by the sheet itself; this is a sheet's title and line under it. */
@Composable
fun ChecklistSheetTitle(title: String, subtitle: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
        Text(text = title, style = typography.bold18, color = colors.ink)
        subtitle?.takeIf { it.isNotEmpty() }?.let { Text(text = it, style = typography.regular11, color = colors.muted) }
    }
}

/** Adding a new bottle to what's left, and when to warn that it's running low. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistRefillSheet(item: ChecklistItem, onRefill: (added: Int, warnAt: Int) -> Unit, onDismiss: () -> Unit) {
    var size by rememberSaveable { mutableIntStateOf(value = 60) }
    var warnAt by rememberSaveable { mutableIntStateOf(value = item.stockWarnAt) }
    var isTypingWarnAt by rememberSaveable { mutableStateOf(value = false) }
    val left = item.stock ?: 0
    val total = (left + size).coerceAtMost(maximumValue = CHECKLIST_STOCK_MAX)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            ChecklistSheetTitle(
                title = stringResource(resource = Res.string.checklist_refill_title, item.name),
                subtitle = stringResource(
                    resource = Res.string.checklist_refill_sub,
                    checklistStock(item = item, count = left),
                    checklistDose(item = item).ifEmpty { checklistStock(item = item, count = item.perDose) }
                )
            )
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                ChecklistLabel(text = stringResource(resource = Res.string.checklist_new_bottle))
                val sizesLabel = stringResource(resource = Res.string.checklist_bottle_size)
                Row(
                    modifier = Modifier.selectableGroup().semantics { contentDescription = sizesLabel },
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    CHECKLIST_REFILL_SIZES.forEach { option ->
                        val isOn = option == size
                        val shape = RoundedCornerShape(size = 14.dp)
                        Box(
                            modifier = Modifier
                                .weight(weight = 1f)
                                .height(height = 46.dp)
                                .clip(shape = shape)
                                .background(color = if (isOn) colors.accentSoft else colors.surface)
                                .border(width = if (isOn) 2.dp else 1.dp, color = if (isOn) colors.accent else colors.line, shape = shape)
                                .selectable(selected = isOn, role = Role.RadioButton) { size = option },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = option.toString(), style = typography.bold18, color = colors.ink)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 16.dp))
                    .background(color = colors.hero)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = stringResource(resource = Res.string.checklist_after_refill), style = typography.bold12, color = colors.accentInk)
                    Text(text = checklistStock(item = item, count = total), style = typography.bold24, color = colors.ink)
                }
                val days = item.daysFor(stock = total)
                Text(
                    text = pluralStringResource(resource = Res.plurals.checklist_lasts, quantity = days, days),
                    style = typography.bold13,
                    color = colors.muted,
                    textAlign = TextAlign.End
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = stringResource(resource = Res.string.checklist_warn_low), style = typography.bold14, color = colors.ink)
                    val warnDays = item.daysFor(stock = warnAt)
                    Text(
                        text = stringResource(
                            resource = Res.string.checklist_warn_low_body,
                            warnAt,
                            pluralStringResource(resource = Res.plurals.checklist_about_days, quantity = warnDays, warnDays)
                        ),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
                ChecklistValueButton(
                    text = stringResource(resource = Res.string.checklist_warn_value, warnAt),
                    description = stringResource(resource = Res.string.checklist_warn_aria, warnAt),
                    onClick = { isTypingWarnAt = true }
                )
            }
            ChecklistPrimaryButton(
                text = stringResource(resource = Res.string.checklist_refill_add, checklistStock(item = item, count = size)),
                onClick = { onRefill(size, warnAt) }
            )
        }
    }
    if (isTypingWarnAt) {
        val label = stringResource(resource = Res.string.checklist_warn_field)
        CommonKeypadSheet(
            fields = listOf(
                KeypadField(
                    id = WARN_FIELD,
                    label = label,
                    value = warnAt.toString(),
                    wholeDigits = 3,
                    allowsZero = true,
                    min = 0.0,
                    max = 999.0,
                    unit = stringResource(resource = Res.string.checklist_left, "").trim()
                )
            ),
            openId = WARN_FIELD,
            onOpen = {},
            onCommit = { _, text ->
                text.toIntOrNull()?.let { warnAt = it }
                isTypingWarnAt = false
            },
            onDismiss = { isTypingWarnAt = false }
        )
    }
}

private const val WARN_FIELD = "checklist_warn_at"
