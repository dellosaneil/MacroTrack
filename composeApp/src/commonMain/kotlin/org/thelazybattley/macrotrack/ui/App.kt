package org.thelazybattley.macrotrack.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.thelazybattley.macrotrack.ui.theme.LocalColors
import org.thelazybattley.macrotrack.ui.theme.MyTheme

@Composable
@Preview
fun App() {
    MyTheme {
        Text(
            text = "Testing",
            color = LocalColors.current.black
        )
    }
}
