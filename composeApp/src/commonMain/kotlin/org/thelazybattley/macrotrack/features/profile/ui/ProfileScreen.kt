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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.keypad_enter_weight
import macrotrack.composeapp.generated.resources.keypad_from_today
import macrotrack.composeapp.generated.resources.keypad_from_today_bmi
import macrotrack.composeapp.generated.resources.profile
import macrotrack.composeapp.generated.resources.target_weight
import macrotrack.composeapp.generated.resources.target_weight_subtitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.features.profile.ProfileCallbacks
import org.thelazybattley.macrotrack.features.profile.ProfileViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileViewState
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.KeypadPreview
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private const val TARGET_FIELD = "target"
private const val MIN_TARGET_KG = 30.0
private const val MAX_TARGET_KG = 300.0

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
        onOpenStepsSettings = { onNavigate(AppDestinations.Root.StepsSettings.route) },
        onReplayTour = onReplayTour
    )
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewState: ProfileViewState,
    callbacks: ProfileCallbacks,
    onLogWeight: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenDeleteData: () -> Unit = {},
    onOpenStepsSettings: () -> Unit = {},
    onReplayTour: () -> Unit = {}
) {
    var showTargetKeypad by rememberSaveable { mutableStateOf(value = false) }
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
            onSetTarget = { showTargetKeypad = true },
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
            onTargetWeightClick = { showTargetKeypad = true },
            onUnitSystemSelected = callbacks::onUnitSystemSelected,
            onOpenStepsSettings = onOpenStepsSettings,
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

    if (showTargetKeypad) {
        TargetWeightKeypad(
            currentTarget = viewState.userDetails?.targetWeight,
            currentWeight = viewState.userDetails?.weight,
            heightCm = viewState.userDetails?.height,
            onSave = { target -> callbacks.onSaveTargetWeight(targetKg = target) },
            onDismiss = { showTargetKeypad = false }
        )
    }
}

/** Types the target on the shared keypad, one decimal in the app's weight unit. Done saves it and closes. */
@Composable
private fun TargetWeightKeypad(
    currentTarget: Double?,
    currentWeight: Double?,
    heightCm: Double?,
    onSave: (targetKg: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val unitSystem = LocalUnitSystem.current
    val unit = weightUnitLabel()
    CommonKeypadSheet(
        fields = listOf(
            KeypadField(
                id = TARGET_FIELD,
                label = stringResource(resource = Res.string.target_weight),
                subtitle = stringResource(resource = Res.string.target_weight_subtitle),
                unit = unit,
                value = currentTarget?.let { unitSystem.weightInputText(kg = it) }.orEmpty(),
                wholeDigits = 3,
                decimals = 1,
                min = unitSystem.weightFromKg(kg = MIN_TARGET_KG).roundToInt().toDouble(),
                max = unitSystem.weightFromKg(kg = MAX_TARGET_KG).roundToInt().toDouble(),
                emptyLabel = stringResource(resource = Res.string.keypad_enter_weight)
            )
        ),
        openId = TARGET_FIELD,
        onOpen = {},
        onCommit = { _, text -> text.toDoubleOrNull()?.let { onSave(unitSystem.weightToKg(value = it)) } },
        onDismiss = onDismiss,
        preview = { id, live ->
            val targetKg = live(id).toDoubleOrNull()?.takeIf { it > 0 }?.let { unitSystem.weightToKg(value = it) }
            when {
                currentWeight == null -> null
                targetKg == null -> KeypadPreview(value = "–", caption = stringResource(resource = Res.string.keypad_from_today))
                else -> {
                    val change = unitSystem.weightFromKg(kg = targetKg - currentWeight)
                    val tenths = (abs(change) * 10).roundToInt()
                    val sign = if (tenths == 0) "" else if (change < 0) "−" else "+"
                    val bmi = heightCm?.takeIf { it > 0 }?.let { targetKg / (it / 100).pow(2) }
                    KeypadPreview(
                        value = "$sign${tenths / 10}.${tenths % 10} $unit",
                        caption = bmi?.let {
                            val bmiTenths = (it * 10).roundToInt()
                            stringResource(resource = Res.string.keypad_from_today_bmi, "${bmiTenths / 10}.${bmiTenths % 10}")
                        } ?: stringResource(resource = Res.string.keypad_from_today)
                    )
                }
            }
        }
    )
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
