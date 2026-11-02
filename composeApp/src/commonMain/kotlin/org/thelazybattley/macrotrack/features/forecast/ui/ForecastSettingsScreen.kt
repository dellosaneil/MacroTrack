package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.forecast_basis_from_calories
import macrotrack.composeapp.generated.resources.forecast_basis_from_calories_sub
import macrotrack.composeapp.generated.resources.forecast_basis_last_2_weeks
import macrotrack.composeapp.generated.resources.forecast_basis_last_2_weeks_sub
import macrotrack.composeapp.generated.resources.forecast_basis_last_4_weeks
import macrotrack.composeapp.generated.resources.forecast_basis_last_4_weeks_sub
import macrotrack.composeapp.generated.resources.forecast_basis_not_enough
import macrotrack.composeapp.generated.resources.forecast_basis_pace_sub
import macrotrack.composeapp.generated.resources.forecast_basis_since_start
import macrotrack.composeapp.generated.resources.forecast_basis_since_start_sub
import macrotrack.composeapp.generated.resources.forecast_dash
import macrotrack.composeapp.generated.resources.forecast_display
import macrotrack.composeapp.generated.resources.forecast_settings_title
import macrotrack.composeapp.generated.resources.forecast_show_range
import macrotrack.composeapp.generated.resources.forecast_show_range_sub
import macrotrack.composeapp.generated.resources.forecast_work_out_pace_from
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.ForecastBasis
import org.thelazybattley.macrotrack.features.forecast.GoalForecastCallbacks
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewModel
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewState
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ForecastSettingsScreen(onPopBackStack: () -> Unit) {
    val viewModel = koinViewModel<GoalForecastViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ForecastSettingsScreen(viewState = viewState, callbacks = viewModel, onBack = onPopBackStack)
}

@Composable
private fun ForecastSettingsScreen(
    viewState: GoalForecastViewState,
    callbacks: GoalForecastCallbacks,
    onBack: () -> Unit
) {
    ForecastScaffold(title = Res.string.forecast_settings_title, onBack = onBack) {
        val snapshot = viewState.snapshot ?: return@ForecastScaffold
        val forecast = snapshot.forecast ?: return@ForecastScaffold
        ForecastSectionLabel(text = stringResource(resource = Res.string.forecast_work_out_pace_from))
        Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            ForecastBasis.entries.forEach { basis ->
                val (title, description) = when (basis) {
                    ForecastBasis.LAST_2_WEEKS -> Res.string.forecast_basis_last_2_weeks to stringResource(resource = Res.string.forecast_basis_last_2_weeks_sub)
                    ForecastBasis.LAST_4_WEEKS -> Res.string.forecast_basis_last_4_weeks to stringResource(resource = Res.string.forecast_basis_last_4_weeks_sub)
                    ForecastBasis.SINCE_START -> Res.string.forecast_basis_since_start to stringResource(resource = Res.string.forecast_basis_since_start_sub)
                    ForecastBasis.FROM_CALORIES -> Res.string.forecast_basis_from_calories to
                        stringResource(resource = Res.string.forecast_basis_from_calories_sub, snapshot.maintenance.withThousands())
                }
                val pace = forecast.paceByBasis[basis]
                val isSelected = forecast.basis == basis
                val shape = RoundedCornerShape(size = 14.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = shape)
                        .background(color = if (isSelected) colors.accentSoft else colors.surface)
                        .border(width = if (isSelected) 1.5.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                        .selectable(selected = isSelected, enabled = pace != null, role = Role.RadioButton, onClick = { callbacks.onBasisSelect(basis) })
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    ForecastRadioDot(isSelected = isSelected)
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(text = stringResource(resource = title), style = typography.bold14, color = if (pace != null) colors.ink else colors.faint)
                        Text(
                            text = if (pace != null) {
                                stringResource(
                                    resource = Res.string.forecast_basis_pace_sub,
                                    description,
                                    pace.signedWeekly(direction = if (pace < 0) -1 else 1),
                                    weightUnitLabel()
                                )
                            } else {
                                stringResource(resource = Res.string.forecast_basis_not_enough, description)
                            },
                            style = typography.regular11,
                            color = colors.muted
                        )
                    }
                    Text(
                        text = forecast.dateFor(basis = basis)?.forecastDate(today = forecast.today) ?: stringResource(resource = Res.string.forecast_dash),
                        style = typography.bold13,
                        color = colors.muted
                    )
                }
            }
        }
        ForecastSectionLabel(text = stringResource(resource = Res.string.forecast_display))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            ForecastSwitchRow(
                label = stringResource(resource = Res.string.forecast_show_range),
                subtitle = stringResource(resource = Res.string.forecast_show_range_sub),
                isOn = snapshot.settings.isRangeShown,
                onToggle = callbacks::onRangeShownToggle
            )
        }
    }
}
