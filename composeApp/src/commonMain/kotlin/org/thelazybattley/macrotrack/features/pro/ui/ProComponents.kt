package org.thelazybattley.macrotrack.features.pro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_lock
import macrotrack.composeapp.generated.resources.pro_badge
import macrotrack.composeapp.generated.resources.pro_banner_days_left
import macrotrack.composeapp.generated.resources.pro_banner_days_left_body
import macrotrack.composeapp.generated.resources.pro_banner_ended
import macrotrack.composeapp.generated.resources.pro_banner_ended_body
import macrotrack.composeapp.generated.resources.pro_dismiss
import macrotrack.composeapp.generated.resources.pro_locked_card_action
import macrotrack.composeapp.generated.resources.pro_upgrade
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.FREE_RECIPE_LIMIT
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Trial warnings start this many days before Pro locks. */
private const val TRIAL_WARNING_DAYS = 5

/** A small "PRO" tag for controls that open the paywall once the trial is over. */
@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 6.dp))
            .background(color = colors.warmSoft)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 3.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_lock),
            contentDescription = null,
            tint = colors.warmInk,
            modifier = Modifier.size(size = 10.dp)
        )
        Text(
            text = stringResource(resource = Res.string.pro_badge),
            style = typography.bold9,
            color = colors.warmInk,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Shown in place of a Pro card once the trial is over; the card's data is kept and comes back with Pro. */
@Composable
fun ProLockedCard(
    modifier: Modifier = Modifier,
    feature: ProFeature,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Text(
                text = stringResource(resource = feature.title),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            ProBadge()
        }
        Text(
            text = stringResource(resource = feature.body, FREE_RECIPE_LIMIT),
            style = typography.regular13,
            color = colors.muted
        )
        Text(
            text = stringResource(resource = Res.string.pro_locked_card_action),
            style = typography.bold13,
            color = colors.accentInk
        )
    }
}

/**
 * Warns in the last days of the trial, then says once that it ended until dismissed. Shows nothing otherwise.
 */
@Composable
fun ProTrialBanner(modifier: Modifier = Modifier) {
    val pro = LocalProAccess.current
    val status = pro.state.status
    when {
        status.isTrialActive && status.trialDaysLeft <= TRIAL_WARNING_DAYS -> ProBanner(
            modifier = modifier,
            title = pluralStringResource(
                resource = Res.plurals.pro_banner_days_left,
                quantity = status.trialDaysLeft,
                status.trialDaysLeft
            ),
            body = stringResource(resource = Res.string.pro_banner_days_left_body),
            onUpgrade = { pro.openPaywall(null) },
            onDismiss = null
        )
        status.hasTrialEnded && !pro.state.isTrialEndedNoticeDismissed -> ProBanner(
            modifier = modifier,
            title = stringResource(resource = Res.string.pro_banner_ended),
            body = stringResource(resource = Res.string.pro_banner_ended_body),
            onUpgrade = { pro.openPaywall(null) },
            onDismiss = pro.callbacks::onTrialEndedNoticeDismissed
        )
    }
}

@Composable
private fun ProBanner(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    onUpgrade: () -> Unit,
    onDismiss: (() -> Unit)?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.warmSoft)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = if (onDismiss == null) 14.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Text(text = title, style = typography.bold13, color = colors.warmInk)
            Text(text = body, style = typography.regular11, color = colors.warmInk)
        }
        Text(
            text = stringResource(resource = Res.string.pro_upgrade),
            style = typography.bold13,
            color = colors.onAccent,
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(color = colors.accent)
                .clickable(role = Role.Button, onClick = onUpgrade)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        if (onDismiss != null) {
            Box(
                modifier = Modifier
                    .size(size = 36.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .clickable(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = stringResource(resource = Res.string.pro_dismiss),
                    tint = colors.warmInk,
                    modifier = Modifier.size(size = 14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProLockedCard() {
    MacroTrackTheme {
        Column(modifier = Modifier.padding(all = 16.dp), verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            ProBadge()
            ProLockedCard(feature = ProFeature.INSIGHTS, onClick = {})
            ProBanner(title = "3 days of Pro left", body = "Keep the moving average.", onUpgrade = {}, onDismiss = {})
        }
    }
}
