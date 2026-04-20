package org.thelazybattley.macrotrack.features.profile.personalinformation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_level_camel
import macrotrack.composeapp.generated.resources.age
import macrotrack.composeapp.generated.resources.cm
import macrotrack.composeapp.generated.resources.empty
import macrotrack.composeapp.generated.resources.female
import macrotrack.composeapp.generated.resources.height
import macrotrack.composeapp.generated.resources.how_active_are_you_on_a_typical_week
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.male
import macrotrack.composeapp.generated.resources.personal_information
import macrotrack.composeapp.generated.resources.sex
import macrotrack.composeapp.generated.resources.yrs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.descriptionRes
import org.thelazybattley.macrotrack.domain.model.iconRes
import org.thelazybattley.macrotrack.domain.model.titleRes
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationCallbacks
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewModel
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState
import org.thelazybattley.macrotrack.ui.common.CommonBottomSheet
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<PersonalInformationViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()


    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.white,
        topBar = {
            CommonTopBar(
                stringResource = Res.string.personal_information,
                onClick = {
                    onPopBackStack()
                }
            )
        }
    ) { innerPadding ->
        PersonalInformationScreen(
            modifier = Modifier.padding(paddingValues = innerPadding),
            viewState = viewState,
            callbacks = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    viewState: PersonalInformationViewState,
    callbacks: PersonalInformationCallbacks
) {
    val showBottomSheet = remember { mutableStateOf(value = false) }
    LaunchedEffect(key1 = viewState.userDetails?.activityLevel) {
        showBottomSheet.value = false
    }
    Column(
        modifier = modifier
            .padding(paddingValues = AppPadding)
            .scrollable(
                state = rememberScrollState(),
                orientation = Orientation.Vertical
            ),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            PersonalInfoValues(
                modifier = Modifier.weight(weight = 0.5f),
                title = Res.string.age,
                value = viewState.userDetails?.age.toString(),
                suffixText = Res.string.yrs
            )
            val maleColors =
                getPersonalInfoSexColors(selected = viewState.userDetails?.sex == UserSex.MALE)
            val femaleColors =
                getPersonalInfoSexColors(selected = viewState.userDetails?.sex == UserSex.FEMALE)
            PersonalInfoValues(
                modifier = Modifier.weight(weight = 0.25f),
                title = Res.string.sex,
                value = stringResource(resource = Res.string.male),
                backgroundColor = maleColors.backgroundColor,
                borderColor = maleColors.borderColor,
                textColor = maleColors.textColor
            )

            PersonalInfoValues(
                modifier = Modifier.weight(weight = 0.25f),
                title = Res.string.empty,
                value = stringResource(resource = Res.string.female),
                backgroundColor = femaleColors.backgroundColor,
                borderColor = femaleColors.borderColor,
                textColor = femaleColors.textColor
            )
        }

        PersonalInfoValues(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.height,
            value = viewState.userDetails?.height.toString(),
            suffixText = Res.string.cm
        )
        if (viewState.userDetails?.activityLevel != null) {
            ActivityLevelUi(
                modifier = Modifier.fillMaxWidth(),
                activityLevel = viewState.userDetails.activityLevel
            ) {
                showBottomSheet.value = true
            }
        }
    }


    val bottomSheetState = rememberModalBottomSheetState()
    CommonBottomSheet(
        bottomSheetState = bottomSheetState,
        showBottomSheet = showBottomSheet
    ) { bottomSheetModifier ->
        ActivityLevelBottomSheetContent(
            modifier = bottomSheetModifier,
            currentActivityLevel = viewState.userDetails?.activityLevel,
            callbacks = callbacks
        )
    }
}

@Composable
private fun ActivityLevelUi(
    modifier: Modifier = Modifier,
    activityLevel: ActivityLevel,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.activity_level_camel),
            color = colors.mediumGray,
            style = typography.bold10
        )
        Row(
            modifier = Modifier
                .clickable {
                    onClick()
                }
                .fillMaxWidth()
                .background(
                    color = colors.white,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = colors.lightGray,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Image(
                modifier = Modifier
                    .background(color = colors.paleBlue, shape = RoundedCornerShape(size = 8.dp))
                    .padding(all = 8.dp)
                    .size(size = 16.dp),
                painter = painterResource(resource = activityLevel.iconRes()),
                contentDescription = null
            )
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(
                    text = stringResource(resource = activityLevel.titleRes()),
                    color = colors.black,
                    style = typography.bold14,
                )
                Text(
                    text = stringResource(resource = activityLevel.descriptionRes()),
                    color = colors.mediumGray,
                    style = typography.regular11
                )
            }

            Icon(
                modifier = Modifier.size(size = 16.dp).rotate(degrees = 180f),
                painter = painterResource(resource = Res.drawable.ic_chevron_left),
                contentDescription = null,
                tint = colors.mediumGray
            )
        }
    }
}

@Composable
private fun ActivityLevelBottomSheetContent(
    modifier: Modifier = Modifier,
    currentActivityLevel: ActivityLevel?,
    callbacks: PersonalInformationCallbacks
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.activity_level_camel),
            style = typography.bold18,
            color = colors.black
        )
        Text(
            text = stringResource(resource = Res.string.how_active_are_you_on_a_typical_week),
            color = colors.mediumGray,
            style = typography.regular11
        )

        ActivityLevel.entries.forEach { activityLevel ->
            ActivityLevelPicker(
                modifier = Modifier.fillMaxWidth(),
                isSelected = currentActivityLevel == activityLevel,
                activityLevel = activityLevel
            ) {
                callbacks.onActivityLevelSelected(activityLevel = activityLevel)
            }
        }
    }
}

@Composable
private fun ActivityLevelPicker(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    activityLevel: ActivityLevel,
    onClick: () -> Unit
) {
    val pickerColors = if (isSelected) {
        ActivityLevelPickerColors(
            containerColor = colors.paleBlue,
            borderColor = colors.deepBlue,
            titleTextColor = colors.deepBlue,
            descriptionTextColor = colors.deepBlue.copy(
                alpha = 0.8f
            ),
            multiplierColor = colors.deepBlue
        )
    } else {
        ActivityLevelPickerColors(
            containerColor = colors.iceMist,
            borderColor = Color.Transparent,
            titleTextColor = colors.black,
            descriptionTextColor = colors.mediumGray,
            multiplierColor = colors.mediumGray
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = pickerColors.containerColor),
        border = BorderStroke(width = 1.dp, color = pickerColors.borderColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(all = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Image(
                painter = painterResource(resource = activityLevel.iconRes()),
                contentDescription = null,
                modifier = Modifier.size(size = 16.dp)
            )
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(
                    text = stringResource(resource = activityLevel.titleRes()),
                    style = typography.bold13,
                    color = pickerColors.titleTextColor
                )
                Text(
                    text = stringResource(resource = activityLevel.descriptionRes()),
                    style = typography.regular10,
                    color = pickerColors.descriptionTextColor

                )
            }
            Text(
                text = "x${activityLevel.multiplier}",
                color = pickerColors.multiplierColor,
                style = typography.bold11,
            )
        }
    }
}

@Composable
private fun PersonalInfoValues(
    modifier: Modifier = Modifier,
    title: StringResource,
    value: String,
    suffixText: StringResource? = null,
    backgroundColor: Color = colors.iceMist,
    borderColor: Color = colors.lightGray,
    textColor: Color = colors.black,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = title),
            color = colors.mediumGray,
            style = typography.bold10
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                color = textColor,
                style = typography.bold15,
            )
            if (suffixText != null) {
                Text(
                    text = stringResource(resource = suffixText),
                    style = typography.regular11,
                    color = colors.mediumGray
                )
            }
        }
    }
}

@Composable
private fun getPersonalInfoSexColors(selected: Boolean): PersonalInfoSexColors {
    return if (selected) {
        PersonalInfoSexColors(
            backgroundColor = colors.paleBlue,
            textColor = colors.deepBlue,
            borderColor = colors.deepBlue
        )
    } else {
        PersonalInfoSexColors(
            backgroundColor = colors.iceMist,
            borderColor = colors.lightGray,
            textColor = colors.mediumGray,
        )
    }
}

private data class PersonalInfoSexColors(
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color,
)


@Preview(showBackground = true)
@Composable
private fun PreviewPersonalInformationScreen() {
    MacroTrackTheme {
        PersonalInformationScreen(
            modifier = Modifier.fillMaxWidth(),
            viewState = PersonalInformationViewState(
                userDetails = UserDetails(
                    weight = 60.0,
                    age = 20,
                    height = 170.0,
                    sex = UserSex.MALE,
                    activityLevel = ActivityLevel.LIGHTLY_ACTIVE,
                    goal = Goal.MAINTAIN_WEIGHT
                )
            ),
            callbacks = PersonalInformationCallbacks.default()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewActivityLevelBottomSheetContent() {
    ActivityLevelBottomSheetContent(
        modifier = Modifier.fillMaxWidth(),
        currentActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
        callbacks = PersonalInformationCallbacks.default()
    )
}


private data class ActivityLevelPickerColors(
    val containerColor: Color,
    val titleTextColor: Color,
    val borderColor: Color,
    val descriptionTextColor: Color,
    val multiplierColor: Color
)
