package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.keypad_enter_number
import macrotrack.composeapp.generated.resources.keypad_max
import macrotrack.composeapp.generated.resources.keypad_min
import macrotrack.composeapp.generated.resources.keypad_type_to_replace
import macrotrack.composeapp.generated.resources.typing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.AmountInput
import org.thelazybattley.macrotrack.core.KeypadCheck
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** What the right of the readout shows while typing, like the height in the other units. */
data class KeypadPreview(val value: String, val caption: String)

/** Step chips show only when there are a few fields with short names, so they fit on one row. */
private const val MAX_STEP_CHIPS = 4
private const val MAX_STEP_LABEL_LENGTH = 8

/**
 * The keypad every number field opens in place of the phone keyboard. [fields] are the screen's number fields
 * in order, and the step chips switch between them; Done commits [openId] and closes. Closing without Done
 * keeps the field as it was. [preview] works out the readout's side note from what is typed so far, where
 * `live(id)` is the typed text for the open field and the saved text for the others.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonKeypadSheet(
    fields: List<KeypadField>,
    openId: String,
    onOpen: (String) -> Unit,
    onCommit: (id: String, text: String) -> Unit,
    onDismiss: () -> Unit,
    preview: @Composable (id: String, live: (String) -> String) -> KeypadPreview? = { _, _ -> null }
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        KeypadSheetContent(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            fields = fields,
            openId = openId,
            onOpen = onOpen,
            onCommit = onCommit,
            onDismiss = onDismiss,
            preview = preview
        )
    }
}

@Composable
private fun KeypadSheetContent(
    modifier: Modifier = Modifier,
    fields: List<KeypadField>,
    openId: String,
    onOpen: (String) -> Unit,
    onCommit: (id: String, text: String) -> Unit,
    onDismiss: () -> Unit,
    preview: @Composable (id: String, live: (String) -> String) -> KeypadPreview?
) {
    val field = fields.firstOrNull { it.id == openId } ?: return
    var input by remember(openId) { mutableStateOf(value = AmountInput(text = field.value)) }
    val live: (String) -> String = { id -> if (id == openId) input.text else fields.firstOrNull { it.id == id }?.value.orEmpty() }
    val check = field.check(text = input.text)
    val isValid = check == KeypadCheck.Valid
    fun commit(then: String?) {
        if (!isValid) return
        onCommit(field.id, input.text.removeSuffix(suffix = "."))
        if (then != null) onOpen(then) else onDismiss()
    }
    val problem = when (check) {
        is KeypadCheck.TooHigh -> stringResource(resource = Res.string.keypad_max, check.max.withUnit(unit = field.unit))
        is KeypadCheck.TooLow -> stringResource(resource = Res.string.keypad_min, check.min.withUnit(unit = field.unit))
        else -> field.emptyLabel ?: stringResource(resource = Res.string.keypad_enter_number)
    }
    val isOutOfRange = check is KeypadCheck.TooHigh || check is KeypadCheck.TooLow

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = field.label, style = typography.bold18, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (field.subtitle.isNotEmpty()) {
                    Text(text = field.subtitle, style = typography.regular11, color = colors.muted)
                }
            }
            val shape = RoundedCornerShape(size = 10.dp)
            Box(
                modifier = Modifier
                    .size(size = 36.dp)
                    .clip(shape = shape)
                    .border(width = 1.dp, color = colors.line, shape = shape)
                    .clickable(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = stringResource(resource = Res.string.close),
                    tint = colors.ink,
                    modifier = Modifier.size(size = 15.dp)
                )
            }
        }
        if (fields.size in 2..MAX_STEP_CHIPS && fields.all { it.shortLabel.length <= MAX_STEP_LABEL_LENGTH }) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                fields.forEach { step ->
                    StepChip(
                        modifier = Modifier.weight(weight = 1f),
                        field = step,
                        value = live(step.id),
                        isOpen = step.id == openId,
                        // Moving on keeps a valid amount; an unfinished one is left as it was.
                        onClick = { if (isValid) commit(then = step.id) else onOpen(step.id) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.hero)
                .padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            val shape = RoundedCornerShape(size = 12.dp)
            Column(
                modifier = Modifier
                    .widthIn(min = 132.dp, max = 220.dp)
                    .clip(shape = shape)
                    .background(color = colors.surface)
                    .border(width = 1.5.dp, color = colors.accent, shape = shape)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Row(modifier = Modifier.heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = input.text.ifEmpty { field.placeholder.ifEmpty { "0" } },
                        style = field.readoutStyle(),
                        color = when {
                            input.text.isEmpty() -> colors.faint
                            input.replaceOnType -> colors.accentInk
                            else -> colors.ink
                        },
                        maxLines = 1
                    )
                    KeypadCaret()
                    if (field.unit.isNotEmpty()) {
                        Text(text = field.unit, style = typography.bold14, color = colors.muted, modifier = Modifier.padding(start = 4.dp))
                    }
                }
                Text(
                    text = when {
                        input.replaceOnType -> stringResource(resource = Res.string.keypad_type_to_replace)
                        isOutOfRange -> problem
                        else -> stringResource(resource = Res.string.typing)
                    },
                    style = typography.regular11,
                    color = if (!input.replaceOnType && isOutOfRange) colors.fat else colors.muted
                )
            }
            preview(field.id, live)?.let { (value, caption) ->
                Column(
                    modifier = Modifier.weight(weight = 1f).padding(end = 6.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(space = 2.dp)
                ) {
                    Text(text = value, style = typography.bold18, color = colors.ink, textAlign = TextAlign.End, maxLines = 1)
                    Text(text = caption, style = typography.regular11, color = colors.muted, textAlign = TextAlign.End)
                }
            }
        }
        CommonKeypad(
            isDecimalEnabled = field.isDecimalEnabled,
            onKey = { key -> input = input.typed(key = key, rule = field.rule) }
        )
        SheetButton(
            modifier = Modifier.fillMaxWidth(),
            text = if (isValid) stringResource(resource = Res.string.done) else problem,
            enabled = isValid,
            onClick = { commit(then = null) }
        )
    }
}

@Composable
private fun StepChip(
    modifier: Modifier = Modifier,
    field: KeypadField,
    value: String,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 10.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = if (isOpen) colors.accentSoft else colors.surface)
            .border(width = 1.5.dp, color = if (isOpen) colors.accent else colors.line, shape = shape)
            .clickable(enabled = !isOpen, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(space = 1.dp)
    ) {
        Text(text = field.shortLabel, style = typography.bold11, color = if (isOpen) colors.accentInk else colors.muted, maxLines = 1)
        Text(
            text = value.ifEmpty { "–" } + if (field.unit.isNotEmpty()) " ${field.unit}" else "",
            style = typography.bold13,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SheetButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background = if (enabled) colors.accent else colors.line
    val content = if (enabled) colors.onAccent else colors.faint
    Box(
        modifier = modifier
            .height(height = 50.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold15, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Codes read digit by digit in a fixed-width face; amounts use the big number face. */
@Composable
private fun KeypadField.readoutStyle(): TextStyle =
    if (isCode) typography.bold24.copy(fontFamily = FontFamily.Monospace, fontSize = 22.sp, letterSpacing = 0.9.sp) else typography.bold32

private fun Double.withUnit(unit: String): String = toCleanString() + if (unit.isNotEmpty()) " $unit" else ""

/**
 * A number field on the screen that opens the keypad when tapped, in the same look as [CommonInputField].
 * [isOpen] marks it while its keypad is up.
 */
@Composable
fun CommonKeypadField(
    modifier: Modifier = Modifier,
    value: String,
    onClick: () -> Unit,
    placeholder: String = "",
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle = typography.regular13,
    isOpen: Boolean = false,
    isHighlighted: Boolean = false,
    isError: Boolean = false,
    background: Color = colors.surface,
    cornerRadius: Dp = 14.dp,
    horizontalPadding: Dp = 14.dp,
    showBorder: Boolean = true,
    contentDescription: String? = null
) {
    val shape = RoundedCornerShape(size = cornerRadius)
    val borderColor = when {
        isError && !isOpen -> colors.fat
        isOpen || isHighlighted -> colors.accent
        else -> colors.line
    }
    Box(
        modifier = modifier
            .clip(shape = shape)
            .background(color = if (isOpen && !showBorder) colors.accentSoft else background)
            .then(
                if (showBorder) {
                    Modifier.border(width = if (isOpen || isHighlighted || isError) 1.5.dp else 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                }
            )
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick)
            .padding(horizontal = horizontalPadding),
        contentAlignment = if (textAlign == TextAlign.End) Alignment.CenterEnd else if (textAlign == TextAlign.Center) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = value.ifEmpty { placeholder },
            style = textStyle.copy(textAlign = textAlign),
            color = when {
                value.isEmpty() -> colors.faint
                isOpen -> colors.accentInk
                else -> colors.ink
            },
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCommonKeypadSheet() {
    MacroTrackTheme {
        KeypadSheetContent(
            modifier = Modifier.background(color = colors.surface).padding(all = 16.dp),
            fields = listOf(
                KeypadField(id = "age", label = "Age", unit = "yrs", value = "29", wholeDigits = 3, min = 13.0, max = 120.0),
                KeypadField(id = "height", label = "Height", unit = "cm", value = "175", wholeDigits = 3, min = 100.0, max = 250.0),
                KeypadField(id = "weight", label = "Current weight", shortLabel = "Weight", unit = "kg", value = "76", wholeDigits = 3, decimals = 1)
            ),
            openId = "height",
            onOpen = {},
            onCommit = { _, _ -> },
            onDismiss = {},
            preview = { _, _ -> KeypadPreview(value = "5 ft 8.9 in", caption = "in feet and inches") }
        )
    }
}
