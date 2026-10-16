package org.thelazybattley.macrotrack.features.foodlibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.LibraryCategory
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.QUERY

class FoodLibraryViewModel(
    private val getFoodLibraryUseCase: GetFoodLibraryUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), FoodLibraryCallbacks {

    private val _state = MutableStateFlow(
        value = FoodLibraryViewState(
            mealType = MealType.entries.find { it.name == savedStateHandle[MEAL_TYPE] } ?: MealType.BREAKFAST,
            query = savedStateHandle.get<String>(key = QUERY).orEmpty()
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getFoodLibraryUseCase().collect { foods ->
                _state.update { it.copy(foods = foods) }
            }
        }
    }

    override fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    override fun onCategorySelected(category: LibraryCategory?) {
        _state.update { it.copy(category = category, query = "") }
    }

    override fun onToggleFood(name: String) {
        _state.update { currentState ->
            currentState.copy(
                selection = if (name in currentState.selection) currentState.selection - name else currentState.selection + name
            )
        }
    }

    override fun onOpenFood(name: String) {
        _state.update { it.copy(openFoodName = name) }
    }

    override fun onCloseFood() {
        _state.update { it.copy(openFoodName = null) }
    }

    override fun onAddOpenFood() {
        _state.update { currentState ->
            val name = currentState.openFoodName ?: return@update currentState
            currentState.copy(
                selection = if (name in currentState.selection) currentState.selection else currentState.selection + name,
                openFoodName = null
            )
        }
    }

    override fun onMakeOwnVersion() {
        val name = _state.value.openFoodName ?: return
        _state.update {
            it.copy(
                openFoodName = null,
                destinationRoute = AppDestinations.Root.CreateFood.createRoute(foodName = "", libraryFood = name)
            )
        }
    }

    override fun onCreateFood() {
        _state.update {
            it.copy(destinationRoute = AppDestinations.Root.CreateFood.createRoute(foodName = "", prefillName = it.query.trim()))
        }
    }

    override fun onScan() {
        _state.update { it.copy(destinationRoute = AppDestinations.Root.ScanBarcode.createRoute(mealType = it.mealType.name)) }
    }

    /** Logs one serving of each picked food. Only the name and portion are logged; nothing is added to My foods. */
    override fun onAddSelection() {
        val currentState = _state.value
        if (currentState.selection.isEmpty() || currentState.isSaved) return
        viewModelScope.launch {
            currentState.selection.mapNotNull { name -> currentState.foods.find { it.food.name == name }?.food }.forEach { food ->
                insertFoodLogUseCase(
                    foodName = food.name,
                    carbs = food.macros.carbs,
                    fat = food.macros.fat,
                    calories = food.macros.calories,
                    protein = food.macros.protein,
                    mealType = currentState.mealType,
                    weight = food.weight,
                    dominantMacro = food.dominantMacro
                )
            }
            _state.update { it.copy(isSaved = true) }
        }
    }

    override fun resetNavigateScreen() {
        _state.update { it.copy(destinationRoute = null) }
    }
}
