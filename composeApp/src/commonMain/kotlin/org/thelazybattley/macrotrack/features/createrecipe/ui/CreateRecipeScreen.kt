package org.thelazybattley.macrotrack.features.createrecipe.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.ui.common.portionUnit
import org.thelazybattley.macrotrack.ui.common.formatPortion
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_ingredients
import macrotrack.composeapp.generated.resources.add_value
import macrotrack.composeapp.generated.resources.grilled_chicken_breast
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.no_ingredients_yet
import macrotrack.composeapp.generated.resources.p_c_f
import macrotrack.composeapp.generated.resources.recipe_name
import macrotrack.composeapp.generated.resources.remove_value
import macrotrack.composeapp.generated.resources.save_recipe
import macrotrack.composeapp.generated.resources.search_ingredient
import macrotrack.composeapp.generated.resources.this_food_is_already_saved
import macrotrack.composeapp.generated.resources.total
import macrotrack.composeapp.generated.resources.update_recipe
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.new_recipe_button
import macrotrack.composeapp.generated.resources.ingredients_added
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.ui.stripeColor
import org.thelazybattley.macrotrack.features.createfood.ui.MacroPreviewCard
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeCallbacks
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewModel
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewState
import org.thelazybattley.macrotrack.ui.common.CommonInputField
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CreateRecipeScreen(
    modifier: Modifier = Modifier,
    onBackButtonPressed: () -> Unit
) {
    val viewModel = koinViewModel<CreateRecipeViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.recipeSaved) {
        if (viewState.recipeSaved) onBackButtonPressed()
    }
    CreateRecipeScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBackButtonPressed
    )
}

@Composable
private fun CreateRecipeScreen(
    modifier: Modifier = Modifier,
    viewState: CreateRecipeViewState,
    callbacks: CreateRecipeCallbacks,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf(value = "") }
    val selectedNames = viewState.selectedIngredients.map { it.name }.toSet()
    val available = viewState.filteredIngredients.filterNot { it.name in selectedNames }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(
                title = if (viewState.isUpdating) Res.string.update_recipe else Res.string.new_recipe_button,
                onBack = onBack
            )
        },
        bottomBar = {
            CommonPrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                text = if (viewState.isUpdating) Res.string.update_recipe else Res.string.save_recipe,
                enabled = viewState.buttonEnabled,
                onClick = callbacks::onSaveRecipe
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            item(key = "name") {
                Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                    SectionLabel(text = stringResource(resource = Res.string.recipe_name))
                    CommonInputField(
                        modifier = Modifier.fillMaxWidth().height(height = 50.dp),
                        value = viewState.recipeName,
                        onValueChange = callbacks::inputRecipeName,
                        placeholder = stringResource(resource = Res.string.grilled_chicken_breast),
                        textStyle = typography.medium13,
                        isError = viewState.isRecipeNameTaken,
                        enabled = !viewState.isUpdating
                    )
                    if (viewState.isRecipeNameTaken) {
                        Text(
                            text = stringResource(resource = Res.string.this_food_is_already_saved),
                            style = typography.regular11,
                            color = colors.fat
                        )
                    }
                }
            }
            item(key = "totals") {
                MacroPreviewCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(resource = Res.string.total).uppercase(),
                    calories = viewState.macros.calories,
                    protein = viewState.macros.protein,
                    carbs = viewState.macros.carbs,
                    fat = viewState.macros.fat,
                    footnote = pluralStringResource(
                        resource = Res.plurals.items,
                        quantity = viewState.selectedIngredients.size,
                        viewState.selectedIngredients.size
                    )
                )
            }
            item(key = "selected") {
                SelectedIngredients(
                    ingredients = viewState.selectedIngredients,
                    onAmountChanged = callbacks::onIngredientAmountChanged,
                    onRemove = callbacks::removeIngredient
                )
            }
            item(key = "add_header") {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    SectionLabel(text = stringResource(resource = Res.string.add_ingredients))
                    SearchField(
                        query = searchQuery,
                        onQueryChange = { query ->
                            searchQuery = query
                            callbacks.onSearchKeyword(keyword = query)
                        }
                    )
                }
            }
            items(items = available, key = { "available:" + it.name }) { food ->
                AvailableIngredientRow(
                    food = food,
                    onAdd = { callbacks.onAddIngredient(food = food) }
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = typography.bold12, color = colors.muted)
}

@Composable
private fun SelectedIngredients(
    ingredients: List<Food>,
    onAmountChanged: (String, Double) -> Unit,
    onRemove: (Food) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.ingredients_added))
        if (ingredients.isEmpty()) {
            Text(
                text = stringResource(resource = Res.string.no_ingredients_yet),
                style = typography.regular13,
                color = colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 18.dp))
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 18.dp))
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            )
            return@Column
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            ingredients.forEachIndexed { index, food ->
                SelectedIngredientRow(
                    food = food,
                    onAmountChanged = { onAmountChanged(food.name, it) },
                    onRemove = { onRemove(food) }
                )
                if (index != ingredients.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

@Composable
private fun SelectedIngredientRow(
    food: Food,
    onAmountChanged: (Double) -> Unit,
    onRemove: () -> Unit
) {
    // Typed in the shown unit (oz for an ingredient stored in grams when imperial), kept in the stored one.
    val portionUnit = portionUnit(storedUnit = food.unit)
    var amount by remember(food.name, portionUnit) { mutableStateOf(value = portionUnit.format(amount = food.weight)) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width = 4.dp)
                .height(height = 32.dp)
                .background(color = food.dominantMacro.stripeColor(), shape = RoundedCornerShape(size = 2.dp))
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = food.name,
                style = typography.bold14,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    resource = Res.string.p_c_f,
                    food.macros.protein.toInt(),
                    food.macros.carbs.toInt(),
                    food.macros.fat.toInt()
                ) + " · " + stringResource(resource = Res.string.kcal, food.macros.calories),
                style = typography.regular11,
                color = colors.muted
            )
        }
        CommonInputField(
            modifier = Modifier.width(width = 64.dp).height(height = 38.dp),
            value = amount,
            onValueChange = { value ->
                if (value.length <= 6 && value.count { it == '.' } <= 1 && value.all { it.isDigit() || it == '.' }) {
                    amount = value
                    value.toDoubleOrNull()?.takeIf { it > 0 }?.let { onAmountChanged(portionUnit.toStored(value = it)) }
                }
            },
            keyboardType = KeyboardType.Decimal,
            textAlign = TextAlign.End,
            textStyle = typography.bold14,
            isHighlighted = true,
            cornerRadius = 10.dp,
            horizontalPadding = 8.dp
        )
        Text(
            text = portionUnit.label,
            style = typography.regular11,
            color = colors.muted,
            maxLines = 1,
            modifier = Modifier.width(width = 30.dp)
        )
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_close),
                contentDescription = stringResource(resource = Res.string.remove_value, food.name),
                tint = colors.muted,
                modifier = Modifier.size(size = 16.dp)
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            CommonInputField(
                modifier = Modifier.fillMaxWidth().height(height = 48.dp),
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(resource = Res.string.search_ingredient),
                horizontalPadding = 40.dp
            )
            Icon(
                painter = painterResource(resource = Res.drawable.ic_search),
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier
                    .align(alignment = Alignment.CenterStart)
                    .padding(start = 14.dp)
                    .size(size = 18.dp)
            )
        }
    }
}

@Composable
private fun AvailableIngredientRow(
    food: Food,
    onAdd: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onAdd)
            .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width = 4.dp)
                .height(height = 28.dp)
                .background(color = food.dominantMacro.stripeColor(), shape = RoundedCornerShape(size = 2.dp))
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = food.name,
                style = typography.bold14,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatPortion(amount = food.weight, storedUnit = food.unit) +
                    " · " + stringResource(resource = Res.string.kcal, food.macros.calories),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_plus),
                contentDescription = stringResource(resource = Res.string.add_value, food.name),
                tint = colors.accentInk,
                modifier = Modifier.size(size = 18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreateRecipeScreen() {
    MacroTrackTheme {
        CreateRecipeScreen(
            viewState = CreateRecipeViewState(
                recipeName = "Chicken rice bowl",
                selectedIngredients = listOf(dummyFood),
                filteredIngredients = listOf(dummyFood, dummyFood.copy(name = "Jasmine rice"))
            ),
            callbacks = CreateRecipeCallbacks.default(),
            onBack = {}
        )
    }
}
