package org.thelazybattley.macrotrack.features.onboarding.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.continue_text
import macrotrack.composeapp.generated.resources.step_count
import macrotrack.composeapp.generated.resources.tell_us_about_yourself
import macrotrack.composeapp.generated.resources.used_to_estimate_your_daily_energy_needs_accurately
import macrotrack.composeapp.generated.resources.we_will_calculate_daily_calorie_and_macro_targets
import macrotrack.composeapp.generated.resources.whats_your_main_goal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme

@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = koinViewModel<OnboardingViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    OnboardingScreen(
        modifier = Modifier,
        viewState = viewState,
        callbacks = viewModel,
    )
}

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography
    Scaffold(
        modifier = modifier,
        containerColor = colors.white
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Text(
                text = stringResource(
                    Res.string.step_count
                ),
                color = colors.blue,
                style = typography.bold11
            )
            Text(
                text = stringResource(resource = viewState.currentStep.titleRes),
                color = colors.black,
                style = typography.bold24
            )
            Text(
                text = stringResource(resource = viewState.currentStep.descriptionRes),
                color = colors.gray,
                style = typography.regular13
            )

            OnboardingGoalSettingScreen(
                modifier = Modifier,
                viewState = viewState,
                callbacks = callbacks
            )


            Spacer(modifier = Modifier.weight(weight = 1f))
            Button(
                modifier = Modifier.fillMaxWidth().height(height = 46.dp),
                onClick = {
                    callbacks.onContinueClicked()
                },
                shape = RoundedCornerShape(size = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.blue,
                    disabledContainerColor = colors.skyBlue,
                    contentColor = colors.white,
                    disabledContentColor = colors.babyBlue,
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = colors.lightGray
                ),
                enabled = viewState.selectedGoal != null
            ) {
                Text(
                    text = stringResource(resource = Res.string.continue_text),
                    style = typography.bold15
                )
            }
            Spacer(modifier = Modifier.height(height = 16.dp))
        }
    }
}

@Preview
@Composable
private fun PreviewOnboardingScreen() {
    MacroTrackTheme {
        OnboardingScreen(
            viewState = OnboardingViewState(),
            callbacks = OnboardingCallbacks.default()
        )
    }
}

enum class OnboardingStep(
    val titleRes: StringResource,
    val descriptionRes: StringResource,
    val stepNumber: Int
) {
    GOAL(
        titleRes = Res.string.whats_your_main_goal,
        descriptionRes = Res.string.we_will_calculate_daily_calorie_and_macro_targets,
        stepNumber = 1
    ),
    BODY_STATS(
        titleRes = Res.string.tell_us_about_yourself,
        descriptionRes = Res.string.used_to_estimate_your_daily_energy_needs_accurately,
        stepNumber = 2
    )
}
