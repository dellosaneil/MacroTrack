package org.thelazybattley.macrotrack.features.profile.personalinformation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.about_you
import macrotrack.composeapp.generated.resources.activity_level_camel
import macrotrack.composeapp.generated.resources.age
import macrotrack.composeapp.generated.resources.cm
import macrotrack.composeapp.generated.resources.current_weight
import macrotrack.composeapp.generated.resources.female
import macrotrack.composeapp.generated.resources.from_your_latest_log
import macrotrack.composeapp.generated.resources.height
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.ft
import macrotrack.composeapp.generated.resources.height_feet
import macrotrack.composeapp.generated.resources.height_inches
import macrotrack.composeapp.generated.resources.unit_in
import macrotrack.composeapp.generated.resources.units
import macrotrack.composeapp.generated.resources.units_imperial_detail
import macrotrack.composeapp.generated.resources.units_metric_detail
import macrotrack.composeapp.generated.resources.male
import macrotrack.composeapp.generated.resources.personal_information
import macrotrack.composeapp.generated.resources.save_changes
import macrotrack.composeapp.generated.resources.sex
import macrotrack.composeapp.generated.resources.updates_when_you_change_details
import macrotrack.composeapp.generated.resources.your_daily_target
import macrotrack.composeapp.generated.resources.yrs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.descriptionRes
import org.thelazybattley.macrotrack.domain.model.titleRes
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationCallbacks
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewModel
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState
import org.thelazybattley.macrotrack.ui.common.CommonActivityLevelOption
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.CommonUnitSystemToggle
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightInputText
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import org.thelazybattley.macrotrack.ui.common.CommonKeypadField
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.heightInFeetPreview
import org.thelazybattley.macrotrack.ui.common.heightInCmPreview
import org.thelazybattley.macrotrack.core.KeypadField
import macrotrack.composeapp.generated.resources.keypad_age_subtitle
import macrotrack.composeapp.generated.resources.keypad_feet
import macrotrack.composeapp.generated.resources.keypad_inches
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState.Companion.MIN_AGE
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState.Companion.MAX_AGE
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState.Companion.MIN_HEIGHT
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState.Companion.MAX_HEIGHT

@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<PersonalInformationViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onPopBackStack()
    }

    PersonalInformationScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onPopBackStack
    )
}

@Composable
private fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    viewState: PersonalInformationViewState,
    callbacks: PersonalInformationCallbacks,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(title = Res.string.personal_information, onBack = onBack)
        },
        bottomBar = {
            CommonPrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                text = Res.string.save_changes,
                enabled = viewState.canSave,
                onClick = callbacks::onSave
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            UnitsSection(selected = viewState.unitSystem, onSelected = callbacks::onUnitSystemSelected)
            AboutYouSection(viewState = viewState, callbacks = callbacks)
            ActivityLevelSection(
                selected = viewState.activityLevel,
                onSelected = callbacks::onActivityLevelSelected
            )
            DailyTargetCard(dailyCalories = viewState.dailyCalories)
        }
    }
}

@Composable
private fun AboutYouSection(
    viewState: PersonalInformationViewState,
    callbacks: PersonalInformationCallbacks
) {
    var openField by remember { mutableStateOf<String?>(value = null) }
    val isImperial = viewState.unitSystem == UnitSystem.IMPERIAL
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.about_you))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            NumberRow(
                label = Res.string.age,
                value = viewState.age,
                suffix = stringResource(resource = Res.string.yrs),
                isError = viewState.parsedAge == null,
                isOpen = openField == AGE_FIELD,
                onClick = { openField = AGE_FIELD }
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            InfoRow(label = Res.string.sex) {
                SexToggle(selected = viewState.sex, onSelected = callbacks::onSexSelected)
            }
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            if (isImperial) {
                FeetInchesRow(
                    feet = viewState.heightFeet,
                    inches = viewState.heightInches,
                    isError = viewState.parsedHeight == null,
                    openField = openField,
                    onOpen = { openField = it }
                )
            } else {
                NumberRow(
                    label = Res.string.height,
                    value = viewState.height,
                    suffix = stringResource(resource = Res.string.cm),
                    isError = viewState.parsedHeight == null,
                    isOpen = openField == HEIGHT_FIELD,
                    onClick = { openField = HEIGHT_FIELD }
                )
            }
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            CurrentWeightRow(
                weight = viewState.currentWeight,
                latestWeight = viewState.latestWeight
            )
        }
    }

    openField?.let { open ->
        val ageField = KeypadField(
            id = AGE_FIELD,
            label = stringResource(resource = Res.string.age),
            subtitle = stringResource(resource = Res.string.keypad_age_subtitle),
            unit = stringResource(resource = Res.string.yrs),
            value = viewState.age,
            wholeDigits = 3,
            min = MIN_AGE.toDouble(),
            max = MAX_AGE.toDouble()
        )
        val heightFields = if (isImperial) {
            listOf(
                KeypadField(
                    id = FEET_FIELD,
                    label = stringResource(resource = Res.string.height),
                    shortLabel = stringResource(resource = Res.string.keypad_feet),
                    unit = stringResource(resource = Res.string.ft),
                    value = viewState.heightFeet,
                    wholeDigits = 1,
                    min = 1.0,
                    max = 8.0
                ),
                KeypadField(
                    id = INCHES_FIELD,
                    label = stringResource(resource = Res.string.height),
                    shortLabel = stringResource(resource = Res.string.keypad_inches),
                    unit = stringResource(resource = Res.string.unit_in),
                    value = viewState.heightInches,
                    wholeDigits = 2,
                    decimals = 1,
                    max = UnitSystem.INCHES_PER_FOOT - 0.1,
                    allowsZero = true
                )
            )
        } else {
            listOf(
                KeypadField(
                    id = HEIGHT_FIELD,
                    label = stringResource(resource = Res.string.height),
                    unit = stringResource(resource = Res.string.cm),
                    value = viewState.height,
                    wholeDigits = 3,
                    decimals = 1,
                    min = MIN_HEIGHT,
                    max = MAX_HEIGHT
                )
            )
        }
        CommonKeypadSheet(
            fields = listOf(ageField) + heightFields,
            openId = open,
            onOpen = { openField = it },
            onCommit = { id, text ->
                when (id) {
                    AGE_FIELD -> callbacks.onAgeChanged(age = text)
                    HEIGHT_FIELD -> callbacks.onHeightChanged(height = text)
                    FEET_FIELD -> callbacks.onHeightFeetChanged(feet = text)
                    INCHES_FIELD -> callbacks.onHeightInchesChanged(inches = text)
                }
            },
            onDismiss = { openField = null },
            preview = { id, live ->
                when (id) {
                    HEIGHT_FIELD -> heightInFeetPreview(cm = live(HEIGHT_FIELD))
                    FEET_FIELD, INCHES_FIELD -> heightInCmPreview(feet = live(FEET_FIELD), inches = live(INCHES_FIELD))
                    else -> null
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    label: StringResource,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(
            text = stringResource(resource = label),
            style = typography.medium13,
            color = colors.ink,
            modifier = Modifier.weight(weight = 1f)
        )
        trailing()
    }
}

@Composable
private fun NumberRow(
    label: StringResource,
    value: String,
    suffix: String,
    isError: Boolean,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    InfoRow(label = label) {
        NumberField(
            value = value,
            isError = isError,
            isOpen = isOpen,
            contentDescription = stringResource(resource = label),
            onClick = onClick
        )
        UnitSuffix(text = suffix)
    }
}

/** Height as whole feet plus inches, for imperial units. */
@Composable
private fun FeetInchesRow(
    feet: String,
    inches: String,
    isError: Boolean,
    openField: String?,
    onOpen: (String) -> Unit
) {
    val feetDescription = stringResource(resource = Res.string.height_feet)
    val inchesDescription = stringResource(resource = Res.string.height_inches)
    InfoRow(label = Res.string.height) {
        NumberField(
            modifier = Modifier.width(width = 48.dp),
            value = feet,
            isError = isError,
            isOpen = openField == FEET_FIELD,
            contentDescription = feetDescription,
            onClick = { onOpen(FEET_FIELD) }
        )
        UnitSuffix(text = stringResource(resource = Res.string.ft))
        NumberField(
            modifier = Modifier.width(width = 60.dp),
            value = inches,
            isError = isError,
            isOpen = openField == INCHES_FIELD,
            contentDescription = inchesDescription,
            onClick = { onOpen(INCHES_FIELD) }
        )
        UnitSuffix(text = stringResource(resource = Res.string.unit_in))
    }
}

@Composable
private fun NumberField(
    modifier: Modifier = Modifier.width(width = 72.dp),
    value: String,
    isError: Boolean,
    isOpen: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    CommonKeypadField(
        modifier = modifier.height(height = 40.dp),
        value = value,
        onClick = onClick,
        textAlign = TextAlign.End,
        textStyle = typography.bold16,
        isOpen = isOpen,
        isError = isError,
        background = colors.background,
        cornerRadius = 10.dp,
        horizontalPadding = 10.dp,
        contentDescription = contentDescription
    )
}

private const val AGE_FIELD = "age"
private const val HEIGHT_FIELD = "height"
private const val FEET_FIELD = "feet"
private const val INCHES_FIELD = "inches"

@Composable
private fun UnitSuffix(text: String) {
    Text(
        text = text,
        style = typography.regular13,
        color = colors.muted,
        modifier = Modifier.width(width = 24.dp)
    )
}

@Composable
private fun UnitsSection(
    selected: UnitSystem,
    onSelected: (UnitSystem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.units))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
                .heightIn(min = 58.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Text(
                text = stringResource(
                    resource = if (selected == UnitSystem.IMPERIAL) Res.string.units_imperial_detail else Res.string.units_metric_detail
                ),
                style = typography.medium13,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            CommonUnitSystemToggle(selected = selected, onSelected = onSelected)
        }
    }
}

@Composable
private fun SexToggle(
    selected: UserSex?,
    onSelected: (UserSex) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            .padding(all = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        listOf(UserSex.MALE to Res.string.male, UserSex.FEMALE to Res.string.female).forEach { (sex, label) ->
            val isSelected = sex == selected
            Box(
                modifier = Modifier
                    .height(height = 36.dp)
                    .clip(shape = RoundedCornerShape(size = 9.dp))
                    .background(color = if (isSelected) colors.accent else Color.Transparent)
                    .clickable { onSelected(sex) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = label),
                    style = typography.bold13,
                    color = if (isSelected) colors.onAccent else colors.muted
                )
            }
        }
    }
}

@Composable
private fun CurrentWeightRow(
    weight: Double?,
    latestWeight: Weight?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = Res.string.current_weight),
                style = typography.medium13,
                color = colors.ink
            )
            if (latestWeight != null) {
                Text(
                    text = stringResource(
                        resource = Res.string.from_your_latest_log,
                        latestWeight.date.toAbbreviatedMonthDay()
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        Text(
            text = weight?.let { LocalUnitSystem.current.weightInputText(kg = it) } ?: "–",
            style = typography.bold16,
            color = colors.ink
        )
        UnitSuffix(text = weightUnitLabel())
    }
}

@Composable
private fun ActivityLevelSection(
    selected: ActivityLevel?,
    onSelected: (ActivityLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.activity_level_camel))
        ActivityLevel.entries.forEachIndexed { index, activityLevel ->
            CommonActivityLevelOption(
                level = index,
                activityLevel = activityLevel,
                isSelected = activityLevel == selected,
                onClick = { onSelected(activityLevel) }
            )
        }
    }
}

@Composable
private fun DailyTargetCard(dailyCalories: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.hero)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = Res.string.your_daily_target),
                style = typography.regular11,
                color = colors.muted
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
            ) {
                Text(
                    text = dailyCalories?.withThousands() ?: "–",
                    style = typography.bold24,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.kcal_text),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        Text(
            text = stringResource(resource = Res.string.updates_when_you_change_details),
            style = typography.regular11,
            color = colors.muted,
            textAlign = TextAlign.End,
            modifier = Modifier.width(width = 140.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = typography.bold12,
        color = colors.muted
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonalInformationScreen() {
    MacroTrackTheme {
        PersonalInformationScreen(
            modifier = Modifier.fillMaxWidth(),
            viewState = PersonalInformationViewState(
                userDetails = UserDetails(
                    weight = 74.2,
                    age = 29,
                    height = 175.0,
                    sex = UserSex.MALE,
                    activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                    goal = Goal.LOSE_WEIGHT
                ),
                latestWeight = Weight(date = getCurrentDate(), weight = 74.2),
                age = "29",
                height = "175",
                sex = UserSex.MALE,
                activityLevel = ActivityLevel.LIGHTLY_ACTIVE,
                dailyCalories = 2150
            ),
            callbacks = PersonalInformationCallbacks.default(),
            onBack = {}
        )
    }
}
