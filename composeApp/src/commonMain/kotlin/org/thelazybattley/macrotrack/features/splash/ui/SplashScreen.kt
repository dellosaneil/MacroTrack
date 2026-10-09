package org.thelazybattley.macrotrack.features.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_app_logo
import macrotrack.composeapp.generated.resources.macrotrack
import macrotrack.composeapp.generated.resources.splash_tagline
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.splash.SplashViewModel
import org.thelazybattley.macrotrack.features.splash.SplashViewState
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onNavigation: (AppDestinations.Root) -> Unit
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
    onNavigation: (AppDestinations.Root) -> Unit
) {
    LaunchedEffect(key1 = viewState.isLoading) {
        if (!viewState.isLoading && viewState.destination != null) {
            delay(timeMillis = 250L)
            onNavigation(viewState.destination)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.background)
            .navigationBarsPadding()
            .padding(bottom = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size = 128.dp)
                    .clip(shape = RoundedCornerShape(size = 32.dp))
                    .background(color = colors.surface)
                    .border(
                        width = 1.dp,
                        color = colors.line,
                        shape = RoundedCornerShape(size = 32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(resource = Res.drawable.ic_app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(size = 96.dp)
                )
            }
            Spacer(modifier = Modifier.height(height = 22.dp))
            Text(
                text = stringResource(resource = Res.string.macrotrack),
                color = colors.ink,
                style = typography.bold36
            )
            Spacer(modifier = Modifier.height(height = 6.dp))
            Text(
                text = stringResource(resource = Res.string.splash_tagline),
                color = colors.muted,
                style = typography.regular13
            )
        }
        LinearProgressIndicator(
            modifier = Modifier
                .width(width = 120.dp)
                .height(height = 4.dp)
                .clip(shape = RoundedCornerShape(size = 2.dp)),
            color = colors.accent,
            trackColor = colors.heroTrack,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp
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
