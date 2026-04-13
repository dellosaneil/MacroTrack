package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileCallbacks
import org.thelazybattley.macrotrack.features.profile.ProfileViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileViewState
import org.thelazybattley.macrotrack.ui.common.CommonSurface
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel
    )
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewState: ProfileViewState,
    callbacks: ProfileCallbacks
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        item {
            ProfileCurrentGoal(
                modifier = Modifier.fillMaxWidth(),
                currentGoal = viewState.currentGoal
            )
        }
        item {
            CommonSurface {
                ProfileBMI(
                    modifier = Modifier.fillMaxWidth(),
                    profileBMI = viewState.profileBMI
                )
            }
        }
        item {
            CommonSurface {
                ProfileWeightCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xfffffff)
@Composable
private fun PreviewProfileScreen() {
    MacroTrackTheme {
        ProfileScreen(
            modifier = Modifier.fillMaxSize(),
            viewState = ProfileViewState(),
            callbacks = ProfileCallbacks.default()
        )
    }
}
