package org.thelazybattley.macrotrack.features.foodlibrary.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.ui.common.portionUnit
import org.thelazybattley.macrotrack.ui.common.formatPortion
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_count
import macrotrack.composeapp.generated.resources.add_to_meal
import macrotrack.composeapp.generated.resources.add_value
import macrotrack.composeapp.generated.resources.adding_to_meal
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.clear_search
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.create_query_in_my_foods
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.fiber
import macrotrack.composeapp.generated.resources.food_library
import macrotrack.composeapp.generated.resources.grams_value
import macrotrack.composeapp.generated.resources.ic_barcode_scan
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_copy
import macrotrack.composeapp.generated.resources.ic_lock
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.ic_search_none
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.library_categories
import macrotrack.composeapp.generated.resources.library_data_source
import macrotrack.composeapp.generated.resources.library_popular
import macrotrack.composeapp.generated.resources.library_read_only_banner
import macrotrack.composeapp.generated.resources.library_read_only_tag
import macrotrack.composeapp.generated.resources.library_results_for
import macrotrack.composeapp.generated.resources.make_my_own_version
import macrotrack.composeapp.generated.resources.make_my_own_version_hint
import macrotrack.composeapp.generated.resources.milligrams_value
import macrotrack.composeapp.generated.resources.not_in_library_body
import macrotrack.composeapp.generated.resources.not_in_library_title
import macrotrack.composeapp.generated.resources.nutrition_facts
import macrotrack.composeapp.generated.resources.nutrition_of
import macrotrack.composeapp.generated.resources.p_c_f
import macrotrack.composeapp.generated.resources.per_amount_serving
import macrotrack.composeapp.generated.resources.per_serving
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.remove_value
import macrotrack.composeapp.generated.resources.scan_barcode
import macrotrack.composeapp.generated.resources.search_the_library
import macrotrack.composeapp.generated.resources.selected_for_meal
import macrotrack.composeapp.generated.resources.sodium
import macrotrack.composeapp.generated.resources.sugar
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.LibraryCategory
import org.thelazybattley.macrotrack.domain.model.LibraryFood
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.stripeColor
import org.thelazybattley.macrotrack.features.foodlibrary.FoodLibraryCallbacks
import org.thelazybattley.macrotrack.features.foodlibrary.FoodLibraryViewModel
import org.thelazybattley.macrotrack.features.foodlibrary.FoodLibraryViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

@Composable
fun FoodLibraryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSaved: () -> Unit
) {
    val viewModel = koinViewModel<FoodLibraryViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.destinationRoute) {
        viewState.destinationRoute?.let { route ->
            onNavigate(route)
            viewModel.resetNavigateScreen()
        }
    }
    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onSaved()
    }
    FoodLibraryScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack
    )
}

@Composable
private fun FoodLibraryScreen(
    modifier: Modifier = Modifier,
    viewState: FoodLibraryViewState,
    callbacks: FoodLibraryCallbacks,
    onBack: () -> Unit
) {
    val mealTitle = stringResource(resource = viewState.mealType.title)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            LibraryHeader(
                mealTitle = mealTitle,
                selectionCount = viewState.selection.size,
                onAdd = callbacks::onAddSelection,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        val foods = viewState.visibleFoods
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 14.dp)
            ) {
                ReadOnlyBanner()
                LibrarySearchField(query = viewState.query, onQueryChange = callbacks::onQueryChanged)
            }
            CategoryChips(
                selected = viewState.category.takeIf { !viewState.isSearching },
                isSearching = viewState.isSearching,
                onSelected = callbacks::onCategorySelected
            )
            LazyColumn(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                item(key = "heading") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                viewState.isSearching -> pluralStringResource(
                                    resource = Res.plurals.library_results_for,
                                    quantity = foods.size,
                                    foods.size,
                                    viewState.query.trim()
                                )
                                else -> stringResource(resource = viewState.category?.title ?: Res.string.library_popular)
                            },
                            style = typography.bold13,
                            color = colors.ink,
                            modifier = Modifier.weight(weight = 1f)
                        )
                        Text(text = stringResource(resource = Res.string.per_serving), style = typography.regular11, color = colors.muted)
                    }
                }
                if (foods.isEmpty() && viewState.isSearching) {
                    item(key = "empty") {
                        NotInLibrary(
                            query = viewState.query.trim(),
                            onScan = callbacks::onScan,
                            onCreateFood = callbacks::onCreateFood,
                            onClearSearch = { callbacks.onQueryChanged(query = "") }
                        )
                    }
                }
                items(items = foods, key = { it.food.name }) { libraryFood ->
                    LibraryListItem(
                        food = libraryFood.food,
                        isSelected = libraryFood.food.name in viewState.selection,
                        onOpen = { callbacks.onOpenFood(name = libraryFood.food.name) },
                        onToggle = { callbacks.onToggleFood(name = libraryFood.food.name) }
                    )
                }
                if (foods.isNotEmpty()) {
                    item(key = "source") {
                        Text(
                            text = stringResource(resource = Res.string.library_data_source),
                            style = typography.regular11,
                            color = colors.faint,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
    viewState.openFood?.let { libraryFood ->
        NutritionSheet(
            libraryFood = libraryFood,
            mealTitle = mealTitle,
            isSelected = libraryFood.food.name in viewState.selection,
            onAdd = callbacks::onAddOpenFood,
            onMakeOwnVersion = callbacks::onMakeOwnVersion,
            onDismiss = callbacks::onCloseFood
        )
    }
}

@Composable
private fun LibraryHeader(
    mealTitle: String,
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
        SquareIconButton(
            icon = Res.drawable.ic_chevron_left,
            contentDescription = stringResource(resource = Res.string.back),
            onClick = onBack
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = stringResource(resource = Res.string.adding_to_meal, mealTitle),
                style = typography.regular11,
                color = colors.muted
            )
            Text(text = stringResource(resource = Res.string.food_library), style = typography.bold18, color = colors.ink)
        }
        if (selectionCount > 0) {
            Row(
                modifier = Modifier
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.accent)
                    .clickable(role = Role.Button, onClick = onAdd)
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
        }
    }
}

@Composable
private fun SquareIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = contentDescription,
            tint = colors.ink,
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

@Composable
private fun ReadOnlyBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.nightSoft)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_lock),
            contentDescription = null,
            tint = colors.nightInk,
            modifier = Modifier.size(size = 16.dp)
        )
        Text(
            text = stringResource(resource = Res.string.library_read_only_banner),
            style = typography.bold12,
            color = colors.nightInk
        )
    }
}

@Composable
private fun LibrarySearchField(query: String, onQueryChange: (String) -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(start = 14.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_search),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 18.dp)
        )
        val placeholder = stringResource(resource = Res.string.search_the_library)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = typography.regular13.copy(color = colors.ink),
            cursorBrush = SolidColor(value = colors.accent),
            modifier = Modifier
                .weight(weight = 1f)
                .semantics { contentDescription = placeholder },
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
                    .size(size = 32.dp)
                    .clip(shape = RoundedCornerShape(size = 16.dp))
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
    }
}

/** Popular, then each category. While searching no chip is picked; picking one clears the search. */
@Composable
private fun CategoryChips(
    selected: LibraryCategory?,
    isSearching: Boolean,
    onSelected: (LibraryCategory?) -> Unit
) {
    val label = stringResource(resource = Res.string.library_categories)
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { paneTitle = label },
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        val chips = listOf<LibraryCategory?>(null) + LibraryCategory.entries
        items(items = chips, key = { it?.name ?: "popular" }) { category ->
            val isSelected = !isSearching && category == selected
            val shape = RoundedCornerShape(size = 18.dp)
            Box(
                modifier = Modifier
                    .height(height = 36.dp)
                    .clip(shape = shape)
                    .background(color = if (isSelected) colors.nightSoft else colors.surface)
                    .border(width = 1.dp, color = if (isSelected) colors.nightInk else colors.line, shape = shape)
                    .clickable(role = Role.Tab) { onSelected(category) }
                    .semantics { this.selected = isSelected }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = category?.title ?: Res.string.library_popular),
                    style = typography.bold13,
                    color = if (isSelected) colors.nightInk else colors.ink,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LibraryListItem(
    food: Food,
    isSelected: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width = 5.dp)
                .fillMaxHeight()
                .background(color = food.dominantMacro.stripeColor())
        )
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .padding(start = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Text(
                text = food.name,
                style = typography.bold15,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatPortion(amount = food.weight, storedUnit = food.unit) + " · " +
                    stringResource(
                        resource = Res.string.p_c_f,
                        food.macros.protein.roundToInt(),
                        food.macros.carbs.roundToInt(),
                        food.macros.fat.roundToInt()
                    ),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = food.macros.calories.toString(), style = typography.bold15, color = colors.ink)
            Text(text = stringResource(resource = Res.string.kcal_text), style = typography.regular10, color = colors.muted)
        }
        Box(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = if (isSelected) colors.accent else colors.accentSoft)
                .clickable(role = Role.Checkbox, onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = if (isSelected) Res.drawable.ic_check else Res.drawable.ic_plus),
                contentDescription = stringResource(
                    resource = if (isSelected) Res.string.remove_value else Res.string.add_value,
                    food.name
                ),
                tint = if (isSelected) colors.onAccent else colors.accentInk,
                modifier = Modifier.size(size = 18.dp)
            )
        }
    }
}

@Composable
private fun NotInLibrary(
    query: String,
    onScan: () -> Unit,
    onCreateFood: () -> Unit,
    onClearSearch: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
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
                text = stringResource(resource = Res.string.not_in_library_title),
                style = typography.bold18,
                color = colors.ink,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(resource = Res.string.not_in_library_body, query),
                style = typography.regular13,
                color = colors.muted,
                textAlign = TextAlign.Center
            )
        }
        ActionButton(
            text = stringResource(resource = Res.string.scan_barcode),
            icon = Res.drawable.ic_barcode_scan,
            isPrimary = true,
            onClick = onScan
        )
        ActionButton(
            text = stringResource(resource = Res.string.create_query_in_my_foods, query),
            isPrimary = false,
            onClick = onCreateFood
        )
        Text(
            text = stringResource(resource = Res.string.clear_search),
            style = typography.bold14,
            color = colors.accentInk,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClick = onClearSearch)
                .padding(horizontal = 16.dp, vertical = 13.dp)
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    isPrimary: Boolean,
    icon: DrawableResource? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    val contentColor = if (isPrimary) colors.onAccent else colors.ink
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = if (isPrimary) 52.dp else 48.dp)
            .clip(shape = shape)
            .background(color = if (isPrimary) colors.accent else colors.surface)
            .then(if (isPrimary) Modifier else Modifier.border(width = 1.dp, color = colors.line, shape = shape))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        icon?.let {
            Icon(
                painter = painterResource(resource = it),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Text(
            text = text,
            style = if (isPrimary) typography.bold15 else typography.bold14,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** A library food's nutrition per serving. It can be picked for the meal, or copied to My foods to edit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NutritionSheet(
    libraryFood: LibraryFood,
    mealTitle: String,
    isSelected: Boolean,
    onAdd: () -> Unit,
    onMakeOwnVersion: () -> Unit,
    onDismiss: () -> Unit
) {
    val food = libraryFood.food
    val sheetTitle = stringResource(resource = Res.string.nutrition_of, food.name)
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
                .semantics { paneTitle = sheetTitle }
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    Text(text = food.name, style = typography.bold24, color = colors.ink)
                    Row(
                        modifier = Modifier
                            .background(color = colors.nightSoft, shape = RoundedCornerShape(size = 8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.ic_lock),
                            contentDescription = null,
                            tint = colors.nightInk,
                            modifier = Modifier.size(size = 12.dp)
                        )
                        Text(
                            text = stringResource(resource = Res.string.library_read_only_tag),
                            style = typography.bold11,
                            color = colors.nightInk
                        )
                    }
                }
                SquareIconButton(
                    icon = Res.drawable.ic_close,
                    contentDescription = stringResource(resource = Res.string.close),
                    onClick = onDismiss
                )
            }
            MacroTiles(macros = food.macros)
            NutritionFacts(libraryFood = libraryFood)
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                ActionButton(
                    text = stringResource(
                        resource = if (isSelected) Res.string.selected_for_meal else Res.string.add_to_meal,
                        mealTitle
                    ),
                    isPrimary = true,
                    onClick = onAdd
                )
                ActionButton(
                    text = stringResource(resource = Res.string.make_my_own_version),
                    icon = Res.drawable.ic_copy,
                    isPrimary = false,
                    onClick = onMakeOwnVersion
                )
                Text(
                    text = stringResource(resource = Res.string.make_my_own_version_hint),
                    style = typography.regular11,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MacroTiles(macros: FoodMacros) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        listOf(
            Triple(stringResource(resource = Res.string.kcal_text), macros.calories.toString(), null),
            Triple(stringResource(resource = Res.string.protein), "${macros.protein.toCleanString()}g", colors.protein),
            Triple(stringResource(resource = Res.string.carbs), "${macros.carbs.toCleanString()}g", colors.carbs),
            Triple(stringResource(resource = Res.string.fat), "${macros.fat.toCleanString()}g", colors.fat)
        ).forEach { (label, value, dotColor) ->
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                    .semantics(mergeDescendants = true) {}
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    if (dotColor != null) {
                        Box(
                            modifier = Modifier
                                .size(size = 7.dp)
                                .background(color = dotColor, shape = RoundedCornerShape(size = 2.dp))
                        )
                    }
                    Text(text = label, style = typography.regular11, color = colors.muted, maxLines = 1)
                }
                Text(text = value, style = typography.bold16, color = colors.ink, maxLines = 1)
            }
        }
    }
}

@Composable
private fun NutritionFacts(libraryFood: LibraryFood) {
    val food = libraryFood.food
    val facts = listOfNotNull(
        libraryFood.fiber?.let { stringResource(resource = Res.string.fiber) to stringResource(Res.string.grams_value, it.toCleanString()) },
        libraryFood.sugar?.let { stringResource(resource = Res.string.sugar) to stringResource(Res.string.grams_value, it.toCleanString()) },
        libraryFood.sodium?.let { stringResource(resource = Res.string.sodium) to stringResource(Res.string.milligrams_value, it.toString()) }
    )
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = stringResource(resource = Res.string.nutrition_facts),
                style = typography.bold12,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = portionUnit(storedUnit = food.unit).let { stringResource(Res.string.per_amount_serving, it.format(amount = food.weight), it.label) },
                style = typography.bold12,
                color = colors.muted
            )
        }
        facts.forEach { (label, value) ->
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            Row(
                modifier = Modifier
                    .semantics(mergeDescendants = true) {}
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(text = label, style = typography.regular13, color = colors.muted, modifier = Modifier.weight(weight = 1f))
                Text(text = value, style = typography.bold13, color = colors.ink)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewFoodLibraryScreen() {
    val apple = LibraryFood(
        food = Food(
            macros = FoodMacros(calories = 95, protein = 0.5, carbs = 25.1, fat = 0.3),
            name = "Apple, medium",
            weight = 182.0,
            dominantMacro = MacroType.CARBS,
            unit = "g"
        ),
        category = LibraryCategory.FRUIT,
        isPopular = true,
        fiber = 4.4,
        sugar = 18.9,
        sodium = 2
    )
    MacroTrackTheme {
        FoodLibraryScreen(
            viewState = FoodLibraryViewState(mealType = MealType.DINNER, foods = listOf(apple), selection = listOf(apple.food.name)),
            callbacks = FoodLibraryCallbacks.default(),
            onBack = {}
        )
    }
}
