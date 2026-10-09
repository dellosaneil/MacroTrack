package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Single-line field in the redesign style: surface fill, line border, accent border when [isHighlighted]. */
@Composable
fun CommonInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle = typography.regular13,
    isHighlighted: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    cornerRadius: Dp = 14.dp,
    horizontalPadding: Dp = 14.dp,
    showBorder: Boolean = true
) {
    val shape = RoundedCornerShape(size = cornerRadius)
    val borderColor = when {
        isError -> colors.fat
        isHighlighted -> colors.accent
        else -> colors.line
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle.copy(color = if (enabled) colors.ink else colors.muted, textAlign = textAlign),
        cursorBrush = SolidColor(value = colors.accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .clip(shape = shape)
            .background(color = if (enabled) colors.surface else colors.divider)
            .then(
                if (showBorder) {
                    Modifier.border(width = if (isHighlighted || isError) 1.5.dp else 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                }
            ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
                contentAlignment = if (textAlign == TextAlign.End) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(textAlign = textAlign),
                        color = colors.faint
                    )
                }
                innerTextField()
            }
        }
    )
}
