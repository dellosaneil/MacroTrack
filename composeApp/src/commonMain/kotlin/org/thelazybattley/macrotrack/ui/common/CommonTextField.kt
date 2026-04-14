package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.search_food
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CommonTextField(
    modifier: Modifier = Modifier,
    placeholder: String,
    textValue: String,
    prefixIcon: DrawableResource? = null,
    borderColor: Color = colors.deepBlue,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = colors.white,
        unfocusedContainerColor = colors.white,
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        errorContainerColor = colors.whiteSmoke,
        errorIndicatorColor = Color.Transparent,
        cursorColor = colors.deepBlue,
        disabledContainerColor = colors.iceMist,
        focusedTextColor = colors.black,
        unfocusedTextColor = colors.black,
    ),
    isEnabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    clearActionAllowed: Boolean = true,
    textStyle: TextStyle = typography.regular13,
    onValueChanged: (String) -> Unit,
) {
    val updatedBorderColor = if (isError) {
        colors.crimsonRed
    } else {
        borderColor
    }

    TextField(
        value = textValue,
        textStyle = textStyle,
        onValueChange = { newValue ->
            onValueChanged(newValue)
        },
        modifier = modifier
            .border(
                border = BorderStroke(width = 1.dp, color = updatedBorderColor),
                shape = RoundedCornerShape(size = 12.dp)
            ),
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle,
                color = colors.gray
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(size = 12.dp),
        colors = textFieldColors,
        prefix = {
            if (prefixIcon != null) {
                Icon(
                    painter = painterResource(resource = prefixIcon),
                    contentDescription = null,
                    tint = colors.gray,
                    modifier = Modifier.size(size = 16.dp)
                )
            }
        },
        enabled = isEnabled,
        isError = isError,
        keyboardOptions = keyboardOptions,
        suffix = {
            if (isEnabled && textValue.isNotEmpty() && clearActionAllowed) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = null,
                    modifier = Modifier.size(size = 16.dp).clickable {
                        onValueChanged("")
                    }
                )
            }
        }
    )
}

@Preview
@Composable
private fun PreviewCommonTextField() {
    MacroTrackTheme {
        CommonTextField(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            placeholder = stringResource(resource = Res.string.search_food),
            prefixIcon = Res.drawable.ic_search,
            textValue = "",
        ) {

        }
    }
}
