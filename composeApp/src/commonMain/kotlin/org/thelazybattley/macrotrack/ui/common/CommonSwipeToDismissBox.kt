package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_trash
import org.jetbrains.compose.resources.painterResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun CommonSwipeToDismissBox(
    modifier: Modifier = Modifier,
    onSwipeToDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold,
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeToDismiss()
            }
            value != SwipeToDismissBoxValue.EndToStart
        }
    )
    SwipeToDismissBox(
        modifier = modifier,
        state = swipeToDismissBoxState,
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> {}
                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_trash),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = colors.lightRed)
                            .wrapContentSize(align = Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = colors.crimsonRed
                    )
                }
                SwipeToDismissBoxValue.Settled -> {}
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        content()
    }
}
