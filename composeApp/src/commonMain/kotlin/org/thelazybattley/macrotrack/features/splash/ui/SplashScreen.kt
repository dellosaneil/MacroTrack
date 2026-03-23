package org.thelazybattley.macrotrack.features.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_splash_screen
import macrotrack.composeapp.generated.resources.macrotrack
import macrotrack.composeapp.generated.resources.track_improve_thrive
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.splash.SplashViewModel
import org.thelazybattley.macrotrack.features.splash.SplashViewState
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onNavigation: (MacroTrackDestination) -> Unit
) {
    val viewModel = koinViewModel<SplashViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    SplashScreen(
        modifier = modifier,
        viewState = viewState
    ) { destination ->
        onNavigation(destination)
    }
}

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    viewState: SplashViewState,
    onNavigation: (MacroTrackDestination) -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(colors.royalBlue, colors.deepBlueSplash, colors.brightBlue),
        start = Offset(0f,0f),
        end = Offset(x = Float.POSITIVE_INFINITY, y = Float.POSITIVE_INFINITY)
    )
    LaunchedEffect(key1 = viewState.isLoading) {
    //        if (!viewState.isLoading && viewState.destination != null) {
    //            delay(1_500)
    //            onNavigation(viewState.destination)
    //        }
    }

    Column(
        modifier = modifier.fillMaxSize()
            .background(brush = gradient,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .border(
                    width = 2.dp,
                    color = colors.whiteSplash.copy(
                        alpha = 0.22f
                    ),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .background(
                    color = colors.whiteSplash.copy(
                        alpha = 0.22f
                    )
                ),
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_splash_screen),
                contentDescription = null,
                tint = colors.whiteSplash,
                modifier = Modifier.size(size = 66.dp).padding(all = 16.dp)
            )
        }
        Text(
            text = stringResource(resource = Res.string.macrotrack),
            color = colors.whiteSplash,
            style = typography.bold36
        )
        Text(
            text = stringResource(resource = Res.string.track_improve_thrive),
            color = colors.whiteSplash.copy(
                alpha = 0.55f
            ),
            style = typography.regular13
        )


    }
}

@Preview
@Composable
private fun PreviewSplashScreen() {
    MacroTrackTheme {
        SplashScreen(viewState = SplashViewState()) {

        }
    }
}
