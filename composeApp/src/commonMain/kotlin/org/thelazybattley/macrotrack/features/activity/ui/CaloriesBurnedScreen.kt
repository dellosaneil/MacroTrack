package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.burn_based_on_you
import macrotrack.composeapp.generated.resources.burn_bmr
import macrotrack.composeapp.generated.resources.burn_bmr_sub
import macrotrack.composeapp.generated.resources.burn_budget_body
import macrotrack.composeapp.generated.resources.burn_budget_preview
import macrotrack.composeapp.generated.resources.burn_budget_preview_body
import macrotrack.composeapp.generated.resources.burn_budget_target
import macrotrack.composeapp.generated.resources.burn_budget_title
import macrotrack.composeapp.generated.resources.burn_disclaimer
import macrotrack.composeapp.generated.resources.burn_edit
import macrotrack.composeapp.generated.resources.burn_everyday
import macrotrack.composeapp.generated.resources.burn_everyday_distance
import macrotrack.composeapp.generated.resources.burn_everyday_distance_math
import macrotrack.composeapp.generated.resources.burn_everyday_energy
import macrotrack.composeapp.generated.resources.burn_everyday_energy_math
import macrotrack.composeapp.generated.resources.burn_footnote
import macrotrack.composeapp.generated.resources.burn_from_steps
import macrotrack.composeapp.generated.resources.burn_hero_label
import macrotrack.composeapp.generated.resources.burn_hero_resting
import macrotrack.composeapp.generated.resources.burn_how
import macrotrack.composeapp.generated.resources.burn_kcal_steps
import macrotrack.composeapp.generated.resources.burn_locked_body
import macrotrack.composeapp.generated.resources.burn_locked_free
import macrotrack.composeapp.generated.resources.burn_locked_label
import macrotrack.composeapp.generated.resources.burn_locked_perk_budget
import macrotrack.composeapp.generated.resources.burn_locked_perk_history
import macrotrack.composeapp.generated.resources.burn_locked_perk_sessions
import macrotrack.composeapp.generated.resources.burn_locked_perk_today
import macrotrack.composeapp.generated.resources.burn_locked_unlock
import macrotrack.composeapp.generated.resources.burn_ml_per_kg_min
import macrotrack.composeapp.generated.resources.burn_session_energy
import macrotrack.composeapp.generated.resources.burn_session_energy_math
import macrotrack.composeapp.generated.resources.burn_session_gps
import macrotrack.composeapp.generated.resources.burn_session_gps_math
import macrotrack.composeapp.generated.resources.burn_split_description
import macrotrack.composeapp.generated.resources.burn_step_length
import macrotrack.composeapp.generated.resources.burn_title
import macrotrack.composeapp.generated.resources.burn_weight_latest
import macrotrack.composeapp.generated.resources.burn_weight_profile
import macrotrack.composeapp.generated.resources.burn_height_sub
import macrotrack.composeapp.generated.resources.burn_age_sub
import macrotrack.composeapp.generated.resources.burn_sex_sub
import macrotrack.composeapp.generated.resources.age
import macrotrack.composeapp.generated.resources.female
import macrotrack.composeapp.generated.resources.height
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.male
import macrotrack.composeapp.generated.resources.sex
import macrotrack.composeapp.generated.resources.weight
import macrotrack.composeapp.generated.resources.yrs
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toDayMonthDay
import org.thelazybattley.macrotrack.domain.model.ActivityCalories
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.BurnProfile
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.features.activity.CaloriesBurnedCallbacks
import org.thelazybattley.macrotrack.features.activity.CaloriesBurnedViewModel
import org.thelazybattley.macrotrack.features.activity.CaloriesBurnedViewState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatHeightWithUnit
import org.thelazybattley.macrotrack.ui.common.formatWeightOneDecimalWithUnit
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CaloriesBurnedScreen(
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit,
    onEditProfile: () -> Unit
) {
    val viewModel = koinViewModel<CaloriesBurnedViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    CaloriesBurnedScreen(viewState = viewState, callbacks = viewModel, onBack = onBack, onOpenSession = onOpenSession, onEditProfile = onEditProfile)
}

@Composable
private fun CaloriesBurnedScreen(
    viewState: CaloriesBurnedViewState,
    callbacks: CaloriesBurnedCallbacks,
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit,
    onEditProfile: () -> Unit
) {
    val pro = LocalProAccess.current
    val profile = viewState.profile
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 18.dp)
    ) {
        ActivityHeader(
            overline = stringResource(resource = Res.string.burn_from_steps, viewState.date.toDayMonthDay()),
            title = stringResource(resource = Res.string.burn_title),
            onBack = onBack
        )
        if (pro.isActive && profile != null) {
            BurnHero(viewState = viewState, profile = profile, onOpenSession = onOpenSession)
        } else if (!pro.isActive) {
            LockedCard(onUnlock = { pro.openPaywall(ProFeature.CALORIES_BURNED) })
        }
        profile?.let {
            BasedOnYou(profile = it, onEdit = onEditProfile)
            if (pro.isActive) {
                HowItsWorkedOut(viewState = viewState, profile = it)
                BudgetCard(viewState = viewState, profile = it, onToggle = callbacks::onBudgetToggled)
            }
        }
        InfoNote(text = stringResource(resource = Res.string.burn_disclaimer))
    }
}

@Composable
private fun BurnHero(viewState: CaloriesBurnedViewState, profile: BurnProfile, onOpenSession: (Long) -> Unit) {
    val day = viewState.day
    val sessions = day.sessions
    val everyday = day.everydayKcal(profile = profile)
    val total = day.activeKcal(profile = profile)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Column {
            Text(text = stringResource(resource = Res.string.burn_hero_label), style = typography.regular11, color = colors.muted)
            ValueWithUnit(value = "≈" + total.withThousands(), unit = stringResource(resource = Res.string.kcal_text), valueStyle = typography.bold36)
            Text(
                text = stringResource(resource = Res.string.burn_hero_resting, profile.restingBurn.withThousands()),
                style = typography.regular13,
                color = colors.muted
            )
        }
        val parts = sessions.map { profile.kcal(session = it) } + everyday
        val description = stringResource(resource = Res.string.burn_split_description, total.withThousands())
        if (total > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 12.dp)
                    .clip(shape = RoundedCornerShape(size = 6.dp))
                    .semantics { contentDescription = description },
                horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                parts.forEachIndexed { index, kcal ->
                    if (kcal > 0) {
                        Box(
                            modifier = Modifier
                                .weight(weight = kcal.toFloat())
                                .fillMaxHeight()
                                .background(color = partColor(index = index, everydayIndex = parts.lastIndex))
                        )
                    }
                }
            }
        }
        (sessions.map { it as ActivitySession? } + null).chunked(size = 2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                row.forEach { session ->
                    val index = session?.let { sessions.indexOf(it) } ?: parts.lastIndex
                    PartTile(
                        modifier = Modifier.weight(weight = 1f),
                        color = partColor(index = index, everydayIndex = parts.lastIndex),
                        label = session?.displayName() ?: stringResource(resource = Res.string.burn_everyday),
                        kcal = parts[index],
                        steps = session?.steps ?: day.everydaySteps,
                        onClick = session?.let { { onOpenSession(it.id) } }
                    )
                }
                if (row.size == 1) Box(modifier = Modifier.weight(weight = 1f))
            }
        }
    }
}

/** Walks and runs in the accent, everyday steps in the lighter track colour. */
@Composable
private fun partColor(index: Int, everydayIndex: Int): Color = if (index == everydayIndex) colors.heroTrack else colors.accent.copy(alpha = 1f - 0.25f * (index % 3))

@Composable
private fun PartTile(
    modifier: Modifier = Modifier,
    color: Color,
    label: String,
    kcal: Int,
    steps: Int,
    onClick: (() -> Unit)?
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.surface)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Box(modifier = Modifier.size(size = 8.dp).clip(shape = RoundedCornerShape(size = 2.dp)).background(color = color))
            Text(text = label, style = typography.medium11, color = colors.muted, maxLines = 1)
        }
        ValueWithUnit(
            value = kcal.withThousands(),
            unit = stringResource(resource = Res.string.burn_kcal_steps, steps.withThousands())
        )
    }
}

@Composable
private fun LockedCard(onUnlock: () -> Unit) {
    val shape = RoundedCornerShape(size = 24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.5.dp, color = colors.warmInk, shape = shape)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = Res.string.burn_locked_label),
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            ProBadge()
        }
        ValueWithUnit(value = "≈ – – –", unit = stringResource(resource = Res.string.kcal_text), valueStyle = typography.bold36, valueColor = colors.line)
        Text(text = stringResource(resource = Res.string.burn_locked_body), style = typography.regular13, color = colors.ink)
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            listOf(
                Res.string.burn_locked_perk_today,
                Res.string.burn_locked_perk_sessions,
                Res.string.burn_locked_perk_history,
                Res.string.burn_locked_perk_budget
            ).forEach { perk ->
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(text = "✓", style = typography.bold13, color = colors.accentInk)
                    Text(text = stringResource(resource = perk), style = typography.regular13, color = colors.muted)
                }
            }
        }
        CommonPrimaryButton(modifier = Modifier.fillMaxWidth(), text = stringResource(resource = Res.string.burn_locked_unlock), onClick = onUnlock)
        Text(
            text = stringResource(resource = Res.string.burn_locked_free),
            style = typography.regular11,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BasedOnYou(profile: BurnProfile, onEdit: () -> Unit) {
    val rows = listOf(
        Triple(
            stringResource(resource = Res.string.weight),
            profile.weightDate?.let { stringResource(resource = Res.string.burn_weight_latest, it.toAbbreviatedMonthDay()) }
                ?: stringResource(resource = Res.string.burn_weight_profile),
            profile.weightKg.formatWeightOneDecimalWithUnit()
        ),
        Triple(stringResource(resource = Res.string.height), stringResource(resource = Res.string.burn_height_sub), profile.heightCm.formatHeightWithUnit()),
        Triple(stringResource(resource = Res.string.age), stringResource(resource = Res.string.burn_age_sub), "${profile.age} " + stringResource(resource = Res.string.yrs)),
        Triple(
            stringResource(resource = Res.string.sex),
            stringResource(resource = Res.string.burn_sex_sub),
            stringResource(resource = if (profile.sex == UserSex.MALE) Res.string.male else Res.string.female)
        ),
        Triple(
            stringResource(resource = Res.string.burn_bmr),
            stringResource(resource = Res.string.burn_bmr_sub),
            profile.restingBurn.withThousands() + " " + stringResource(resource = Res.string.kcal_text)
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.burn_based_on_you))
            Text(
                text = stringResource(resource = Res.string.burn_edit),
                style = typography.bold13,
                color = colors.accentInk,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .clickable(role = Role.Button, onClick = onEdit)
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            rows.forEachIndexed { index, (label, sub, value) ->
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(text = label, style = typography.medium13, color = colors.ink)
                        Text(text = sub, style = typography.regular11, color = colors.muted)
                    }
                    Text(text = value, style = typography.bold15, color = colors.ink)
                }
                if (index != rows.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            }
        }
    }
}

@Composable
private fun HowItsWorkedOut(viewState: CaloriesBurnedViewState, profile: BurnProfile) {
    val day = viewState.day
    val isImperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
    val weight = profile.weightKg.formatWeightOneDecimalWithUnit()
    val stepLength = profile.stepLengthMeters
    val stepLengthText = if (isImperial) {
        (stepLength * 39.3701).toFixed(decimals = 1) + " in"
    } else {
        (stepLength * 100).toFixed(decimals = 1) + " cm"
    }
    val everydayMeters = day.everydaySteps * stepLength
    val steps = buildList {
        add(
            Triple(
                stringResource(resource = Res.string.burn_step_length),
                profile.heightCm.formatHeightWithUnit() + " × " + ActivityCalories.stepLengthFactor(sex = profile.sex),
                stepLengthText
            )
        )
        add(
            Triple(
                stringResource(resource = Res.string.burn_everyday_distance),
                stringResource(resource = Res.string.burn_everyday_distance_math, day.everydaySteps.withThousands(), stepLengthText),
                distanceText(meters = everydayMeters)
            )
        )
        day.sessions.forEach { session ->
            val minutes = session.movingSeconds / 60.0
            val oxygenPerKg = ActivityCalories.oxygenPerKg(type = session.type, meters = session.distanceMeters, climbMeters = session.climbMeters)
            val perMinute = if (minutes > 0) oxygenPerKg / minutes else 0.0
            val liters = oxygenPerKg * profile.weightKg / 1000
            add(
                Triple(
                    stringResource(resource = Res.string.burn_session_gps, session.displayName()),
                    stringResource(
                        resource = Res.string.burn_session_gps_math,
                        distanceText(meters = session.distanceMeters),
                        minutes.toFixed(decimals = 1),
                        speedText(meters = session.distanceMeters, seconds = session.movingSeconds),
                        climbValue(meters = session.climbMeters) + " " + climbUnit()
                    ),
                    stringResource(resource = Res.string.burn_ml_per_kg_min, perMinute.toFixed(decimals = 1))
                )
            )
            add(
                Triple(
                    stringResource(resource = Res.string.burn_session_energy, session.displayName()),
                    stringResource(
                        resource = Res.string.burn_session_energy_math,
                        perMinute.toFixed(decimals = 1),
                        weight,
                        minutes.toFixed(decimals = 1),
                        liters.toFixed(decimals = 1)
                    ),
                    profile.kcal(session = session).withThousands() + " " + stringResource(resource = Res.string.kcal_text)
                )
            )
        }
        add(
            Triple(
                stringResource(resource = Res.string.burn_everyday_energy),
                stringResource(resource = Res.string.burn_everyday_energy_math, weight, distanceText(meters = everydayMeters)),
                day.everydayKcal(profile = profile).withThousands() + " " + stringResource(resource = Res.string.kcal_text)
            )
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionTitle(text = stringResource(resource = Res.string.burn_how))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            steps.forEachIndexed { index, (title, math, result) ->
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 24.dp)
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .background(color = colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = (index + 1).toString(), style = typography.bold12, color = colors.accentInk)
                    }
                    Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                        Text(text = title, style = typography.bold13, color = colors.ink)
                        Text(text = math, style = typography.regular13, color = colors.muted)
                    }
                    Text(text = result, style = typography.bold14, color = colors.ink)
                }
                if (index != steps.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            }
        }
        Text(text = stringResource(resource = Res.string.burn_footnote), style = typography.regular11, color = colors.muted, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun BudgetCard(viewState: CaloriesBurnedViewState, profile: BurnProfile, onToggle: () -> Unit) {
    ActivityCard {
        SwitchRow(
            title = stringResource(resource = Res.string.burn_budget_title),
            body = stringResource(resource = Res.string.burn_budget_body),
            isOn = viewState.isAddedToBudget,
            onToggle = onToggle
        )
        val goal = viewState.calorieGoal
        if (viewState.isAddedToBudget && goal != null) {
            val active = viewState.day.activeKcal(profile = profile)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.warmSoft)
                    .padding(all = 12.dp),
                verticalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Row {
                    Text(text = stringResource(resource = Res.string.burn_budget_target), style = typography.bold13, color = colors.warmInk, modifier = Modifier.weight(weight = 1f))
                    Text(
                        text = stringResource(resource = Res.string.burn_budget_preview, goal.withThousands(), active.withThousands(), (goal + active).withThousands()),
                        style = typography.bold13,
                        color = colors.warmInk
                    )
                }
                Text(text = stringResource(resource = Res.string.burn_budget_preview_body), style = typography.regular13, color = colors.warmInk)
            }
        }
    }
}
