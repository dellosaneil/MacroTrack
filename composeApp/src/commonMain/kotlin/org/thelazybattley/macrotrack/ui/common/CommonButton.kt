package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography


@Composable
fun CommonButton(
    modifier: Modifier = Modifier,
    buttonText: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth().height(height = 46.dp),
        onClick = onClick,
        shape = RoundedCornerShape(size = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.blue,
            disabledContainerColor = colors.skyBlue,
            contentColor = colors.white,
            disabledContentColor = colors.babyBlue,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = colors.lightGray
        ),
        enabled = isEnabled
    ) {
        Text(
            text = buttonText,
            style = typography.bold15
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MacroTrackTheme {
        CommonButton(
            buttonText = "Continue"
        ) {

        }
    }
}
