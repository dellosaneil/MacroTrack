package org.thelazybattley.macrotrack.features.nextday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.next_day_all_footer
import macrotrack.composeapp.generated.resources.next_day_all_sub
import macrotrack.composeapp.generated.resources.next_day_all_time
import macrotrack.composeapp.generated.resources.next_day_all_title
import macrotrack.composeapp.generated.resources.next_day_average
import macrotrack.composeapp.generated.resources.next_day_clear_search
import macrotrack.composeapp.generated.resources.next_day_filter
import macrotrack.composeapp.generated.resources.next_day_filter_all
import macrotrack.composeapp.generated.resources.next_day_food_average
import macrotrack.composeapp.generated.resources.next_day_last_3_months
import macrotrack.composeapp.generated.resources.next_day_last_year
import macrotrack.composeapp.generated.resources.next_day_no_match
import macrotrack.composeapp.generated.resources.next_day_no_match_body
import macrotrack.composeapp.generated.resources.next_day_search_hint
import macrotrack.composeapp.generated.resources.next_day_search_label
import macrotrack.composeapp.generated.resources.next_day_show_more
import macrotrack.composeapp.generated.resources.next_day_showing
import macrotrack.composeapp.generated.resources.next_day_sort
import macrotrack.composeapp.generated.resources.next_day_sort_down
import macrotrack.composeapp.generated.resources.next_day_sort_often
import macrotrack.composeapp.generated.resources.next_day_sort_up
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.DinnerEffect
import org.thelazybattley.macrotrack.domain.model.DinnerSort
import org.thelazybattley.macrotrack.domain.model.NextDayDinner
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.NextDaySettings
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.features.nextday.NEXT_DAY_PAGE
import org.thelazybattley.macrotrack.features.nextday.NextDayAllCallbacks
import org.thelazybattley.macrotrack.features.nextday.NextDayAllViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayAllViewState
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsScaffold
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Every dinner: search by a food in it, filter by what it does, sort, and show more a page at a time. */
@Composable
fun NextDayAllScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<NextDayAllViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    NextDayAllScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onOpenDinner = { dinner -> onNavigate(AppDestinations.Root.NextDayDinner.createRoute(key = dinner.key, period = viewState.period.name)) }
    )
}

private val NextDayPeriod.inText: StringResource
    get() = when (this) {
        NextDayPeriod.MONTHS_3 -> Res.string.next_day_last_3_months
        NextDayPeriod.YEAR -> Res.string.next_day_last_year
        NextDayPeriod.ALL -> Res.string.next_day_all_time
    }

@Composable
private fun NextDayAllScreen(
    viewState: NextDayAllViewState,
    callbacks: NextDayAllCallbacks,
    onBack: () -> Unit,
    onOpenDinner: (NextDayDinner) -> Unit
) {
    val total = viewState.overview?.dinners?.size ?: 0
    WhatFitsScaffold(
        title = stringResource(resource = Res.string.next_day_all_title),
        subtitle = pluralStringResource(resource = Res.plurals.next_day_all_sub, quantity = total, total, stringResource(resource = viewState.period.inText)),
        onBack = onBack
    ) {
        SearchField(query = viewState.query, onQueryChange = callbacks::onQueryChanged)
        FilterChips(viewState = viewState, onSelect = callbacks::onFilterSelected)
        if (viewState.inputs == null) return@WhatFitsScaffold
        viewState.foodAverage?.let { average ->
            val query = viewState.query.trim()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 16.dp))
                    .background(color = colors.hero)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.next_day_food_average, query, average.dinners, average.nights).withBold(query),
                    style = typography.regular13,
                    color = colors.ink,
                    modifier = Modifier.weight(weight = 1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = average.average.signedHundredthsWithUnit(),
                        style = typography.bold18,
                        color = when {
                            average.average >= 0.05 -> colors.danger
                            average.average <= -0.05 -> colors.accentInk
                            else -> colors.ink
                        }
                    )
                    Text(text = stringResource(resource = Res.string.next_day_average), style = typography.regular10, color = colors.muted)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Text(
                text = stringResource(resource = Res.string.next_day_showing, viewState.shown.size, viewState.filtered.size),
                style = typography.bold11,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            NextDaySegmented(
                label = stringResource(resource = Res.string.next_day_sort),
                options = listOf(
                    stringResource(resource = Res.string.next_day_sort_up),
                    stringResource(resource = Res.string.next_day_sort_down),
                    stringResource(resource = Res.string.next_day_sort_often)
                ),
                selected = DinnerSort.entries.indexOf(viewState.sort),
                height = 34,
                isCompact = true,
                onSelect = { callbacks.onSortSelected(sort = DinnerSort.entries[it]) }
            )
        }
        if (viewState.filtered.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 20.dp))
                    .padding(horizontal = 18.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Text(text = stringResource(resource = Res.string.next_day_no_match), style = typography.bold14, color = colors.ink)
                Text(
                    text = stringResource(resource = Res.string.next_day_no_match_body),
                    style = typography.regular13,
                    color = colors.muted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            ) {
                viewState.shown.forEachIndexed { index, dinner ->
                    if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                    DinnerRow(
                        dinner = dinner,
                        unitOf = { name, isRecipe -> viewState.unitOf(name = name, isRecipe = isRecipe) },
                        highlight = viewState.query,
                        onClick = { onOpenDinner(dinner) }
                    )
                }
                if (viewState.hasMore) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                    MoreButton(
                        text = stringResource(
                            resource = Res.string.next_day_show_more,
                            minOf(NEXT_DAY_PAGE, viewState.filtered.size - viewState.shown.size)
                        ),
                        onClick = callbacks::onShowMore
                    )
                }
            }
        }
        Text(
            text = stringResource(resource = Res.string.next_day_all_footer),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.5.dp, color = if (query.isBlank()) colors.line else colors.accent, shape = shape)
            .padding(start = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_search),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 18.dp)
        )
        val label = stringResource(resource = Res.string.next_day_search_label)
        val placeholder = stringResource(resource = Res.string.next_day_search_hint)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = typography.regular13.copy(color = colors.ink),
            cursorBrush = SolidColor(value = colors.accent),
            modifier = Modifier
                .weight(weight = 1f)
                .semantics { contentDescription = label },
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(text = placeholder, style = typography.regular13, color = colors.muted)
                }
                innerTextField()
            }
        )
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(size = 36.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = colors.divider)
                    .clickable(role = Role.Button) { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = stringResource(resource = Res.string.next_day_clear_search),
                    tint = colors.muted,
                    modifier = Modifier.size(size = 14.dp)
                )
            }
        }
    }
}

/** All, then each effect, with how many of the searched dinners have it. */
@Composable
private fun FilterChips(viewState: NextDayAllViewState, onSelect: (DinnerEffect?) -> Unit) {
    val label = stringResource(resource = Res.string.next_day_filter)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(state = rememberScrollState())
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        listOf(null, DinnerEffect.RAISES, DinnerEffect.LOWERS, DinnerEffect.EVEN, DinnerEffect.WAITING).forEach { effect ->
            val isOn = viewState.filter == effect
            Row(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = if (isOn) colors.ink else colors.surface)
                    .border(width = 1.5.dp, color = if (isOn) colors.ink else colors.line, shape = RoundedCornerShape(size = 20.dp))
                    .toggleable(value = isOn, role = Role.Checkbox, onValueChange = { onSelect(effect) })
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                if (effect != null) EffectDot(effect = effect, size = 8)
                Text(
                    text = stringResource(resource = effect?.label ?: Res.string.next_day_filter_all),
                    style = typography.bold13,
                    color = if (isOn) colors.background else colors.ink
                )
                Text(
                    text = viewState.count(effect = effect).toString(),
                    style = typography.medium13,
                    color = (if (isOn) colors.background else colors.ink).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewNextDayAllScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val (wings, wingWeights) = previewDinners(today = today, foods = listOf("Chicken wings" to 720, "Rice" to 325), daysAgo = listOf(2, 9, 16, 23), change = 1.1)
    val (adobo, adoboWeights) = previewDinners(today = today, foods = listOf("Pork adobo" to 600, "Rice" to 325), daysAgo = listOf(4, 11, 18), change = 0.9)
    MacroTrackTheme {
        NextDayAllScreen(
            viewState = NextDayAllViewState(
                today = today,
                inputs = NextDayInputs(items = wings + adobo, weights = (wingWeights + adoboWeights).distinctBy { it.date }, settings = NextDaySettings()),
                query = "rice"
            ),
            callbacks = NextDayAllCallbacks.default(),
            onBack = {},
            onOpenDinner = {}
        )
    }
}
