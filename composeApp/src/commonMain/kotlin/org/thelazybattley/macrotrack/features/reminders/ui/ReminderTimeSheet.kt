package org.thelazybattley.macrotrack.features.reminders.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.ic_chevron_down
import macrotrack.composeapp.generated.resources.ic_chevron_up
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.reminders_am
import macrotrack.composeapp.generated.resources.reminders_picker_habit
import macrotrack.composeapp.generated.resources.reminders_picker_hour_down
import macrotrack.composeapp.generated.resources.reminders_picker_hour_up
import macrotrack.composeapp.generated.resources.reminders_picker_meal_sub
import macrotrack.composeapp.generated.resources.reminders_picker_meal_title
import macrotrack.composeapp.generated.resources.reminders_picker_minute_down
import macrotrack.composeapp.generated.resources.reminders_picker_minute_up
import macrotrack.composeapp.generated.resources.reminders_picker_no_habit
import macrotrack.composeapp.generated.resources.reminders_picker_snack
import macrotrack.composeapp.generated.resources.reminders_picker_suggested
import macrotrack.composeapp.generated.resources.reminders_picker_weight_habit
import macrotrack.composeapp.generated.resources.reminders_picker_weight_sub
import macrotrack.composeapp.generated.resources.reminders_picker_weight_title
import macrotrack.composeapp.generated.resources.reminders_pm
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.LOG_REMINDER_STEP_MINUTES
import org.thelazybattley.macrotrack.domain.model.LogReminderKind
import org.thelazybattley.macrotrack.domain.model.MINUTES_PER_DAY
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.suggestedReminderMinutes
import org.thelazybattley.macrotrack.features.photos.ui.formatReminderTime
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/**
 * Picks when [kind]'s reminder goes out, in [LOG_REMINDER_STEP_MINUTES]-minute steps, with times about an hour
 * after [usualMinutes] (when it usually gets logged) offered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderTimeSheet(
    kind: LogReminderKind,
    minutes: Int,
    usualMinutes: Int?,
    onDismiss: () -> Unit,
    onDone: (Int) -> Unit
) {
    var draft by rememberSaveable { mutableIntStateOf(value = minutes) }
    val setDraft: (Int) -> Unit = { draft = (it % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY }
    val mealType = kind.mealType
    val mealName = mealType?.let { stringResource(resource = it.title).lowercase() }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(
                        text = if (mealName != null) {
                            stringResource(resource = Res.string.reminders_picker_meal_title, mealName)
                        } else {
                            stringResource(resource = Res.string.reminders_picker_weight_title)
                        },
                        style = typography.bold18,
                        color = colors.ink
                    )
                    Text(
                        text = if (mealName != null) {
                            stringResource(resource = Res.string.reminders_picker_meal_sub, mealName)
                        } else {
                            stringResource(resource = Res.string.reminders_picker_weight_sub)
                        },
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
                StepButton(
                    icon = Res.drawable.ic_close,
                    description = stringResource(resource = Res.string.close),
                    width = 34,
                    height = 34,
                    isOutlined = true,
                    onClick = onDismiss
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 18.dp))
                    .background(color = colors.hero)
                    .padding(all = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp, alignment = Alignment.CenterHorizontally)
            ) {
                val hour = draft / 60
                Stepper(
                    value = ((hour + 11) % 12 + 1).toString(),
                    upDescription = stringResource(resource = Res.string.reminders_picker_hour_up),
                    downDescription = stringResource(resource = Res.string.reminders_picker_hour_down),
                    onUp = { setDraft(draft + 60) },
                    onDown = { setDraft(draft - 60) }
                )
                Text(text = ":", style = typography.bold36, color = colors.ink)
                Stepper(
                    value = (draft % 60).toString().padStart(length = 2, padChar = '0'),
                    upDescription = stringResource(resource = Res.string.reminders_picker_minute_up),
                    downDescription = stringResource(resource = Res.string.reminders_picker_minute_down),
                    onUp = { setDraft(draft + LOG_REMINDER_STEP_MINUTES) },
                    onDown = { setDraft(draft - LOG_REMINDER_STEP_MINUTES) }
                )
                Column(modifier = Modifier.padding(start = 6.dp), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                    val isPm = hour >= 12
                    listOf(false to Res.string.reminders_am, true to Res.string.reminders_pm).forEach { (pm, label) ->
                        val isPicked = isPm == pm
                        Box(
                            modifier = Modifier
                                .size(width = 56.dp, height = 40.dp)
                                .clip(shape = RoundedCornerShape(size = 10.dp))
                                .background(color = if (isPicked) colors.accent else colors.surface)
                                .clickable(role = Role.RadioButton) { if (!isPicked) setDraft(draft + if (pm) 720 else -720) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(resource = label), style = typography.bold13, color = if (isPicked) colors.onAccent else colors.muted)
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(text = stringResource(resource = Res.string.reminders_picker_suggested), style = typography.bold12, color = colors.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    suggestedReminderMinutes(kind = kind, usualMinutes = usualMinutes).forEach { suggestion ->
                        val isPicked = suggestion == draft
                        Box(
                            modifier = Modifier
                                .height(height = 36.dp)
                                .clip(shape = RoundedCornerShape(size = 18.dp))
                                .background(color = if (isPicked) colors.accentSoft else colors.surface)
                                .border(width = 1.dp, color = if (isPicked) colors.accent else colors.line, shape = RoundedCornerShape(size = 18.dp))
                                .clickable { setDraft(suggestion) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatReminderTime(minutes = suggestion),
                                style = typography.bold13,
                                color = if (isPicked) colors.accentInk else colors.ink
                            )
                        }
                    }
                }
                Text(
                    text = when {
                        mealName == null -> stringResource(resource = Res.string.reminders_picker_weight_habit)
                        usualMinutes != null -> stringResource(resource = Res.string.reminders_picker_habit, mealName, formatReminderTime(minutes = usualMinutes))
                        mealType == MealType.SNACK -> stringResource(resource = Res.string.reminders_picker_snack)
                        else -> stringResource(resource = Res.string.reminders_picker_no_habit, mealName)
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            CommonPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = Res.string.done,
                onClick = { onDone(draft) }
            )
        }
    }
}

@Composable
private fun Stepper(value: String, upDescription: String, downDescription: String, onUp: () -> Unit, onDown: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        StepButton(icon = Res.drawable.ic_chevron_up, description = upDescription, onClick = onUp)
        Text(text = value, style = typography.bold36, color = colors.ink)
        StepButton(icon = Res.drawable.ic_chevron_down, description = downDescription, onClick = onDown)
    }
}

@Composable
private fun StepButton(
    icon: DrawableResource,
    description: String,
    width: Int = 64,
    height: Int = 36,
    isOutlined: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = width.dp, height = height.dp)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = colors.surface)
            .then(if (isOutlined) Modifier.border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 10.dp)) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 16.dp))
    }
}
