package org.thelazybattley.macrotrack.features.onboarding.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_and_targets
import macrotrack.composeapp.generated.resources.choose_your_goal
import macrotrack.composeapp.generated.resources.continue_text
import macrotrack.composeapp.generated.resources.set_up_your_profile
import macrotrack.composeapp.generated.resources.step_count
import macrotrack.composeapp.generated.resources.tell_us_how_active_you_are
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
    val pagerState = rememberPagerState { OnboardingStep.entries.size }
    val colors = MacroTrackTheme.colors
    val typography = MacroTrackTheme.typography
    LaunchedEffect(key1 = viewState.currentStep.ordinal) {
        pagerState.animateScrollToPage(page = viewState.currentStep.ordinal)
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.white
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(height = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(times = pagerState.pageCount) {
                    Spacer(
                        modifier = Modifier
                            .clip(shape = CircleShape)
                            .size(size = 8.dp)
                            .background(color = if (it == pagerState.currentPage) colors.blue else colors.lightGray)
                    )
                }
            }
            Text(
                text = stringResource(
                    Res.string.step_count,
                    viewState.currentStep.ordinal.inc(),
                    OnboardingStep.entries.size
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

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    OnboardingStep.GOAL_AND_STATS.ordinal -> {
                        OnboardingGoalAndStatsScreen(
                            modifier = Modifier,
                            viewState = viewState,
                            callbacks = callbacks
                        )
                    }

                    OnboardingStep.ACTIVITY_AND_TARGETS.ordinal -> {
                        OnboardingActivityAndTargets(
                            modifier = Modifier,
                            viewState = viewState,
                            callbacks = callbacks
                        )
                    }
                }
            }

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
                enabled = isButtonEnabled(viewState = viewState)
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

private fun isButtonEnabled(viewState: OnboardingViewState): Boolean {
    return when (viewState.currentStep) {
        OnboardingStep.GOAL_AND_STATS -> {
            viewState.age > 0 &&
                    viewState.height > 0 &&
                    viewState.weight > 0 &&
                    viewState.selectedGoal != null &&
                    viewState.selectedGender != null
        }

        OnboardingStep.ACTIVITY_AND_TARGETS -> false
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
    val descriptionRes: StringResource
) {
    GOAL_AND_STATS(
        titleRes = Res.string.set_up_your_profile,
        descriptionRes = Res.string.choose_your_goal
    ),
    ACTIVITY_AND_TARGETS(
        titleRes = Res.string.activity_and_targets,
        descriptionRes = Res.string.tell_us_how_active_you_are
    )
}
