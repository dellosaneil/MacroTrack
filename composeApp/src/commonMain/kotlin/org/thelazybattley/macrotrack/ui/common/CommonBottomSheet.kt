package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonBottomSheet(
    modifier: Modifier = Modifier,
    bottomSheetState: SheetState,
    showBottomSheet: MutableState<Boolean>,
    content: @Composable (Modifier) -> Unit
) {
    if (showBottomSheet.value) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = {
                showBottomSheet.value = false
            },
            sheetState = bottomSheetState,
            containerColor = colors.white,
        ) {
            content(
                Modifier
                    .padding(paddingValues = AppPadding)
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewCommonBottomSheet() {
    MacroTrackTheme {
        val bottomSheetState = rememberModalBottomSheetState()
        val showBottomSheet = remember { mutableStateOf(value = true) }

        CommonBottomSheet(
            modifier = Modifier,
            bottomSheetState = bottomSheetState,
            showBottomSheet = showBottomSheet
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 108.dp)
            )

        }
    }
}
