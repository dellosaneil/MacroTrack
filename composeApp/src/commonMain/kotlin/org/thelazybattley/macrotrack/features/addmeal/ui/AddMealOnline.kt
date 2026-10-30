package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_value
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.fiber
import macrotrack.composeapp.generated.resources.grams_value
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_cloud_alert
import macrotrack.composeapp.generated.resources.ic_globe
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_warning
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.milligrams_value
import macrotrack.composeapp.generated.resources.online_add_to_meal
import macrotrack.composeapp.generated.resources.online_added_by_user
import macrotrack.composeapp.generated.resources.online_badge_brand
import macrotrack.composeapp.generated.resources.online_badge_community
import macrotrack.composeapp.generated.resources.online_barcode
import macrotrack.composeapp.generated.resources.online_caption
import macrotrack.composeapp.generated.resources.online_caption_offline
import macrotrack.composeapp.generated.resources.online_community_missing
import macrotrack.composeapp.generated.resources.online_community_warning
import macrotrack.composeapp.generated.resources.online_count
import macrotrack.composeapp.generated.resources.online_fix
import macrotrack.composeapp.generated.resources.online_for_portion
import macrotrack.composeapp.generated.resources.online_header
import macrotrack.composeapp.generated.resources.online_none
import macrotrack.composeapp.generated.resources.online_notice_saved
import macrotrack.composeapp.generated.resources.online_notice_selected
import macrotrack.composeapp.generated.resources.online_nutrient_carbs
import macrotrack.composeapp.generated.resources.online_nutrient_fat
import macrotrack.composeapp.generated.resources.online_nutrient_fiber
import macrotrack.composeapp.generated.resources.online_nutrient_protein
import macrotrack.composeapp.generated.resources.online_nutrient_sodium
import macrotrack.composeapp.generated.resources.online_nutrient_sugar
import macrotrack.composeapp.generated.resources.online_offline_body
import macrotrack.composeapp.generated.resources.online_offline_title
import macrotrack.composeapp.generated.resources.online_open
import macrotrack.composeapp.generated.resources.online_per
import macrotrack.composeapp.generated.resources.online_privacy
import macrotrack.composeapp.generated.resources.online_save_only
import macrotrack.composeapp.generated.resources.online_searching
import macrotrack.composeapp.generated.resources.online_serving
import macrotrack.composeapp.generated.resources.online_show_more
import macrotrack.composeapp.generated.resources.online_too_short
import macrotrack.composeapp.generated.resources.online_try_again
import macrotrack.composeapp.generated.resources.online_two_servings
import macrotrack.composeapp.generated.resources.online_view_source
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.remove_value
import macrotrack.composeapp.generated.resources.search_scope_all
import macrotrack.composeapp.generated.resources.search_scope_library
import macrotrack.composeapp.generated.resources.search_scope_online
import macrotrack.composeapp.generated.resources.search_scope_yours
import macrotrack.composeapp.generated.resources.sodium
import macrotrack.composeapp.generated.resources.sugar
import macrotrack.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.OnlineFood
import org.thelazybattley.macrotrack.domain.model.OnlineFoodSource
import org.thelazybattley.macrotrack.domain.model.OnlineNutrient
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.addmeal.OnlineNotice
import org.thelazybattley.macrotrack.features.addmeal.OnlineStatus
import org.thelazybattley.macrotrack.features.addmeal.SearchScope
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.formatPortion
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

private const val NOTICE_MILLIS = 4_000L
private const val SKELETON_ROWS = 3

private val SearchScope.title: StringResource
    get() = when (this) {
        SearchScope.ALL -> Res.string.search_scope_all
        SearchScope.YOURS -> Res.string.search_scope_yours
        SearchScope.LIBRARY -> Res.string.search_scope_library
        SearchScope.ONLINE -> Res.string.search_scope_online
    }

/** While searching, picks which group of results shows. */
@Composable
fun SearchScopeChips(modifier: Modifier = Modifier, selected: SearchScope, onSelected: (SearchScope) -> Unit) {
    Row(
        modifier = modifier.horizontalScroll(state = rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        SearchScope.entries.forEach { scope ->
            val isOn = scope == selected
            val shape = RoundedCornerShape(size = 17.dp)
            Text(
                text = stringResource(resource = scope.title),
                style = typography.bold13,
                color = if (isOn) colors.accentInk else colors.ink,
                modifier = Modifier
                    .height(height = 34.dp)
                    .clip(shape = shape)
                    .background(color = if (isOn) colors.accentSoft else colors.surface)
                    .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = shape)
                    .semantics { this.selected = isOn }
                    .clickable(role = Role.Tab) { onSelected(scope) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

/** The Online group of search results: searching, offline, the foods found, or none. */
fun LazyListScope.onlineResults(viewState: AddMealViewState, callbacks: AddMealCallbacks) {
    val online = viewState.online
    item(key = "online_header") {
        OnlineGroupHeader(
            count = online.foods.size,
            total = online.totalCount,
            isDone = online.status == OnlineStatus.DONE || online.status == OnlineStatus.LOADING_MORE,
            isOffline = online.status == OnlineStatus.OFFLINE
        )
    }
    when (online.status) {
        OnlineStatus.IDLE -> item(key = "online_short") {
            Text(
                text = pluralStringResource(
                    resource = Res.plurals.online_too_short,
                    quantity = AddMealViewState.ONLINE_MIN_QUERY,
                    AddMealViewState.ONLINE_MIN_QUERY
                ),
                style = typography.regular13,
                color = colors.muted
            )
        }
        OnlineStatus.LOADING -> {
            item(key = "online_loading") {
                Text(
                    text = stringResource(resource = Res.string.online_searching),
                    style = typography.regular11,
                    color = colors.muted,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            items(count = SKELETON_ROWS, key = { "online_skeleton_$it" }) { index -> SkeletonRow(index = index) }
        }
        OnlineStatus.OFFLINE -> item(key = "online_offline") {
            OfflineCard(onRetry = callbacks::onRetryOnline)
        }
        OnlineStatus.DONE, OnlineStatus.LOADING_MORE -> {
            if (online.foods.isEmpty()) {
                item(key = "online_none") {
                    Text(
                        text = stringResource(resource = Res.string.online_none, online.query),
                        style = typography.regular13,
                        color = colors.muted
                    )
                }
            }
            items(items = online.foods, key = { "online:${it.barcode}" }) { food ->
                OnlineFoodRow(
                    food = food,
                    isSelected = viewState.isOnlineSelected(food = food),
                    onOpen = { callbacks.onOpenOnlineFood(food = food) },
                    onAdd = { callbacks.onAddOnlineFood(food = food) }
                )
            }
            if (online.canShowMore || online.status == OnlineStatus.LOADING_MORE) {
                item(key = "online_more") {
                    val shape = RoundedCornerShape(size = 12.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = 44.dp)
                            .clip(shape = shape)
                            .border(width = 1.dp, color = colors.line, shape = shape)
                            .clickable(role = Role.Button, enabled = online.canShowMore, onClick = callbacks::onShowMoreOnline),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                resource = if (online.status == OnlineStatus.LOADING_MORE) Res.string.online_searching else Res.string.online_show_more
                            ),
                            style = typography.bold14,
                            color = colors.waterInk
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnlinePrivacyNote(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(resource = Res.string.online_privacy),
        style = typography.regular11,
        color = colors.muted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

@Composable
private fun OnlineGroupHeader(count: Int, total: Int, isDone: Boolean, isOffline: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        HorizontalDivider(thickness = 1.dp, color = colors.line, modifier = Modifier.padding(bottom = 12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_globe),
                contentDescription = null,
                tint = colors.waterInk,
                modifier = Modifier.size(size = 14.dp)
            )
            Text(
                text = stringResource(resource = Res.string.online_header),
                style = typography.bold13,
                color = colors.waterInk,
                modifier = Modifier.padding(start = 6.dp).weight(weight = 1f)
            )
            if (isDone && count > 0) {
                Text(
                    text = stringResource(resource = Res.string.online_count, count, total.withThousands()),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        Text(
            text = stringResource(resource = if (isOffline) Res.string.online_caption_offline else Res.string.online_caption),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun SkeletonRow(index: Int) {
    val widths = listOf(0.7f, 0.55f, 0.62f)
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clearAndSetSemantics {}
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = widths[index % widths.size])
                    .height(height = 12.dp)
                    .background(color = colors.divider, shape = RoundedCornerShape(size = 6.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = widths[index % widths.size] - 0.2f)
                    .height(height = 10.dp)
                    .background(color = colors.divider, shape = RoundedCornerShape(size = 5.dp))
            )
        }
        Box(modifier = Modifier.size(size = 40.dp).background(color = colors.divider, shape = RoundedCornerShape(size = 12.dp)))
    }
}

@Composable
private fun OfflineCard(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.warmSoft)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(all = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_cloud_alert),
            contentDescription = null,
            tint = colors.warmInk,
            modifier = Modifier.size(size = 20.dp)
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Column {
                Text(text = stringResource(resource = Res.string.online_offline_title), style = typography.bold14, color = colors.warmInk)
                Text(text = stringResource(resource = Res.string.online_offline_body), style = typography.regular13, color = colors.warmInk)
            }
            Text(
                text = stringResource(resource = Res.string.online_try_again),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .clickable(role = Role.Button, onClick = onRetry)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun SourceBadge(source: OnlineFoodSource) {
    val isBrand = source == OnlineFoodSource.BRAND
    Text(
        text = stringResource(resource = if (isBrand) Res.string.online_badge_brand else Res.string.online_badge_community),
        style = typography.bold10,
        color = if (isBrand) colors.waterInk else colors.warmInk,
        maxLines = 1,
        modifier = Modifier
            .background(color = if (isBrand) colors.waterSoft else colors.warmSoft, shape = RoundedCornerShape(size = 6.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}

@Composable
private fun OnlineFoodRow(food: OnlineFood, isSelected: Boolean, onOpen: () -> Unit, onAdd: () -> Unit) {
    val shape = RoundedCornerShape(size = 16.dp)
    val calories = food.food.macros.calories.withThousands()
    val brand = food.brand ?: stringResource(resource = Res.string.online_added_by_user)
    val per = stringResource(resource = Res.string.online_per, formatPortion(amount = OnlineFood.PER_100, storedUnit = food.food.unit))
    val openLabel = stringResource(resource = Res.string.online_open, food.food.name, calories)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width = 5.dp)
                .fillMaxHeight()
                .background(color = food.food.dominantMacro.stripeColor())
        )
        Row(
            modifier = Modifier
                .weight(weight = 1f)
                .clickable(role = Role.Button, onClick = onOpen)
                .clearAndSetSemantics { contentDescription = openLabel }
                .padding(start = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                Text(
                    text = food.food.name,
                    style = typography.bold15,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                    SourceBadge(source = food.source)
                    Text(
                        text = "$brand · $per",
                        style = typography.regular11,
                        color = colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = calories, style = typography.bold15, color = colors.ink)
                Text(text = stringResource(resource = Res.string.kcal_text), style = typography.regular10, color = colors.muted)
            }
        }
        Box(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = if (isSelected) colors.accent else colors.accentSoft)
                .clickable(role = Role.Button, onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = if (isSelected) Res.drawable.ic_check else Res.drawable.ic_plus),
                contentDescription = stringResource(resource = if (isSelected) Res.string.remove_value else Res.string.add_value, food.food.name),
                tint = if (isSelected) colors.onAccent else colors.accentInk,
                modifier = Modifier.size(size = 18.dp)
            )
        }
    }
}

private fun FoodMacros.scaled(amount: Double): FoodMacros {
    val fraction = amount / OnlineFood.PER_100
    return FoodMacros(
        calories = (calories * fraction).roundToInt(),
        protein = protein * fraction,
        carbs = carbs * fraction,
        fat = fat * fraction
    )
}

private val OnlineNutrient.label: StringResource
    get() = when (this) {
        OnlineNutrient.PROTEIN -> Res.string.online_nutrient_protein
        OnlineNutrient.CARBS -> Res.string.online_nutrient_carbs
        OnlineNutrient.FAT -> Res.string.online_nutrient_fat
        OnlineNutrient.FIBER -> Res.string.online_nutrient_fiber
        OnlineNutrient.SUGAR -> Res.string.online_nutrient_sugar
        OnlineNutrient.SODIUM -> Res.string.online_nutrient_sodium
    }

/**
 * An online food to check before using it: portions, calories and macros, the other nutrients, where it comes from.
 * It can be picked for the meal, saved to My foods, or saved and opened to fix its numbers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineFoodSheet(
    food: OnlineFood,
    mealTitle: String,
    onAdd: (amount: Double) -> Unit,
    onSave: () -> Unit,
    onFix: () -> Unit,
    onDismiss: () -> Unit
) {
    val serving = food.servingGrams
    val unit = food.food.unit
    val servingText = serving?.let { food.servingLabel ?: formatPortion(amount = it, storedUnit = unit) }
    val portions = buildList {
        if (serving != null) add(serving to stringResource(resource = Res.string.online_serving, servingText.orEmpty()))
        add(OnlineFood.PER_100 to formatPortion(amount = OnlineFood.PER_100, storedUnit = unit))
        if (serving != null) add(serving * 2 to stringResource(resource = Res.string.online_two_servings))
    }
    // The serving arrives a moment after the sheet opens; it starts on whatever comes first.
    var picked by remember(key1 = portions.size) { mutableIntStateOf(value = 0) }
    val amount = portions[picked.coerceIn(minimumValue = 0, maximumValue = portions.lastIndex)].first
    val macros = food.food.macros.scaled(amount = amount)
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 40.dp, height = 5.dp)
                    .background(color = colors.dashed, shape = RoundedCornerShape(size = 3.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { paneTitle = food.food.name }
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    Text(text = food.food.name, style = typography.bold24, color = colors.ink)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                        SourceBadge(source = food.source)
                        Text(
                            text = listOfNotNull(
                                food.brand ?: stringResource(resource = Res.string.online_added_by_user),
                                stringResource(resource = Res.string.online_barcode, food.barcode)
                            ).joinToString(separator = " · "),
                            style = typography.regular11,
                            color = colors.muted
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(size = 40.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.surface)
                        .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                        .clickable(role = Role.Button, onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_close),
                        contentDescription = stringResource(resource = Res.string.close),
                        tint = colors.ink,
                        modifier = Modifier.size(size = 18.dp)
                    )
                }
            }
            if (food.source == OnlineFoodSource.COMMUNITY) {
                val missing = food.missing.map { stringResource(resource = it.label) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 14.dp))
                        .padding(all = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_warning),
                        contentDescription = null,
                        tint = colors.warmInk,
                        modifier = Modifier.size(size = 18.dp)
                    )
                    Text(
                        text = if (missing.isEmpty()) {
                            stringResource(resource = Res.string.online_community_warning)
                        } else {
                            stringResource(resource = Res.string.online_community_missing, missing.joinToString(separator = ", "))
                        },
                        style = typography.regular13,
                        color = colors.warmInk
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                portions.forEachIndexed { index, (_, label) ->
                    val isOn = index == picked
                    val shape = RoundedCornerShape(size = 10.dp)
                    Box(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .height(height = 40.dp)
                            .clip(shape = shape)
                            .background(color = if (isOn) colors.accentSoft else colors.surface)
                            .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = shape)
                            .semantics { selected = isOn }
                            .clickable(role = Role.Tab) { picked = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = typography.bold13,
                            color = if (isOn) colors.accentInk else colors.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            }
            NutritionCard(food = food, amount = amount, macros = macros)
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 52.dp)
                        .clip(shape = RoundedCornerShape(size = 16.dp))
                        .background(color = colors.accent)
                        .clickable(role = Role.Button) { onAdd(amount) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resource = Res.string.online_add_to_meal, mealTitle, macros.calories.withThousands()),
                        style = typography.bold15,
                        color = colors.onAccent
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    SheetAction(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.online_save_only), onClick = onSave)
                    SheetAction(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.online_fix), onClick = onFix)
                }
                Text(
                    text = stringResource(resource = Res.string.online_view_source),
                    style = typography.bold13,
                    color = colors.waterInk,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .clickable(role = Role.Button) { uriHandler.openUri(uri = "https://world.openfoodfacts.org/product/${food.barcode}") }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun NutritionCard(food: OnlineFood, amount: Double, macros: FoodMacros) {
    val unit = food.food.unit
    val fraction = amount / OnlineFood.PER_100
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Text(text = macros.calories.withThousands(), style = typography.bold24, color = colors.ink)
            Text(text = stringResource(resource = Res.string.kcal_text), style = typography.regular13, color = colors.muted)
            Text(
                text = stringResource(
                    resource = Res.string.online_for_portion,
                    formatPortion(amount = amount, storedUnit = unit),
                    food.food.macros.calories,
                    formatPortion(amount = OnlineFood.PER_100, storedUnit = unit)
                ),
                style = typography.regular11,
                color = colors.muted,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(weight = 1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            listOf(
                Triple(Res.string.protein, macros.protein, colors.protein),
                Triple(Res.string.carbs, macros.carbs, colors.carbs),
                Triple(Res.string.fat, macros.fat, colors.fat)
            ).forEach { (label, grams, dot) ->
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .background(color = colors.background, shape = RoundedCornerShape(size = 12.dp))
                        .semantics(mergeDescendants = true) {}
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                        Box(modifier = Modifier.size(size = 7.dp).background(color = dot, shape = RoundedCornerShape(size = 2.dp)))
                        Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted)
                    }
                    Text(
                        text = stringResource(resource = Res.string.grams_value, ((grams * 10).roundToInt() / 10.0).toCleanString()),
                        style = typography.bold16,
                        color = colors.ink
                    )
                }
            }
        }
        val dash = "–"
        val extras = listOf(
            stringResource(resource = Res.string.fiber) to (food.fiber?.let {
                stringResource(resource = Res.string.grams_value, ((it * fraction * 10).roundToInt() / 10.0).toCleanString())
            } ?: dash),
            stringResource(resource = Res.string.sugar) to (food.sugar?.let {
                stringResource(resource = Res.string.grams_value, ((it * fraction * 10).roundToInt() / 10.0).toCleanString())
            } ?: dash),
            stringResource(resource = Res.string.sodium) to (food.sodium?.let {
                stringResource(resource = Res.string.milligrams_value, (it * fraction).roundToInt().toString())
            } ?: dash)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            extras.forEach { (label, value) ->
                Text(
                    text = "$label $value",
                    style = typography.regular11,
                    color = colors.muted,
                    modifier = Modifier
                        .background(color = colors.background, shape = RoundedCornerShape(size = 8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SheetAction(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Box(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold13, color = colors.ink, textAlign = TextAlign.Center, maxLines = 2)
    }
}

/** What was just done with an online food, with Undo; it goes after a few seconds. */
@Composable
fun OnlineNoticeBar(modifier: Modifier = Modifier, notice: OnlineNotice, mealTitle: String, onUndo: () -> Unit, onDismiss: () -> Unit) {
    LaunchedEffect(key1 = notice.id) {
        delay(timeMillis = NOTICE_MILLIS)
        onDismiss()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.ink)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = if (notice.isSelected) {
                stringResource(resource = Res.string.online_notice_selected, notice.foodName, mealTitle)
            } else {
                stringResource(resource = Res.string.online_notice_saved, notice.foodName)
            },
            style = typography.regular13,
            color = colors.background,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = stringResource(resource = Res.string.undo),
            style = typography.bold14,
            color = colors.background,
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onUndo)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
