package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_name_label
import macrotrack.composeapp.generated.resources.activity_name_count
import macrotrack.composeapp.generated.resources.activity_name_hint_blank
import macrotrack.composeapp.generated.resources.activity_name_hint_named
import macrotrack.composeapp.generated.resources.activity_name_placeholder_run
import macrotrack.composeapp.generated.resources.activity_name_placeholder_walk
import macrotrack.composeapp.generated.resources.activity_names_used
import macrotrack.composeapp.generated.resources.activity_recorded_as
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.clear_name
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_info
import macrotrack.composeapp.generated.resources.ic_run
import macrotrack.composeapp.generated.resources.ic_walk
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.ACTIVITY_NAME_MAX_LENGTH
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Back button, a small line above the title, the title, and room on the right. */
@Composable
internal fun ActivityHeader(
    modifier: Modifier = Modifier,
    overline: String,
    title: String,
    onBack: () -> Unit,
    titleLeading: (@Composable () -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        SquareButton(icon = Res.drawable.ic_chevron_left, description = stringResource(resource = Res.string.back), onClick = onBack)
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = overline, style = typography.medium13, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                titleLeading?.invoke()
                Text(
                    text = title,
                    style = typography.bold24,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                )
                titleTrailing?.invoke()
            }
        }
        trailing?.invoke()
    }
}

/** A 44 dp outlined square with one icon. */
@Composable
internal fun SquareButton(
    modifier: Modifier = Modifier,
    icon: org.jetbrains.compose.resources.DrawableResource,
    description: String,
    size: Dp = 44.dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Box(
        modifier = modifier
            .size(size = size)
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
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

/** The walk or run figure in its colour: green for walks, violet for runs. */
@Composable
internal fun ActivityTypeBadge(
    modifier: Modifier = Modifier,
    type: ActivityType,
    size: Dp = 22.dp,
    iconSize: Dp = 13.dp
) {
    val (background, tint) = type.badgeColors()
    Box(
        modifier = modifier
            .size(size = size)
            .clip(shape = RoundedCornerShape(size = size * 0.32f))
            .background(color = background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = type.icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size = iconSize)
        )
    }
}

internal val ActivityType.icon get() = if (this == ActivityType.RUN) Res.drawable.ic_run else Res.drawable.ic_walk

@Composable
internal fun ActivityType.badgeColors(): Pair<Color, Color> =
    if (this == ActivityType.RUN) colors.nightSoft to colors.nightInk else colors.accentSoft to colors.accentInk

/** A small card with a label over a number and its unit; locked ones show the PRO tag and open the paywall. */
@Composable
internal fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String = "",
    isLocked: Boolean = false,
    onLockedClick: () -> Unit = {},
    background: Color = colors.surface,
    showBorder: Boolean = true
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = background)
            .then(if (showBorder) Modifier.border(width = 1.dp, color = colors.line, shape = shape) else Modifier)
            .then(if (isLocked) Modifier.clickable(role = Role.Button, onClick = onLockedClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(text = label, style = typography.medium11, color = colors.muted, modifier = Modifier.weight(weight = 1f, fill = false), maxLines = 1)
            if (isLocked) ProBadge()
        }
        if (isLocked) {
            Text(text = "– – –", style = typography.bold16, color = colors.faint)
        } else {
            ValueWithUnit(value = value, unit = unit)
        }
    }
}

/** A bold number with its unit small beside it. */
@Composable
internal fun ValueWithUnit(
    modifier: Modifier = Modifier,
    value: String,
    unit: String,
    valueStyle: androidx.compose.ui.text.TextStyle = typography.bold16,
    valueColor: Color = colors.ink
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
        Text(text = value, style = valueStyle, color = valueColor, maxLines = 1, modifier = Modifier.alignByBaseline())
        if (unit.isNotEmpty()) {
            Text(text = unit, style = typography.regular11, color = colors.muted, maxLines = 1, modifier = Modifier.alignByBaseline())
        }
    }
}

/** The design's on/off switch: 52 × 32, accent when on. */
@Composable
internal fun ActivitySwitch(isOn: Boolean, description: String, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(shape = CircleShape)
            .background(color = if (isOn) colors.accent else colors.line)
            .toggleable(value = isOn, role = Role.Switch, onValueChange = { onToggle() })
            .semantics { contentDescription = description }
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (isOn) 23.dp else 3.dp, y = 3.dp)
                .size(size = 26.dp)
                .shadow(elevation = 1.dp, shape = CircleShape)
                .background(color = if (isOn) colors.onAccent else colors.surface, shape = CircleShape)
        )
    }
}

/** A setting with a title, a line about it and a switch. */
@Composable
internal fun SwitchRow(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    isOn: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = body, style = typography.regular11, color = colors.muted)
        }
        ActivitySwitch(isOn = isOn, description = title, onToggle = onToggle)
    }
}

/** Options side by side on a grey track; the picked one sits on a white chip. */
@Composable
internal fun <T> SegmentedTabs(
    modifier: Modifier = Modifier,
    options: List<Pair<T, String>>,
    selected: T,
    height: Dp = 34.dp,
    fillWidth: Boolean = false,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            .padding(all = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .then(if (fillWidth) Modifier.weight(weight = 1f) else Modifier)
                    .height(height = height)
                    .clip(shape = RoundedCornerShape(size = 9.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(value) })
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, style = typography.bold12, color = if (isSelected) colors.ink else colors.muted, maxLines = 1)
            }
        }
    }
}

/** A pill-shaped choice; the picked one is filled. */
@Composable
internal fun ChoiceChip(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    isSuggested: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 18.dp)
    val border = when {
        isSelected || isSuggested -> colors.accent
        else -> colors.line
    }
    val background = when {
        isSelected -> colors.accent
        isSuggested -> colors.accentSoft
        else -> colors.surface
    }
    val content = when {
        isSelected -> colors.onAccent
        isSuggested -> colors.accentInk
        else -> colors.ink
    }
    Row(
        modifier = modifier
            .height(height = 36.dp)
            .clip(shape = shape)
            .background(color = background)
            .border(width = 1.dp, color = border, shape = shape)
            .selectable(selected = isSelected, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        leading?.invoke()
        Text(text = label, style = typography.bold13, color = content, maxLines = 1)
    }
}

/** A progress ring: [progress] of the way round, starting at the top. */
@Composable
internal fun ProgressRing(
    modifier: Modifier = Modifier,
    progress: Float,
    strokeWidth: Dp,
    track: Color = colors.heroTrack,
    fill: Color = colors.accent
) {
    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2
        val arcSize = Size(width = size.width - stroke, height = size.height - stroke)
        drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = Offset(x = inset, y = inset), size = arcSize, style = Stroke(width = stroke))
        if (progress > 0f) {
            drawArc(
                color = fill,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(minimumValue = 0f, maximumValue = 1f),
                useCenter = false,
                topLeft = Offset(x = inset, y = inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

/** A quiet note with an info icon, for how something works. */
@Composable
internal fun InfoNote(modifier: Modifier = Modifier, text: String) {
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_info),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 18.dp)
        )
        Text(text = text, style = typography.regular11, color = colors.muted)
    }
}

/** A white card with the design's border and rounding. */
@Composable
internal fun ActivityCard(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(size = 20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = padding),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        content()
    }
}

/** A section title in the design's bold face. */
@Composable
internal fun SectionTitle(modifier: Modifier = Modifier, text: String) {
    Text(modifier = modifier, text = text, style = typography.bold18, color = colors.ink)
}

/**
 * Naming a walk or run: as it's saved, or later from its summary. A blank name saves as the type and date,
 * shown in [fallbackName]. [onTypeChange] is null where the type can't be changed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ActivityNameSheet(
    title: String,
    summary: String,
    name: String,
    type: ActivityType,
    fallbackName: String,
    usedNames: List<String>,
    onNameChange: (String) -> Unit,
    onTypeChange: ((ActivityType) -> Unit)?,
    onDismiss: () -> Unit,
    actions: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = title, style = typography.bold18, color = colors.ink)
                    Text(text = summary, style = typography.regular11, color = colors.muted)
                }
                SquareButton(icon = Res.drawable.ic_close, description = stringResource(resource = Res.string.close), size = 36.dp, onClick = onDismiss)
            }
            val trimmed = name.trim()
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = stringResource(resource = Res.string.activity_name_label), style = typography.bold12, color = colors.muted)
                val shape = RoundedCornerShape(size = 14.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 52.dp)
                        .clip(shape = shape)
                        .border(width = 1.5.dp, color = colors.accent, shape = shape)
                        .padding(start = 14.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(weight = 1f)) {
                        if (name.isEmpty()) {
                            Text(
                                text = stringResource(
                                    resource = if (type == ActivityType.RUN) Res.string.activity_name_placeholder_run else Res.string.activity_name_placeholder_walk
                                ),
                                style = typography.bold16,
                                color = colors.faint
                            )
                        }
                        BasicTextField(
                            value = name,
                            onValueChange = { onNameChange(it.take(n = ACTIVITY_NAME_MAX_LENGTH)) },
                            singleLine = true,
                            textStyle = typography.bold16.copy(color = colors.ink),
                            cursorBrush = SolidColor(value = colors.accent),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (trimmed.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(size = 40.dp)
                                .clip(shape = RoundedCornerShape(size = 10.dp))
                                .clickable(role = Role.Button) { onNameChange("") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(resource = Res.drawable.ic_close),
                                contentDescription = stringResource(resource = Res.string.clear_name),
                                tint = colors.muted,
                                modifier = Modifier.size(size = 16.dp)
                            )
                        }
                    }
                }
                Row {
                    Text(
                        text = if (trimmed.isEmpty()) {
                            stringResource(resource = Res.string.activity_name_hint_blank, fallbackName)
                        } else {
                            stringResource(resource = Res.string.activity_name_hint_named)
                        },
                        style = typography.regular11,
                        color = colors.muted,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(
                        text = stringResource(resource = Res.string.activity_name_count, name.length, ACTIVITY_NAME_MAX_LENGTH),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
            if (usedNames.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(text = stringResource(resource = Res.string.activity_names_used), style = typography.bold12, color = colors.muted)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 6.dp), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                        usedNames.forEach { used ->
                            NameChip(label = used, isSelected = trimmed == used, onClick = { onNameChange(used) })
                        }
                    }
                }
            }
            onTypeChange?.let { onChange ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                    Text(
                        text = stringResource(resource = Res.string.activity_recorded_as),
                        style = typography.bold13,
                        color = colors.ink,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    SegmentedTabs(
                        options = ActivityType.entries.map { it to stringResource(resource = it.title) },
                        selected = type,
                        height = 32.dp,
                        onSelect = onChange
                    )
                }
            }
            actions()
        }
    }
}

@Composable
private fun NameChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    Box(
        modifier = Modifier
            .height(height = 36.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .selectable(selected = isSelected, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = typography.bold13, color = if (isSelected) colors.accentInk else colors.ink, maxLines = 1)
    }
}

/** A full-width button in the sheet's style: accent, quiet grey, or grey with red text for discarding. */
@Composable
internal fun SheetActionButton(
    modifier: Modifier = Modifier,
    text: String,
    style: SheetButtonStyle,
    onClick: () -> Unit
) {
    val (background, content) = when (style) {
        SheetButtonStyle.PRIMARY -> colors.accent to colors.onAccent
        SheetButtonStyle.QUIET -> colors.divider to colors.ink
        SheetButtonStyle.DESTRUCTIVE -> colors.divider to colors.danger
        SheetButtonStyle.TEXT -> Color.Transparent to colors.accentInk
    }
    Box(
        modifier = modifier
            .height(height = if (style == SheetButtonStyle.TEXT) 44.dp else 54.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = background)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold15, color = content, maxLines = 1)
    }
}

enum class SheetButtonStyle { PRIMARY, QUIET, DESTRUCTIVE, TEXT }

/** A thin vertical bar for small charts, [fraction] of [maxHeight] tall. */
@Composable
internal fun ChartBar(
    modifier: Modifier = Modifier,
    fraction: Float,
    maxHeight: Dp,
    color: Color,
    minHeight: Dp = 3.dp,
    cornerRadius: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .height(height = maxOf(minHeight, maxHeight * fraction.coerceIn(minimumValue = 0f, maximumValue = 1f)))
            .clip(shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            .background(color = color)
    )
}
