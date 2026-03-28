package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.search_food
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CommonTextField(
    modifier: Modifier = Modifier,
    placeholder: StringResource,
    prefixIcon: DrawableResource? = null,
    borderColor: Color = Color.Transparent,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = colors.offWhite,
        unfocusedContainerColor = colors.offWhite,
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
    ),
    isEnabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    var value by remember { mutableStateOf(value = "") }
    TextField(
        value = value,
        onValueChange = { newValue ->
            onValueChanged(newValue)
            value = newValue
        },
        modifier = modifier
            .border(
                border = BorderStroke(width = 1.dp, color = borderColor),
                shape = RoundedCornerShape(size = 12.dp)
            ),
        placeholder = {
            Text(
                text = stringResource(resource = placeholder),
                style = typography.regular13,
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
        enabled = isEnabled
    )
}

@Preview
@Composable
private fun PreviewCommonTextField() {
    MacroTrackTheme {
        CommonTextField(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            placeholder = Res.string.search_food,
            prefixIcon = Res.drawable.ic_search
        ) {

        }
    }
}
