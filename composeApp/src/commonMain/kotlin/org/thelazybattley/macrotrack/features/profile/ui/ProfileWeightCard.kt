package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_log
import macrotrack.composeapp.generated.resources.last_value
import macrotrack.composeapp.generated.resources.log_today_weight
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileWeightCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.white
        ),
        border = BorderStroke(width = 1.dp, color = colors.lightGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.log_today_weight),
                    style = typography.bold13,
                    color = colors.black
                )
                Text(
                    text = stringResource(resource = Res.string.last_value, "78.4", "Mar 16"),
                    style = typography.regular11,
                    color = colors.mediumGray
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.deepBlue,
                    contentColor = colors.white
                ),
                shape = RoundedCornerShape(size = 8.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.add_log),
                    style = typography.bold13
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewProfileWeightCard() {
    MacroTrackTheme {
        ProfileWeightCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {})
    }
}
