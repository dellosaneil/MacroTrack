package org.thelazybattley.macrotrack.features.pro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_lock
import macrotrack.composeapp.generated.resources.pro_locked_title
import macrotrack.composeapp.generated.resources.pro_message_nothing_to_restore
import macrotrack.composeapp.generated.resources.pro_message_pending
import macrotrack.composeapp.generated.resources.pro_message_restored
import macrotrack.composeapp.generated.resources.pro_message_unavailable
import macrotrack.composeapp.generated.resources.pro_message_unlocked
import macrotrack.composeapp.generated.resources.pro_name
import macrotrack.composeapp.generated.resources.pro_one_time
import macrotrack.composeapp.generated.resources.pro_owned
import macrotrack.composeapp.generated.resources.pro_restore
import macrotrack.composeapp.generated.resources.pro_store_unsupported
import macrotrack.composeapp.generated.resources.pro_subtitle
import macrotrack.composeapp.generated.resources.pro_trial_days_left
import macrotrack.composeapp.generated.resources.pro_trial_ended
import macrotrack.composeapp.generated.resources.pro_unlock
import macrotrack.composeapp.generated.resources.pro_unlock_with_price
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.FREE_RECIPE_LIMIT
import org.thelazybattley.macrotrack.domain.model.ProStatus
import org.thelazybattley.macrotrack.features.pro.ProCallbacks
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ProMessage
import org.thelazybattley.macrotrack.features.pro.ProViewState
import org.thelazybattley.macrotrack.features.pro.offeredProFeatures
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** What Pro adds and how to buy it. [feature] is the locked control that led here, if any; it is listed first. */
@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    feature: ProFeature?,
    viewState: ProViewState,
    callbacks: ProCallbacks,
    onBack: () -> Unit
) {
    DisposableEffect(key1 = Unit) {
        onDispose { callbacks.onMessageShown() }
    }
    val isOwned = viewState.status.isPurchased
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = { CommonScreenHeader(title = Res.string.pro_name, onBack = onBack) },
        bottomBar = {
            if (!isOwned) {
                PurchaseBar(viewState = viewState, callbacks = callbacks)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 24.dp))
                    .background(color = colors.hero)
                    .padding(all = 20.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 44.dp)
                        .background(color = colors.surface, shape = RoundedCornerShape(size = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(resource = if (isOwned) Res.drawable.ic_check else Res.drawable.ic_lock),
                        contentDescription = null,
                        tint = colors.accentInk,
                        modifier = Modifier.size(size = 22.dp)
                    )
                }
                Text(
                    text = if (feature != null && !isOwned) {
                        stringResource(resource = Res.string.pro_locked_title, stringResource(resource = feature.title))
                    } else {
                        stringResource(resource = Res.string.pro_name)
                    },
                    style = typography.bold24,
                    color = colors.ink
                )
                Text(
                    text = stringResource(resource = Res.string.pro_subtitle),
                    style = typography.regular13,
                    color = colors.muted
                )
            }
            StatusLine(status = viewState.status)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
                    .padding(vertical = 6.dp)
            ) {
                offeredProFeatures(feature = feature)
                    .sortedByDescending { it == feature }
                    .forEach { item -> FeatureRow(feature = item, isHighlighted = item == feature && !isOwned) }
            }
            viewState.message?.let { message ->
                Text(
                    text = stringResource(resource = message.text),
                    style = typography.bold13,
                    color = colors.accentInk,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatusLine(status: ProStatus) {
    val text = when {
        status.isPurchased -> stringResource(resource = Res.string.pro_owned)
        status.isTrialActive -> pluralStringResource(
            resource = Res.plurals.pro_trial_days_left,
            quantity = status.trialDaysLeft,
            status.trialDaysLeft
        )
        status.hasTrialEnded -> stringResource(resource = Res.string.pro_trial_ended)
        else -> return
    }
    Text(
        text = text,
        style = typography.bold13,
        color = if (status.isPurchased) colors.accentInk else colors.warmInk,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = if (status.isPurchased) colors.accentSoft else colors.warmSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

@Composable
private fun FeatureRow(feature: ProFeature, isHighlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = if (isHighlighted) colors.accentSoft else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_check),
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.padding(top = 2.dp).size(size = 18.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = stringResource(resource = feature.title), style = typography.bold14, color = colors.ink)
            Text(
                text = stringResource(resource = feature.body, FREE_RECIPE_LIMIT),
                style = typography.regular13,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun PurchaseBar(viewState: ProViewState, callbacks: ProCallbacks) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = viewState.price?.let { stringResource(resource = Res.string.pro_unlock_with_price, it) }
                ?: stringResource(resource = Res.string.pro_unlock),
            enabled = viewState.isStoreSupported && !viewState.isBusy,
            onClick = callbacks::onUnlockClicked
        )
        if (viewState.isStoreSupported) {
            Text(
                text = stringResource(resource = Res.string.pro_one_time),
                style = typography.regular11,
                color = colors.muted
            )
            Text(
                text = stringResource(resource = Res.string.pro_restore),
                style = typography.bold13,
                color = colors.accentInk,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .clickable(enabled = !viewState.isBusy, role = Role.Button, onClick = callbacks::onRestoreClicked)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        } else {
            Text(
                text = stringResource(resource = Res.string.pro_store_unsupported),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

private val ProMessage.text
    get() = when (this) {
        ProMessage.UNAVAILABLE -> Res.string.pro_message_unavailable
        ProMessage.PENDING -> Res.string.pro_message_pending
        ProMessage.UNLOCKED -> Res.string.pro_message_unlocked
        ProMessage.RESTORED -> Res.string.pro_message_restored
        ProMessage.NOTHING_TO_RESTORE -> Res.string.pro_message_nothing_to_restore
    }

@Preview(showBackground = true)
@Composable
private fun PreviewPaywallScreen() {
    MacroTrackTheme {
        PaywallScreen(
            feature = ProFeature.MOVING_AVERAGE,
            viewState = ProViewState(
                status = ProStatus(trialStart = LocalDate(2026, 9, 1), today = LocalDate(2026, 10, 15)),
                price = "$4.99"
            ),
            callbacks = ProCallbacks.default(),
            onBack = {}
        )
    }
}
