package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.goal_updated
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileCallbacks
import org.thelazybattley.macrotrack.features.profile.ProfileViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileViewState
import org.thelazybattley.macrotrack.ui.common.CommonSnackBarContent
import org.thelazybattley.macrotrack.ui.common.CommonSurface
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = Unit) {
        viewModel.clearGoalUpdated()
    }
    LaunchedEffect(key1 = viewState.updatedGoal) {
        if (viewState.updatedGoal == null) {
            snackBarHostState.currentSnackbarData?.dismiss()
        }
        viewState.updatedGoal?.let {
            snackBarHostState.showSnackbar(message = "")
        }
    }
    LaunchedEffect(key1 = viewState.route) {
        viewState.route?.let { route ->
            onNavigate(route)
            viewModel.onResetNavigation()
        }
    }

    ProfileScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel
    )

    SnackbarHost(
        hostState = snackBarHostState,
        modifier = Modifier
    ) { _ ->
        val goalTitle = viewState.updatedGoal?.let { goal ->
            stringResource(resource = goal.title)
        } ?: return@SnackbarHost

        CommonSnackBarContent(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(resource = Res.string.goal_updated, goalTitle)
        )
    }
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
            CommonSurface {
                ProfileCurrentGoal(
                    modifier = Modifier.fillMaxWidth(),
                    currentGoal = viewState.userDetails?.goal
                ) { goal ->
                    callbacks.updateGoal(goal = goal)
                }
            }
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
                    previousWeight = viewState.userDetails?.weight.toString(),
                    onSaveWeight = { weight ->
                        callbacks.onSaveWeight(weight = weight)
                    },
                    lastWeightUpdate = viewState.lastWeightUpdatedDate
                )
            }
        }
        item {
            ProfileAccount(
                modifier = Modifier.fillMaxWidth(),
                onNavigate = callbacks::onNavigate
            )
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
