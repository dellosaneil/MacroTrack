package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.profile
import macrotrack.composeapp.generated.resources.save_target
import macrotrack.composeapp.generated.resources.target_weight
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.features.profile.ProfileCallbacks
import org.thelazybattley.macrotrack.features.profile.ProfileViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileViewState
import org.thelazybattley.macrotrack.ui.common.CommonBottomSheet
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.route) {
        viewState.route?.let { route ->
            onNavigate(route)
            viewModel.onResetNavigation()
        }
    }

    ProfileScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onLogWeight = { onNavigate(AppDestinations.Root.LogWeight.route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewState: ProfileViewState,
    callbacks: ProfileCallbacks,
    onLogWeight: () -> Unit = {}
) {
    val showTargetWeightSheet = remember { mutableStateOf(value = false) }

    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.profile),
            style = typography.bold32,
            color = colors.ink
        )
        ProfileGoalProgress(
            modifier = Modifier.fillMaxWidth(),
            goalProgress = viewState.goalProgress,
            onLogWeight = onLogWeight,
            onSetTarget = { showTargetWeightSheet.value = true }
        )
        viewState.weekSummary?.let { weekSummary ->
            ProfileThisWeek(
                modifier = Modifier.fillMaxWidth(),
                weekSummary = weekSummary,
                macroGoals = viewState.macroGoals
            )
        }
        ProfileAccount(
            modifier = Modifier.fillMaxWidth(),
            goal = viewState.userDetails?.goal,
            macroGoals = viewState.macroGoals,
            targetWeight = viewState.userDetails?.targetWeight,
            onNavigate = callbacks::onNavigate,
            onTargetWeightClick = { showTargetWeightSheet.value = true }
        )
    }

    CommonBottomSheet(
        bottomSheetState = rememberModalBottomSheetState(),
        showBottomSheet = showTargetWeightSheet
    ) { sheetModifier ->
        TargetWeightSheetContent(
            modifier = sheetModifier,
            currentTarget = viewState.userDetails?.targetWeight,
            currentWeight = viewState.userDetails?.weight,
            onSave = { target ->
                showTargetWeightSheet.value = false
                callbacks.onSaveTargetWeight(targetWeight = target)
            }
        )
    }
}

@Composable
private fun TargetWeightSheetContent(
    modifier: Modifier = Modifier,
    currentTarget: Double?,
    currentWeight: Double?,
    onSave: (String) -> Unit
) {
    var input by rememberSaveable { mutableStateOf(value = currentTarget?.toCleanString().orEmpty()) }
    val isValid = input.toDoubleOrNull()?.let { it > 0 } == true

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.target_weight),
            style = typography.bold18,
            color = colors.ink
        )
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = currentWeight?.toCleanString().orEmpty(),
            textValue = input,
            onValueChanged = { value ->
                if (value.length <= 5 && value.count { it == '.' } <= 1 && value.all { it.isDigit() || it == '.' }) {
                    input = value
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = typography.bold32
        )
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.save_target,
            enabled = isValid,
            onClick = { onSave(input) }
        )
    }
}

@Preview(showBackground = true)
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
