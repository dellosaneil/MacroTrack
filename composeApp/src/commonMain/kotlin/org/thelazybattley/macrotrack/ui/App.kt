package org.thelazybattley.macrotrack.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.thelazybattley.macrotrack.ui.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun App() {
    AppTheme {
        Scaffold {
            Text(
                text = "Testing",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
