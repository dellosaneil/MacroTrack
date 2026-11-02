package org.thelazybattley.macrotrack.features.whatfits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.what_fits_amounts_based_on
import macrotrack.composeapp.generated.resources.what_fits_basis_last_sub
import macrotrack.composeapp.generated.resources.what_fits_basis_last_title
import macrotrack.composeapp.generated.resources.what_fits_basis_serving_sub
import macrotrack.composeapp.generated.resources.what_fits_basis_serving_title
import macrotrack.composeapp.generated.resources.what_fits_basis_usual_sub
import macrotrack.composeapp.generated.resources.what_fits_basis_usual_title
import macrotrack.composeapp.generated.resources.what_fits_put_first
import macrotrack.composeapp.generated.resources.what_fits_rank_calories
import macrotrack.composeapp.generated.resources.what_fits_rank_carbs
import macrotrack.composeapp.generated.resources.what_fits_rank_protein
import macrotrack.composeapp.generated.resources.what_fits_settings_sub
import macrotrack.composeapp.generated.resources.what_fits_settings_title
import macrotrack.composeapp.generated.resources.what_fits_show_ideas
import macrotrack.composeapp.generated.resources.what_fits_skip_today_sub
import macrotrack.composeapp.generated.resources.what_fits_skip_today_title
import macrotrack.composeapp.generated.resources.what_fits_source_my_foods_sub
import macrotrack.composeapp.generated.resources.what_fits_source_my_foods_title
import macrotrack.composeapp.generated.resources.what_fits_source_recent_sub
import macrotrack.composeapp.generated.resources.what_fits_source_recent_title
import macrotrack.composeapp.generated.resources.what_fits_source_recipes_sub
import macrotrack.composeapp.generated.resources.what_fits_source_recipes_title
import macrotrack.composeapp.generated.resources.what_fits_spare
import macrotrack.composeapp.generated.resources.what_fits_too_big_hide
import macrotrack.composeapp.generated.resources.what_fits_too_big_shrink
import macrotrack.composeapp.generated.resources.what_fits_when_too_big
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.FitAmountBasis
import org.thelazybattley.macrotrack.domain.model.FitRanking
import org.thelazybattley.macrotrack.domain.model.FitSource
import org.thelazybattley.macrotrack.domain.model.FitTooBig
import org.thelazybattley.macrotrack.domain.model.WhatFitsSettings
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsCallbacks
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewModel
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WhatFitsSettingsScreen(viewModel: WhatFitsViewModel, onBack: () -> Unit) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    WhatFitsSettingsScreen(settings = viewState.settings, callbacks = viewModel, onBack = onBack)
}

@Composable
private fun WhatFitsSettingsScreen(
    settings: WhatFitsSettings,
    callbacks: WhatFitsCallbacks,
    onBack: () -> Unit
) {
    WhatFitsScaffold(
        title = stringResource(resource = Res.string.what_fits_settings_title),
        subtitle = stringResource(resource = Res.string.what_fits_settings_sub),
        onBack = onBack,
        backIcon = Res.drawable.ic_close,
        backDescription = stringResource(resource = Res.string.close),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.background)
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
            ) {
                CommonPrimaryButton(
                    modifier = Modifier.fillMaxWidth().height(height = 54.dp),
                    text = stringResource(resource = Res.string.what_fits_show_ideas),
                    onClick = onBack
                )
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            WhatFitsSectionLabel(text = stringResource(resource = Res.string.what_fits_amounts_based_on))
            Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                listOf(
                    Triple(FitAmountBasis.LAST_TIME, Res.string.what_fits_basis_last_title, Res.string.what_fits_basis_last_sub),
                    Triple(FitAmountBasis.USUAL, Res.string.what_fits_basis_usual_title, Res.string.what_fits_basis_usual_sub),
                    Triple(FitAmountBasis.SERVING, Res.string.what_fits_basis_serving_title, Res.string.what_fits_basis_serving_sub)
                ).forEach { (basis, title, sub) ->
                    val isSelected = settings.amountBasis == basis
                    val shape = RoundedCornerShape(size = 14.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = shape)
                            .background(color = if (isSelected) colors.accentSoft else colors.surface)
                            .border(width = if (isSelected) 1.5.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { callbacks.onAmountBasis(basis) })
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(size = 20.dp)
                                .background(color = colors.surface, shape = CircleShape)
                                .border(width = if (isSelected) 6.dp else 2.dp, color = if (isSelected) colors.accent else colors.dashed, shape = CircleShape)
                        )
                        Column(modifier = Modifier.weight(weight = 1f)) {
                            Text(text = stringResource(resource = title), style = typography.bold14, color = colors.ink)
                            Text(text = stringResource(resource = sub), style = typography.regular11, color = colors.muted)
                        }
                    }
                }
            }
        }
        ChipGroup(label = stringResource(resource = Res.string.what_fits_when_too_big)) {
            FitTooBig.entries.forEach { tooBig ->
                WhatFitsChoiceChip(
                    text = stringResource(
                        resource = when (tooBig) {
                            FitTooBig.SHRINK -> Res.string.what_fits_too_big_shrink
                            FitTooBig.HIDE -> Res.string.what_fits_too_big_hide
                        }
                    ),
                    isSelected = settings.tooBig == tooBig,
                    onClick = { callbacks.onTooBig(tooBig) }
                )
            }
        }
        ChipGroup(label = stringResource(resource = Res.string.what_fits_put_first)) {
            FitRanking.entries.forEach { ranking ->
                WhatFitsChoiceChip(
                    text = stringResource(
                        resource = when (ranking) {
                            FitRanking.PROTEIN -> Res.string.what_fits_rank_protein
                            FitRanking.CALORIES -> Res.string.what_fits_rank_calories
                            FitRanking.FEWEST_CARBS -> Res.string.what_fits_rank_carbs
                        }
                    ),
                    isSelected = settings.ranking == ranking,
                    onClick = { callbacks.onRanking(ranking) }
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            WhatFitsSectionLabel(text = stringResource(resource = Res.string.what_fits_spare))
            Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                WhatFitsSettings.SPARE_OPTIONS.forEach { calories ->
                    WhatFitsChoiceChip(
                        modifier = Modifier.weight(weight = 1f),
                        text = stringResource(resource = Res.string.kcal, calories),
                        isSelected = settings.spareCalories == calories,
                        onClick = { callbacks.onSpareCalories(calories) }
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            val rows = listOf(
                SwitchRow(Res.string.what_fits_source_recipes_title, Res.string.what_fits_source_recipes_sub, settings.isRecipesOn) {
                    callbacks.onToggleSource(FitSource.RECIPE)
                },
                SwitchRow(Res.string.what_fits_source_my_foods_title, Res.string.what_fits_source_my_foods_sub, settings.isMyFoodsOn) {
                    callbacks.onToggleSource(FitSource.MY_FOOD)
                },
                SwitchRow(Res.string.what_fits_source_recent_title, Res.string.what_fits_source_recent_sub, settings.isRecentOn) {
                    callbacks.onToggleSource(FitSource.RECENT)
                },
                SwitchRow(Res.string.what_fits_skip_today_title, Res.string.what_fits_skip_today_sub, settings.isSkippingEatenToday) {
                    callbacks.onToggleSkipEatenToday()
                }
            )
            rows.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                val label = stringResource(resource = row.title)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 62.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(text = label, style = typography.bold14, color = colors.ink)
                        Text(text = stringResource(resource = row.subtitle), style = typography.regular11, color = colors.muted)
                    }
                    CommonSwitch(isOn = row.isOn, description = label, onToggle = row.onToggle)
                }
            }
        }
    }
}

private data class SwitchRow(
    val title: StringResource,
    val subtitle: StringResource,
    val isOn: Boolean,
    val onToggle: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        WhatFitsSectionLabel(text = label)
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewWhatFitsSettingsScreen() {
    MacroTrackTheme {
        WhatFitsSettingsScreen(settings = WhatFitsSettings(), callbacks = WhatFitsCallbacks.default(), onBack = {})
    }
}
