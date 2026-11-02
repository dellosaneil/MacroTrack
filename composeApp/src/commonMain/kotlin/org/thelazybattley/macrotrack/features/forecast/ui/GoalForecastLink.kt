package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.forecast_link_flat
import macrotrack.composeapp.generated.resources.forecast_link_no_goal
import macrotrack.composeapp.generated.resources.forecast_link_on_the_way
import macrotrack.composeapp.generated.resources.forecast_link_reached
import macrotrack.composeapp.generated.resources.forecast_link_wrong_way
import macrotrack.composeapp.generated.resources.forecast_title
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_target
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.FORECAST_MIN_WEIGH_INS
import org.thelazybattley.macrotrack.domain.model.ForecastStatus
import org.thelazybattley.macrotrack.domain.model.GoalForecast
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import macrotrack.composeapp.generated.resources.forecast_link_collecting

/** The Goal forecast row under Moving average on the weight chart, with the date or why there isn't one. */
@Composable
fun GoalForecastLink(forecast: GoalForecast?, onClick: () -> Unit) {
    val subtitle = when (val status = forecast?.status) {
        null -> stringResource(resource = Res.string.forecast_link_no_goal)
        is ForecastStatus.OnTheWay -> stringResource(resource = Res.string.forecast_link_on_the_way, status.date.forecastDate(today = forecast.today))
        is ForecastStatus.Collecting -> {
            val left = (FORECAST_MIN_WEIGH_INS - forecast.weighIns).coerceAtLeast(minimumValue = 1)
            pluralStringResource(resource = Res.plurals.forecast_link_collecting, quantity = left, left)
        }
        ForecastStatus.Flat -> stringResource(resource = Res.string.forecast_link_flat)
        ForecastStatus.WrongWay -> stringResource(resource = Res.string.forecast_link_wrong_way)
        is ForecastStatus.Reached -> stringResource(resource = Res.string.forecast_link_reached, status.date.forecastDate(today = forecast.today))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.nightSoft)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .background(color = colors.surface, shape = RoundedCornerShape(size = 10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_target),
                contentDescription = null,
                tint = colors.nightInk,
                modifier = Modifier.size(size = 20.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = stringResource(resource = Res.string.forecast_title), style = typography.bold14, color = colors.ink)
            Text(text = subtitle, style = typography.regular11, color = colors.muted)
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}
