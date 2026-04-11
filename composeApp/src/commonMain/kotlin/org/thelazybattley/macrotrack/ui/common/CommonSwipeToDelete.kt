package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_trash
import org.jetbrains.compose.resources.painterResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import kotlin.math.roundToInt

@Composable
fun CommonSwipeToDelete(
    modifier: Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(value = 0f) }
    var iconWidth by remember { mutableFloatStateOf(value = 0f) }
    var contentHeight by remember { mutableFloatStateOf(value = 0f) }
    var iconHeightDp by remember { mutableStateOf(value = 0.dp) }
    val localDensity = LocalDensity.current
    LaunchedEffect(key1 = contentHeight) {
        with(receiver = localDensity) {
            iconHeightDp = contentHeight.toDp()
        }
    }
    Box(
        modifier = modifier
            .clipToBounds()
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_trash),
            contentDescription = null,
            modifier = Modifier
                .background(color = colors.lightRed)
                .align(alignment = Alignment.CenterEnd)
                .height(height = iconHeightDp)
                .onGloballyPositioned {
                    iconWidth = it.size.width.toFloat()
                }
                .clickable(enabled = offsetX == -iconWidth) {
                    onDelete()
                }
                .padding(12.dp),
            tint = colors.crimsonRed
        )
        Box(
            modifier = Modifier.onGloballyPositioned {
                contentHeight = it.size.height.toFloat()
            }
                .offset { IntOffset(x = offsetX.roundToInt(), y = 0) }
                .pointerInput(key1 = Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount)
                                .coerceIn(-iconWidth, 0f)
                        },
                        onDragEnd = {
                            offsetX = if (offsetX < -iconWidth / 2) {
                                -iconWidth
                            } else {
                                0f
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun PreviewCommonSwipeToDelete() {
    MacroTrackTheme {
        CommonSwipeToDelete(
            modifier = Modifier.statusBarsPadding(),
            onDelete = {

            },
        ) {
            Spacer(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .background(color = colors.deepBlue)
                    .height(height = 98.dp)
                    .fillMaxWidth()
            )
        }
    }
}
