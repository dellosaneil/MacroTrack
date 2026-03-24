package org.thelazybattley.macrotrack.features.onboarding.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_level_camel
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.choose_your_goal
import macrotrack.composeapp.generated.resources.continue_text
import macrotrack.composeapp.generated.resources.how_active_are_you
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.lets_go
import macrotrack.composeapp.generated.resources.personalized_plan
import macrotrack.composeapp.generated.resources.set_up_your_profile
import macrotrack.composeapp.generated.resources.youre_all_set
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, onFinishButtonClicked: () -> Unit) {
    val viewModel: OnboardingViewModel = koinViewModel<OnboardingViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    OnboardingScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onFinish = onFinishButtonClicked
    )
}

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState { OnboardingStep.entries.size }

    LaunchedEffect(key1 = viewState.currentStep.ordinal) {
        pagerState.animateScrollToPage(page = viewState.currentStep.ordinal)
    }
    LaunchedEffect(key1 = viewState.isFinished) {
        if (viewState.isFinished) {
            onFinish()
        }
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier.fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            },
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
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

        if (viewState.currentStep.ordinal != 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    callbacks.onBackClicked()
                }
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_chevron_left),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colors.blue,
                )
                Text(
                    text = stringResource(resource = Res.string.back),
                    color = colors.deepBlue,
                    style = typography.medium13
                )
            }
        }
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
            userScrollEnabled = false,
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                OnboardingStep.GOAL_AND_STATS.ordinal -> {
                    OnboardingGoalAndStatsScreen(
                        modifier = Modifier,
                        viewState = viewState,
                        callbacks = callbacks
                    )
                }

                OnboardingStep.ACTIVITY_LEVEL.ordinal -> {
                    OnboardingSetActivityLevel(
                        modifier = Modifier,
                        viewState = viewState,
                        callbacks = callbacks
                    )
                }

                OnboardingStep.SUMMARY.ordinal -> {
                    OnboardingSummaryScreen(
                        modifier = Modifier,
                        viewState = viewState,
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
            val text = if (viewState.currentStep == OnboardingStep.SUMMARY) {
                Res.string.lets_go
            } else {
                Res.string.continue_text
            }
            Text(
                text = stringResource(resource = text),
                style = typography.bold15
            )
        }
        Spacer(modifier = Modifier.height(height = 16.dp))
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

        OnboardingStep.ACTIVITY_LEVEL -> viewState.selectedActivityLevel != null
        OnboardingStep.SUMMARY -> true
    }
}

@Preview
@Composable
private fun PreviewOnboardingScreen() {
    MacroTrackTheme {
        OnboardingScreen(
            viewState = OnboardingViewState(),
            callbacks = OnboardingCallbacks.default()
        ) {

        }
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
    ACTIVITY_LEVEL(
        titleRes = Res.string.activity_level_camel,
        descriptionRes = Res.string.how_active_are_you
    ),
    SUMMARY(
        titleRes = Res.string.youre_all_set,
        descriptionRes = Res.string.personalized_plan
    )
}
