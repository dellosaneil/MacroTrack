package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_count
import macrotrack.composeapp.generated.resources.all_foods_and_recipes
import macrotrack.composeapp.generated.resources.adding_to
import macrotrack.composeapp.generated.resources.clear_search
import macrotrack.composeapp.generated.resources.after_adding_left
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.ic_star_outline
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.new_food_button
import macrotrack.composeapp.generated.resources.new_recipe_button
import macrotrack.composeapp.generated.resources.no_favorites_hint
import macrotrack.composeapp.generated.resources.no_favorites_title
import macrotrack.composeapp.generated.resources.no_items_match
import macrotrack.composeapp.generated.resources.no_recent_items
import macrotrack.composeapp.generated.resources.no_saved_items
import macrotrack.composeapp.generated.resources.recent_portion_hint
import macrotrack.composeapp.generated.resources.recently_logged
import macrotrack.composeapp.generated.resources.remaining_title
import macrotrack.composeapp.generated.resources.search_foods_and_recipes
import macrotrack.composeapp.generated.resources.selected_summary
import macrotrack.composeapp.generated.resources.your_favorites
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonSwipeToDelete
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddMealScreen(
    modifier: Modifier = Modifier,
    onBackButtonPressed: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<AddMealViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.destinationRoute) {
        viewState.destinationRoute?.let { route ->
            onNavigate(route)
            viewModel.resetNavigateScreen()
        }
    }
    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onBackButtonPressed()
    }

    AddMealScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBackButtonPressed
    )
}

@Composable
private fun AddMealScreen(
    modifier: Modifier = Modifier,
    viewState: AddMealViewState,
    callbacks: AddMealCallbacks,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            AddMealHeader(
                mealType = viewState.selectedMealType,
                remainingCalories = viewState.remainingCalories,
                selectionCount = viewState.selection.size,
                onAdd = callbacks::onAddSelection,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        val items = viewState.visibleItems
        val selectedItems = viewState.selection.associateBy { it.key }
        val listState = rememberLazyListState()
        // The list re-sorts once the recently logged order loads; keep the list pinned to the top
        // instead of following the previous first item into the middle.
        LaunchedEffect(key1 = items.firstOrNull()?.key) {
            listState.scrollToItem(index = 0)
        }
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.background)
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                SearchField(query = viewState.searchQuery, onQueryChange = callbacks::onSearchQuery)
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    CreateButton(
                        modifier = Modifier.weight(weight = 1f),
                        text = Res.string.new_food_button,
                        onClick = {
                            callbacks.onNavigateScreen(route = AppDestinations.Root.CreateFood.createRoute(foodName = ""))
                        }
                    )
                    CreateButton(
                        modifier = Modifier.weight(weight = 1f),
                        text = Res.string.new_recipe_button,
                        onClick = {
                            callbacks.onNavigateScreen(route = AppDestinations.Root.CreateRecipe.createRoute(recipeName = ""))
                        }
                    )
                }
                AddMealFilter(
                    modifier = Modifier.fillMaxWidth(),
                    selectedFilter = viewState.activeFilter,
                    favoriteCount = viewState.favoriteCount,
                    onFilterSelected = callbacks::onMealFilterSelected
                )
                if (viewState.selection.isNotEmpty()) {
                    SelectionSummary(
                        count = viewState.selection.size,
                        calories = viewState.selectionCalories,
                        remainingAfter = viewState.remainingCalories?.let { it - viewState.selectionCalories }
                    )
                }
                SectionHeader(
                    title = when (viewState.activeFilter) {
                        MealFilter.RECENT -> Res.string.recently_logged
                        MealFilter.FAVORITES -> Res.string.your_favorites
                        MealFilter.ALL -> Res.string.all_foods_and_recipes
                    },
                    hint = Res.string.recent_portion_hint.takeIf {
                        viewState.activeFilter == MealFilter.RECENT && items.isNotEmpty()
                    }
                )
            }
            LazyColumn(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                if (items.isEmpty() && !viewState.isSearching && viewState.activeFilter == MealFilter.FAVORITES) {
                    item(key = "empty") {
                        EmptyFavorites(modifier = Modifier.fillMaxWidth())
                    }
                } else if (items.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = when {
                                viewState.isSearching -> stringResource(resource = Res.string.no_items_match, viewState.searchQuery)
                                viewState.activeFilter == MealFilter.RECENT -> stringResource(resource = Res.string.no_recent_items)
                                else -> stringResource(resource = Res.string.no_saved_items)
                            },
                            style = typography.regular13,
                            color = colors.muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    }
                }
                items(items = items, key = { it.key }) { item ->
                    val selectedItem = selectedItems[item.key]
                    CommonSwipeToDelete(
                        modifier = Modifier.fillMaxWidth(),
                        onDelete = { callbacks.onDeleteItem(item = item) }
                    ) {
                        AddMealListItem(
                            modifier = Modifier.fillMaxWidth(),
                            item = item,
                            selectedItem = selectedItem,
                            isExpanded = selectedItem != null && item.key == viewState.focusedKey,
                            showLastLogged = viewState.activeFilter == MealFilter.RECENT,
                            onToggle = { callbacks.onToggleItem(item = item) },
                            onToggleFavorite = { callbacks.onToggleFavorite(item = item) },
                            onClick = {
                                if (selectedItem != null) {
                                    callbacks.onFocusItem(key = item.key)
                                } else {
                                    callbacks.onToggleItem(item = item)
                                }
                            },
                            onLongPress = { callbacks.onEditItem(item = item) },
                            onAmountChanged = callbacks::onAmountChanged
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMealHeader(
    mealType: MealType,
    remainingCalories: Int?,
    selectionCount: Int,
    onAdd: () -> Unit,
    onBack: () -> Unit
) {
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
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = Res.string.adding_to),
                style = typography.regular11,
                color = colors.muted
            )
            Text(
                text = stringResource(resource = mealType.title),
                style = typography.bold18,
                color = colors.ink
            )
        }
        if (selectionCount > 0) {
            Row(
                modifier = Modifier
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.accent)
                    .clickable(onClick = onAdd)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(size = 16.dp)
                )
                Text(
                    text = stringResource(resource = Res.string.add_count, selectionCount),
                    style = typography.bold14,
                    color = colors.onAccent
                )
            }
        } else if (remainingCalories != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(resource = Res.string.remaining_title),
                    style = typography.regular11,
                    color = colors.muted
                )
                Text(
                    text = stringResource(resource = Res.string.kcal, remainingCalories),
                    style = typography.bold15,
                    color = colors.ink
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            // The clear button brings its own padding.
            .padding(start = 14.dp, end = if (query.isEmpty()) 14.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_search),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 18.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = typography.regular13.copy(color = colors.ink),
            cursorBrush = SolidColor(value = colors.accent),
            modifier = Modifier.weight(weight = 1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(resource = Res.string.search_foods_and_recipes),
                        style = typography.regular13,
                        color = colors.muted
                    )
                }
                innerTextField()
            }
        )
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = stringResource(resource = Res.string.clear_search),
                    tint = colors.muted,
                    modifier = Modifier.size(size = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateButton(
    modifier: Modifier = Modifier,
    text: StringResource,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(height = 44.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.accentSoft)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_plus),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.size(size = 18.dp)
        )
        Text(
            text = stringResource(resource = text),
            style = typography.bold14,
            color = colors.accentInk
        )
    }
}

@Composable
private fun SectionHeader(
    title: StringResource,
    hint: StringResource?
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = title),
                style = typography.bold13,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            AddMealLegend()
        }
        if (hint != null) {
            Text(
                text = stringResource(resource = hint),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun EmptyFavorites(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = modifier
            .padding(top = 8.dp)
            .clip(shape = shape)
            .border(width = 1.5.dp, color = colors.dashed, shape = shape)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 48.dp)
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = colors.warmSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_star_outline),
                contentDescription = null,
                tint = colors.warmInk,
                modifier = Modifier.size(size = 24.dp)
            )
        }
        Text(
            text = stringResource(resource = Res.string.no_favorites_title),
            style = typography.bold15,
            color = colors.ink
        )
        Text(
            text = stringResource(resource = Res.string.no_favorites_hint),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SelectionSummary(
    count: Int,
    calories: Int,
    remainingAfter: Int?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.hero)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.selected_summary, count, calories.withThousands()),
            style = typography.bold13,
            color = colors.ink,
            modifier = Modifier.weight(weight = 1f)
        )
        if (remainingAfter != null) {
            Text(
                text = stringResource(resource = Res.string.after_adding_left, remainingAfter.withThousands()),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealScreen() {
    MacroTrackTheme {
        AddMealScreen(
            viewState = AddMealViewState(
                selectedMealType = MealType.DINNER,
                completeFoodList = listOf(dummyFood, dummyFood.copy(name = "Salmon fillet")),
                remainingCalories = 1050
            ),
            callbacks = AddMealCallbacks.default(),
            onBack = {}
        )
    }
}
