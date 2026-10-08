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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.about_you
import macrotrack.composeapp.generated.resources.activity_level_camel
import macrotrack.composeapp.generated.resources.age
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.cm
import macrotrack.composeapp.generated.resources.current_weight
import macrotrack.composeapp.generated.resources.female
import macrotrack.composeapp.generated.resources.from_your_latest_log
import macrotrack.composeapp.generated.resources.height
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.male
import macrotrack.composeapp.generated.resources.personal_information
import macrotrack.composeapp.generated.resources.save_changes
import macrotrack.composeapp.generated.resources.sex
import macrotrack.composeapp.generated.resources.updates_when_you_change_details
import macrotrack.composeapp.generated.resources.your_daily_target
import macrotrack.composeapp.generated.resources.yrs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.descriptionRes
import org.thelazybattley.macrotrack.domain.model.titleRes
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationCallbacks
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewModel
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState
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
            PersonalInformationHeader(onBack = onBack)
        },
        bottomBar = {
            SaveButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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
private fun PersonalInformationHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_left),
                contentDescription = stringResource(resource = Res.string.back),
                tint = colors.ink,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Text(
            text = stringResource(resource = Res.string.personal_information),
            style = typography.bold18,
            color = colors.ink
        )
    }
}

@Composable
private fun AboutYouSection(
    viewState: PersonalInformationViewState,
    callbacks: PersonalInformationCallbacks
) {
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
                suffix = Res.string.yrs,
                isError = viewState.parsedAge == null,
                keyboardType = KeyboardType.Number,
                onValueChange = callbacks::onAgeChanged
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            InfoRow(label = Res.string.sex) {
                SexToggle(selected = viewState.sex, onSelected = callbacks::onSexSelected)
            }
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            NumberRow(
                label = Res.string.height,
                value = viewState.height,
                suffix = Res.string.cm,
                isError = viewState.parsedHeight == null,
                keyboardType = KeyboardType.Decimal,
                onValueChange = callbacks::onHeightChanged
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            CurrentWeightRow(
                weight = viewState.currentWeight,
                latestWeight = viewState.latestWeight
            )
        }
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
    suffix: StringResource,
    isError: Boolean,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(size = 10.dp)
    InfoRow(label = label) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = typography.bold16.copy(color = colors.ink, textAlign = TextAlign.End),
            cursorBrush = SolidColor(value = colors.accent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .width(width = 72.dp)
                .height(height = 40.dp)
                .clip(shape = shape)
                .background(color = colors.background)
                .border(width = 1.dp, color = if (isError) colors.fat else colors.line, shape = shape),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    innerTextField()
                }
            }
        )
        Text(
            text = stringResource(resource = suffix),
            style = typography.regular13,
            color = colors.muted,
            modifier = Modifier.width(width = 24.dp)
        )
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
            text = weight?.toCleanString() ?: "–",
            style = typography.bold16,
            color = colors.ink
        )
        Text(
            text = stringResource(resource = Res.string.kg),
            style = typography.regular13,
            color = colors.muted,
            modifier = Modifier.width(width = 24.dp)
        )
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
            ActivityLevelOption(
                level = index,
                activityLevel = activityLevel,
                isSelected = activityLevel == selected,
                onClick = { onSelected(activityLevel) }
            )
        }
    }
}

@Composable
private fun ActivityLevelOption(
    level: Int,
    activityLevel: ActivityLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(
            modifier = Modifier.width(width = 28.dp).height(height = 22.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 3.dp)
        ) {
            repeat(times = ActivityLevel.entries.size) { bar ->
                Box(
                    modifier = Modifier
                        .width(width = 3.dp)
                        .height(height = (6 + bar * 4).dp)
                        .background(
                            color = if (bar <= level) colors.accent else colors.line,
                            shape = RoundedCornerShape(size = 2.dp)
                        )
                )
            }
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = activityLevel.titleRes()),
                style = typography.bold15,
                color = colors.ink
            )
            Text(
                text = stringResource(resource = activityLevel.descriptionRes()),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Box(
            modifier = Modifier
                .size(size = 18.dp)
                .background(color = colors.surface, shape = CircleShape)
                .border(
                    width = if (isSelected) 6.dp else 2.dp,
                    color = if (isSelected) colors.accent else colors.faint,
                    shape = CircleShape
                )
        )
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
private fun SaveButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height = 54.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = if (enabled) colors.accent else colors.line)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(resource = Res.string.save_changes),
            style = typography.bold16,
            color = if (enabled) colors.onAccent else colors.faint
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
