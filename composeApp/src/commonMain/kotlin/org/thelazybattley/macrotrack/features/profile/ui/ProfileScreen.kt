package org.thelazybattley.macrotrack.features.profile.ui

import org.thelazybattley.macrotrack.features.tour.tourTarget
import org.thelazybattley.macrotrack.features.tour.TourTarget
import androidx.compose.foundation.layout.Arrangement
import org.thelazybattley.macrotrack.ui.common.weightInputText
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
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
import macrotrack.composeapp.generated.resources.target_weight_in_unit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.features.profile.ProfileCallbacks
import org.thelazybattley.macrotrack.features.profile.ProfileViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileViewState
import org.thelazybattley.macrotrack.ui.common.CommonBottomSheet
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit,
    onReplayTour: () -> Unit = {}
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
        onLogWeight = { onNavigate(AppDestinations.Root.LogWeight.route) },
        onOpenBackup = { onNavigate(AppDestinations.Root.Backup.route) },
        onOpenDeleteData = { onNavigate(AppDestinations.Root.DeleteData.route) },
        onReplayTour = onReplayTour
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewState: ProfileViewState,
    callbacks: ProfileCallbacks,
    onLogWeight: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenDeleteData: () -> Unit = {},
    onReplayTour: () -> Unit = {}
) {
    val showTargetWeightSheet = remember { mutableStateOf(value = false) }
    var showStartDatePicker by remember { mutableStateOf(value = false) }

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
            onSetTarget = { showTargetWeightSheet.value = true },
            onStartDateClick = { showStartDatePicker = true }
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
            onTargetWeightClick = { showTargetWeightSheet.value = true },
            onUnitSystemSelected = callbacks::onUnitSystemSelected,
            onReplayTour = onReplayTour
        )
        ProfileYourData(
            modifier = Modifier
                .fillMaxWidth()
                .tourTarget(target = TourTarget.PROFILE_YOUR_DATA),
            lastBackup = viewState.lastBackup,
            isTelemetryEnabled = viewState.isTelemetryEnabled,
            onClick = onOpenBackup,
            onDeleteDataClick = onOpenDeleteData,
            onTelemetryToggled = callbacks::onTelemetryToggled
        )
    }

    if (showStartDatePicker) {
        CommonDatePickerDialog(
            selected = viewState.goalProgress?.startDate ?: getCurrentDate(),
            minDate = viewState.firstWeightDate,
            onDismiss = { showStartDatePicker = false },
            onConfirm = { date ->
                showStartDatePicker = false
                callbacks.onStartDateSelected(date = date)
            }
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
                callbacks.onSaveTargetWeight(targetKg = target)
            }
        )
    }
}

@Composable
private fun TargetWeightSheetContent(
    modifier: Modifier = Modifier,
    currentTarget: Double?,
    currentWeight: Double?,
    onSave: (targetKg: Double) -> Unit
) {
    val unitSystem = LocalUnitSystem.current
    var input by rememberSaveable(unitSystem) {
        mutableStateOf(value = currentTarget?.let { unitSystem.weightInputText(kg = it) }.orEmpty())
    }
    val typedValue = input.toDoubleOrNull()?.takeIf { it > 0 }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.target_weight_in_unit, weightUnitLabel()),
            style = typography.bold18,
            color = colors.ink
        )
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = currentWeight?.let { unitSystem.weightInputText(kg = it) }.orEmpty(),
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
            enabled = typedValue != null,
            onClick = { typedValue?.let { onSave(unitSystem.weightToKg(value = it)) } }
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
