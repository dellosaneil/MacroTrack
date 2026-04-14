package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_checkmark
import macrotrack.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CommonSnackBarContent(
    modifier: Modifier = Modifier,
    text: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.lightAqua,
            contentColor = colors.mossGreen
        ),
        shape = RoundedCornerShape(size = 8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colors.lightGreen
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(all = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Box(
                modifier = Modifier.clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = colors.freshGreen)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_checkmark),
                    contentDescription = null,
                    modifier = Modifier.padding(all = 8.dp),
                    tint = colors.white
                )
            }
            Text(
                text = text,
                style = typography.bold12,
            )
            if(onClick != null) {
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(
                    text = stringResource(resource = Res.string.undo),
                    style = typography.bold12,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .clickable {
                            onClick()
                        }
                        .background(color = colors.lightGreen)
                        .padding(all = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCommonSnackBarContent() {
    MacroTrackTheme {
        CommonSnackBarContent(
            modifier = Modifier.fillMaxWidth(),
            text = "SnackBar"
        )
    }
}
