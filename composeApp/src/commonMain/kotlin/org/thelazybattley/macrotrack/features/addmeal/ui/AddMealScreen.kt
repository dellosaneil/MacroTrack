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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_count
import macrotrack.composeapp.generated.resources.adding_to
import macrotrack.composeapp.generated.resources.after_adding_left
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.browse_food_library
import macrotrack.composeapp.generated.resources.browse_food_library_hint
import macrotrack.composeapp.generated.resources.browse_the_food_library
import macrotrack.composeapp.generated.resources.build_a_recipe
import macrotrack.composeapp.generated.resources.clear_search
import macrotrack.composeapp.generated.resources.create_query_as_food
import macrotrack.composeapp.generated.resources.favorites_hint
import macrotrack.composeapp.generated.resources.food_library
import macrotrack.composeapp.generated.resources.food_library_search_hint
import macrotrack.composeapp.generated.resources.ic_barcode_scan
import macrotrack.composeapp.generated.resources.ic_book
import macrotrack.composeapp.generated.resources.ic_bookmark_off
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.ic_search_none
import macrotrack.composeapp.generated.resources.ic_star_outline
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.my_foods
import macrotrack.composeapp.generated.resources.my_foods_hint
import macrotrack.composeapp.generated.resources.new_food_button
import macrotrack.composeapp.generated.resources.new_recipe_button
import macrotrack.composeapp.generated.resources.no_favorites_hint
import macrotrack.composeapp.generated.resources.no_favorites_title
import macrotrack.composeapp.generated.resources.no_library_matches
import macrotrack.composeapp.generated.resources.no_matches_body
import macrotrack.composeapp.generated.resources.no_matches_title
import macrotrack.composeapp.generated.resources.no_matches_try
import macrotrack.composeapp.generated.resources.no_matches_try_barcode
import macrotrack.composeapp.generated.resources.no_matches_try_fewer_words
import macrotrack.composeapp.generated.resources.no_matches_try_spelling
import macrotrack.composeapp.generated.resources.no_my_foods_yet
import macrotrack.composeapp.generated.resources.no_recent_items
import macrotrack.composeapp.generated.resources.no_recipes_yet
import macrotrack.composeapp.generated.resources.no_saved_foods_hint
import macrotrack.composeapp.generated.resources.no_saved_foods_hint_library
import macrotrack.composeapp.generated.resources.no_saved_foods_named
import macrotrack.composeapp.generated.resources.recent_portion_hint
import macrotrack.composeapp.generated.resources.recently_logged
import macrotrack.composeapp.generated.resources.recipes_hint
import macrotrack.composeapp.generated.resources.remaining_title
import macrotrack.composeapp.generated.resources.results
import macrotrack.composeapp.generated.resources.scan_barcode
import macrotrack.composeapp.generated.resources.scan_button
import macrotrack.composeapp.generated.resources.scan_just_added
import macrotrack.composeapp.generated.resources.search_or_scan
import macrotrack.composeapp.generated.resources.see_all_in_library
import macrotrack.composeapp.generated.resources.selected_summary
import macrotrack.composeapp.generated.resources.your_favorites
import macrotrack.composeapp.generated.resources.your_foods_and_recipes
import macrotrack.composeapp.generated.resources.your_recipes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.addmeal.MealListItem
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.tourTarget
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
        val listState = rememberLazyListState()
        val openLibrary = { query: String? ->
            callbacks.onNavigateScreen(
                route = AppDestinations.Root.FoodLibrary.createRoute(mealType = viewState.selectedMealType.name, query = query)
            )
        }
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
                SearchField(
                    modifier = Modifier.tourTarget(target = TourTarget.ADD_MEAL_SEARCH),
                    query = viewState.searchQuery,
                    onQueryChange = callbacks::onSearchQuery,
                    onScan = {
                        callbacks.onNavigateScreen(
                            route = AppDestinations.Root.ScanBarcode.createRoute(mealType = viewState.selectedMealType.name)
                        )
                    }
                )
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
                BrowseLibraryRow(onClick = { openLibrary(null) })
                if (!viewState.isSearching) {
                    AddMealFilter(
                        modifier = Modifier.fillMaxWidth(),
                        selectedFilter = viewState.activeFilter,
                        favoriteCount = viewState.favoriteCount,
                        onFilterSelected = callbacks::onMealFilterSelected
                    )
                }
                if (viewState.selection.isNotEmpty()) {
                    SelectionSummary(
                        count = viewState.selection.size,
                        calories = viewState.selectionCalories,
                        remainingAfter = viewState.remainingCalories?.let { it - viewState.selectionCalories }
                    )
                }
                if (!viewState.isSearching) {
                    SectionHeader(
                        title = when (viewState.activeFilter) {
                            MealFilter.RECENT -> Res.string.recently_logged
                            MealFilter.FAVORITES -> Res.string.your_favorites
                            MealFilter.MY_FOODS -> Res.string.my_foods
                            MealFilter.RECIPES -> Res.string.your_recipes
                        },
                        hint = when (viewState.activeFilter) {
                            MealFilter.RECENT -> Res.string.recent_portion_hint
                            MealFilter.FAVORITES -> Res.string.favorites_hint
                            MealFilter.MY_FOODS -> Res.string.my_foods_hint
                            MealFilter.RECIPES -> Res.string.recipes_hint
                        }.takeIf { items.isNotEmpty() }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                if (viewState.isSearching) {
                    searchResults(viewState = viewState, callbacks = callbacks, openLibrary = openLibrary)
                } else {
                    viewState.justScannedName?.takeIf { name -> items.any { it.isJustScanned } }?.let { name ->
                        item(key = "just_scanned") {
                            JustScannedBanner(foodName = name, mealType = viewState.selectedMealType)
                        }
                    }
                    if (items.isEmpty() && viewState.activeFilter == MealFilter.FAVORITES) {
                        item(key = "empty") {
                            EmptyFavorites(modifier = Modifier.fillMaxWidth())
                        }
                    } else if (items.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(
                                    resource = when (viewState.activeFilter) {
                                        MealFilter.RECENT -> Res.string.no_recent_items
                                        MealFilter.RECIPES -> Res.string.no_recipes_yet
                                        else -> Res.string.no_my_foods_yet
                                    }
                                ),
                                style = typography.regular13,
                                color = colors.muted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            )
                        }
                    }
                    mealItems(
                        items = items,
                        viewState = viewState,
                        callbacks = callbacks,
                        showLastLogged = viewState.activeFilter == MealFilter.RECENT,
                        isTourList = true
                    )
                }
            }
        }
    }
}

/**
 * While searching, results come in two groups: the user's own foods and recipes, then the food library (a short
 * preview that links to the full library). With no match anywhere, the screen offers ways to add the food instead.
 */
private fun LazyListScope.searchResults(
    viewState: AddMealViewState,
    callbacks: AddMealCallbacks,
    openLibrary: (String?) -> Unit
) {
    val query = viewState.searchQuery.trim()
    val matches = viewState.searchMatches
    val libraryMatches = viewState.librarySearchMatches
    val createFood = {
        callbacks.onNavigateScreen(route = AppDestinations.Root.CreateFood.createRoute(foodName = "", prefillName = query))
    }
    val createRecipe = {
        callbacks.onNavigateScreen(route = AppDestinations.Root.CreateRecipe.createRoute(recipeName = ""))
    }
    if (matches.isEmpty() && libraryMatches.isEmpty()) {
        item(key = "no_matches") {
            NoMatches(
                query = query,
                onCreateFood = createFood,
                onScan = {
                    callbacks.onNavigateScreen(
                        route = AppDestinations.Root.ScanBarcode.createRoute(mealType = viewState.selectedMealType.name)
                    )
                },
                onBuildRecipe = createRecipe,
                onClearSearch = { callbacks.onSearchQuery(query = "") }
            )
        }
        return
    }
    item(key = "matches_header") {
        GroupHeader(title = stringResource(resource = Res.string.your_foods_and_recipes), count = matches.size)
    }
    if (matches.isEmpty()) {
        item(key = "no_saved_matches") {
            NoSavedMatches(
                query = query,
                hasLibraryMatches = libraryMatches.isNotEmpty(),
                onNewFood = createFood,
                onNewRecipe = createRecipe
            )
        }
    }
    mealItems(items = matches, viewState = viewState, callbacks = callbacks, showLastLogged = false, isTourList = false)
    item(key = "library_header") {
        LibraryGroupHeader(count = libraryMatches.size, query = query)
    }
    mealItems(
        items = libraryMatches.take(n = AddMealViewState.LIBRARY_PREVIEW_LIMIT),
        viewState = viewState,
        callbacks = callbacks,
        showLastLogged = false,
        isTourList = false
    )
    item(key = "library_more") {
        LibraryMoreButton(
            text = if (libraryMatches.size > AddMealViewState.LIBRARY_PREVIEW_LIMIT) {
                stringResource(resource = Res.string.see_all_in_library, libraryMatches.size)
            } else {
                stringResource(resource = Res.string.browse_the_food_library)
            },
            onClick = { openLibrary(query) }
        )
    }
}

/** Library foods are read-only, so they can't be swiped away or long-pressed to edit. */
private fun LazyListScope.mealItems(
    items: List<MealListItem>,
    viewState: AddMealViewState,
    callbacks: AddMealCallbacks,
    showLastLogged: Boolean,
    isTourList: Boolean
) {
    val selectedItems = viewState.selection.associateBy { it.key }
    items(items = items, key = { it.key }) { item ->
        val selectedItem = selectedItems[item.key]
        val listItem = @Composable {
            AddMealListItem(
                modifier = Modifier.fillMaxWidth(),
                item = item,
                selectedItem = selectedItem,
                isExpanded = selectedItem != null && item.key == viewState.focusedKey,
                showLastLogged = showLastLogged,
                onToggle = { callbacks.onToggleItem(item = item) },
                onToggleFavorite = { callbacks.onToggleFavorite(item = item) },
                onClick = {
                    if (selectedItem != null) {
                        callbacks.onFocusItem(key = item.key)
                    } else {
                        callbacks.onToggleItem(item = item)
                    }
                },
                onLongPress = if (item.isLibrary) null else ({ callbacks.onEditItem(item = item) }),
                onAmountChanged = callbacks::onAmountChanged
            )
        }
        val modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isTourList && item.key == items.first().key) {
                    Modifier.tourTarget(target = TourTarget.ADD_MEAL_FIRST_ITEM)
                } else {
                    Modifier
                }
            )
        if (item.isLibrary) {
            Box(modifier = modifier) { listItem() }
        } else {
            CommonSwipeToDelete(
                modifier = modifier,
                onDelete = { callbacks.onDeleteItem(item = item) }
            ) {
                listItem()
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
                    .tourTarget(target = TourTarget.ADD_MEAL_CONFIRM)
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
            Column(
                modifier = Modifier.tourTarget(target = TourTarget.ADD_MEAL_CONFIRM),
                horizontalAlignment = Alignment.End
            ) {
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
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onScan: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            // The clear and scan buttons bring their own padding.
            .padding(start = 14.dp, end = 4.dp),
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
                        text = stringResource(resource = Res.string.search_or_scan),
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
                    .size(size = 32.dp)
                    .clip(shape = CircleShape)
                    .background(color = colors.divider)
                    .clickable(role = Role.Button) { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = stringResource(resource = Res.string.clear_search),
                    tint = colors.muted,
                    modifier = Modifier.size(size = 14.dp)
                )
            }
        }
        val scanLabel = stringResource(resource = Res.string.scan_barcode)
        Row(
            modifier = Modifier
                .padding(end = 2.dp)
                .height(height = 36.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(color = colors.accent)
                .clickable(role = Role.Button, onClick = onScan)
                .semantics(mergeDescendants = true) { contentDescription = scanLabel }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_barcode_scan),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(size = 18.dp)
            )
            Text(
                text = stringResource(resource = Res.string.scan_button),
                style = typography.bold13,
                color = colors.onAccent,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    }
}

@Composable
private fun JustScannedBanner(
    foodName: String,
    mealType: MealType
) {
    val text = stringResource(Res.string.scan_just_added, foodName, stringResource(resource = mealType.title))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 14.dp))
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_check),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.size(size = 18.dp)
        )
        Text(
            text = buildAnnotatedString {
                val start = text.indexOf(string = foodName)
                if (start < 0) {
                    append(text)
                } else {
                    append(text.substring(startIndex = 0, endIndex = start))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(foodName) }
                    append(text.substring(startIndex = start + foodName.length))
                }
            },
            style = typography.regular13,
            color = colors.accentInk
        )
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
private fun BrowseLibraryRow(onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 32.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(color = colors.nightSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_book),
                contentDescription = null,
                tint = colors.nightInk,
                modifier = Modifier.size(size = 16.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = Res.string.browse_food_library),
                style = typography.bold14,
                color = colors.ink
            )
            Text(
                text = stringResource(resource = Res.string.browse_food_library_hint),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

@Composable
private fun GroupHeader(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = typography.bold13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
        Text(
            text = pluralStringResource(resource = Res.plurals.results, quantity = count, count),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun LibraryGroupHeader(count: Int, query: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        HorizontalDivider(thickness = 1.dp, color = colors.line, modifier = Modifier.padding(bottom = 12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_book),
                contentDescription = null,
                tint = colors.nightInk,
                modifier = Modifier.size(size = 14.dp)
            )
            Text(
                text = stringResource(resource = Res.string.food_library),
                style = typography.bold13,
                color = colors.nightInk,
                modifier = Modifier.padding(start = 6.dp).weight(weight = 1f)
            )
            Text(
                text = pluralStringResource(resource = Res.plurals.results, quantity = count, count),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Text(
            text = stringResource(resource = Res.string.food_library_search_hint),
            style = typography.regular11,
            color = colors.muted
        )
        if (count == 0) {
            Text(
                text = stringResource(resource = Res.string.no_library_matches, query),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun LibraryMoreButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 44.dp)
            .clip(shape = shape)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = colors.nightInk)
    }
}

/** The user has no saved food with this name; the library may still have some, listed below. */
@Composable
private fun NoSavedMatches(
    query: String,
    hasLibraryMatches: Boolean,
    onNewFood: () -> Unit,
    onNewRecipe: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .border(width = 1.5.dp, color = colors.dashed, shape = shape)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.warmSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_bookmark_off),
                    contentDescription = null,
                    tint = colors.warmInk,
                    modifier = Modifier.size(size = 20.dp)
                )
            }
            Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                Text(
                    text = stringResource(resource = Res.string.no_saved_foods_named, query),
                    style = typography.bold14,
                    color = colors.ink
                )
                Text(
                    text = stringResource(
                        resource = if (hasLibraryMatches) Res.string.no_saved_foods_hint_library else Res.string.no_saved_foods_hint
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            CreateButton(modifier = Modifier.weight(weight = 1f), text = Res.string.new_food_button, onClick = onNewFood)
            CreateButton(modifier = Modifier.weight(weight = 1f), text = Res.string.new_recipe_button, onClick = onNewRecipe)
        }
    }
}

/** Nothing matched in the user's foods, their recipes or the library. */
@Composable
private fun NoMatches(
    query: String,
    onCreateFood: () -> Unit,
    onScan: () -> Unit,
    onBuildRecipe: () -> Unit,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .padding(start = 8.dp, end = 8.dp, top = 18.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 64.dp)
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = colors.nightSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_search_none),
                    contentDescription = null,
                    tint = colors.nightInk,
                    modifier = Modifier.size(size = 30.dp)
                )
            }
            Text(
                text = stringResource(resource = Res.string.no_matches_title, query),
                style = typography.bold18,
                color = colors.ink,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(resource = Res.string.no_matches_body),
                style = typography.regular13,
                color = colors.muted,
                textAlign = TextAlign.Center
            )
        }
        val shape = RoundedCornerShape(size = 16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Text(text = stringResource(resource = Res.string.no_matches_try), style = typography.bold13, color = colors.ink)
            listOf(
                Res.string.no_matches_try_spelling,
                Res.string.no_matches_try_fewer_words,
                Res.string.no_matches_try_barcode
            ).forEach { tip ->
                Text(text = "· " + stringResource(resource = tip), style = typography.regular13, color = colors.muted)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 52.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = colors.accent)
                .clickable(role = Role.Button, onClick = onCreateFood)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_plus),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(size = 18.dp)
            )
            Text(
                text = stringResource(resource = Res.string.create_query_as_food, query),
                style = typography.bold15,
                color = colors.onAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            OutlinedAction(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.scan_barcode), onClick = onScan)
            OutlinedAction(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.build_a_recipe), onClick = onBuildRecipe)
        }
        Text(
            text = stringResource(resource = Res.string.clear_search),
            style = typography.bold14,
            color = colors.accentInk,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onClearSearch)
                .padding(horizontal = 16.dp, vertical = 13.dp)
        )
    }
}

@Composable
private fun OutlinedAction(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Box(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = colors.ink, textAlign = TextAlign.Center)
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
