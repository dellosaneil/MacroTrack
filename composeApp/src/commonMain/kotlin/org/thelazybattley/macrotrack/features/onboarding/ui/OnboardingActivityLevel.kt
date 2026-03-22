package org.thelazybattley.macrotrack.features.onboarding.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_level
import macrotrack.composeapp.generated.resources.extremely_active
import macrotrack.composeapp.generated.resources.hard_training_or_sport
import macrotrack.composeapp.generated.resources.ic_heartbeat_extremely_active
import macrotrack.composeapp.generated.resources.ic_heartbeat_lightly_active
import macrotrack.composeapp.generated.resources.ic_heartbeat_moderately_active
import macrotrack.composeapp.generated.resources.ic_heartbeat_sedentary
import macrotrack.composeapp.generated.resources.ic_heartbeat_very_active
import macrotrack.composeapp.generated.resources.lightly_active
import macrotrack.composeapp.generated.resources.little_or_no_exercise
import macrotrack.composeapp.generated.resources.moderately_active
import macrotrack.composeapp.generated.resources.one_to_three_days_per_week
import macrotrack.composeapp.generated.resources.sedentary
import macrotrack.composeapp.generated.resources.six_to_seven_days_a_week
import macrotrack.composeapp.generated.resources.three_to_five_days_a_week
import macrotrack.composeapp.generated.resources.very_active
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingCallbacks
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun OnboardingSetActivityLevel(
    modifier: Modifier = Modifier, viewState: OnboardingViewState,
    callbacks: OnboardingCallbacks
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        item {
            Text(
                text = stringResource(resource = Res.string.activity_level),
                color = colors.charcoalGray,
                style = typography.bold12
            )
        }
        items(items = ActivityLevel.entries, key = {
            it.name
        }) { activityLevel ->
            ActivityLevelChoices(
                modifier = Modifier,
                isSelected = viewState.selectedActivityLevel == activityLevel,
                title = activityLevel.titleRes(),
                description = activityLevel.descriptionRes(),
                icon = activityLevel.iconRes(),
            ) {
                callbacks.onActivityLevelSelected(activityLevel = activityLevel)
            }
        }
    }
}

private fun ActivityLevel.descriptionRes(): StringResource {
    return when (this) {
        ActivityLevel.SEDENTARY -> Res.string.little_or_no_exercise
        ActivityLevel.LIGHTLY_ACTIVE -> Res.string.one_to_three_days_per_week
        ActivityLevel.MODERATELY_ACTIVE -> Res.string.three_to_five_days_a_week
        ActivityLevel.VERY_ACTIVE -> Res.string.six_to_seven_days_a_week
        ActivityLevel.EXTREMELY_ACTIVE -> Res.string.hard_training_or_sport
    }
}

private fun ActivityLevel.iconRes(): DrawableResource {
    return when (this) {
        ActivityLevel.SEDENTARY -> Res.drawable.ic_heartbeat_sedentary
        ActivityLevel.LIGHTLY_ACTIVE -> Res.drawable.ic_heartbeat_lightly_active
        ActivityLevel.MODERATELY_ACTIVE -> Res.drawable.ic_heartbeat_moderately_active
        ActivityLevel.VERY_ACTIVE -> Res.drawable.ic_heartbeat_very_active
        ActivityLevel.EXTREMELY_ACTIVE -> Res.drawable.ic_heartbeat_extremely_active
    }
}

private fun ActivityLevel.titleRes(): StringResource {
    return when (this) {
        ActivityLevel.SEDENTARY -> Res.string.sedentary
        ActivityLevel.LIGHTLY_ACTIVE -> Res.string.lightly_active
        ActivityLevel.MODERATELY_ACTIVE -> Res.string.moderately_active
        ActivityLevel.VERY_ACTIVE -> Res.string.very_active
        ActivityLevel.EXTREMELY_ACTIVE -> Res.string.extremely_active
    }
}

@Composable
private fun ActivityLevelChoices(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    title: StringResource,
    description: StringResource,
    icon: DrawableResource,
    onClick: () -> Unit
) {
    val background = if (isSelected) {
        colors.skyBlue
    } else {
        Color.Transparent
    }
    val borderStroke = if (isSelected) {
        BorderStroke(
            width = 2.dp,
            color = colors.deepBlue
        )
    } else {
        BorderStroke(
            width = 1.dp,
            color = colors.lightGray
        )
    }
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
        ),
        border = borderStroke,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(size = 36.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(
                    text = stringResource(resource = title),
                    color = colors.black,
                    style = typography.bold12
                )
                Text(
                    text = stringResource(resource = description),
                    color = colors.gray,
                    style = typography.regular10
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewOnboardingSetActivityLevel() {
    MacroTrackTheme {
        OnboardingSetActivityLevel(
            modifier = Modifier,
            viewState = OnboardingViewState(),
            callbacks = OnboardingCallbacks.default()
        )
    }
}
