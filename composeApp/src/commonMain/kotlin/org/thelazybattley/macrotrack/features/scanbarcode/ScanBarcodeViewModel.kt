package org.thelazybattley.macrotrack.features.scanbarcode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.BarcodeLookup
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.InsertFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.LookUpBarcodeUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetRemainingCaloriesUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.features.scanbarcode.ui.isCameraScanSupported
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.LOG_DATE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.logDateOf
import kotlin.math.roundToInt

class ScanBarcodeViewModel(
    private val lookUpBarcodeUseCase: LookUpBarcodeUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val insertFoodUseCase: InsertFoodUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val getRemainingCaloriesUseCase: GetRemainingCaloriesUseCase,
    private val scannedFoodRequest: ScannedFoodRequest,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ScanBarcodeCallbacks {

    private val _state = MutableStateFlow(
        value = ScanBarcodeViewState(
            mealType = MealType.entries.find { it.name == savedStateHandle[MEAL_TYPE] } ?: MealType.BREAKFAST,
            logDate = logDateOf(argument = savedStateHandle[LOG_DATE]),
            // Without a camera scanner the screen opens on typing the number.
            mode = if (isCameraScanSupported) ScanMode.SCANNING else ScanMode.MANUAL
        )
    )
    val state = _state.asStateFlow()

    private var savedFoods: List<Food> = emptyList()
    private var lookUpJob: Job? = null
    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            getAllFoodUseCase().collect { foods ->
                savedFoods = foods
                onSavedFoodsChanged(foods = foods)
            }
        }
        viewModelScope.launch {
            getRemainingCaloriesUseCase(date = _state.value.logDate).collect { remainingCalories ->
                _state.update { it.copy(remainingCalories = remainingCalories) }
            }
        }
    }

    override fun onBarcodeDetected(raw: String) {
        if (_state.value.mode != ScanMode.SCANNING) return
        val barcode = normalizeBarcode(raw = raw) ?: return
        lookUp(barcode = barcode)
    }

    override fun onOpenManualEntry() {
        lookUpJob?.cancel()
        _state.update { it.copy(mode = ScanMode.MANUAL, manualBarcode = "") }
    }

    override fun onManualBarcodeChanged(value: String) {
        _state.update { it.copy(manualBarcode = value.filter(Char::isDigit).take(n = MAX_BARCODE_LENGTH)) }
    }

    override fun onLookUpManualBarcode() {
        val barcode = normalizeBarcode(raw = _state.value.manualBarcode) ?: return
        lookUp(barcode = barcode)
    }

    override fun onScanAgain() {
        lookUpJob?.cancel()
        _state.update {
            it.copy(
                mode = if (isCameraScanSupported) ScanMode.SCANNING else ScanMode.MANUAL,
                barcode = null,
                manualBarcode = "",
                scanned = null,
                name = "",
                portionMacros = null
            )
        }
    }

    override fun onToggleTorch() {
        _state.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    override fun onNameChanged(value: String) {
        if (_state.value.scanned?.isInMyFoods != false) return
        _state.update { it.copy(name = value) }
    }

    override fun onAmountChanged(amount: Double) {
        if (amount <= 0) return
        _state.update { currentState ->
            val food = currentState.scanned?.food ?: return@update currentState
            currentState.copy(amount = amount, portionMacros = portionMacros(food = food, amount = amount))
        }
    }

    override fun onAddToMeal() {
        val currentState = _state.value
        if (currentState.isSaved) return
        saveScannedFood { food ->
            val macros = portionMacros(food = food, amount = currentState.amount)
            insertFoodLogUseCase(
                foodName = food.name,
                mealType = currentState.mealType,
                calories = macros.calories,
                fat = macros.fat,
                carbs = macros.carbs,
                protein = macros.protein,
                weight = currentState.amount,
                dominantMacro = food.dominantMacro,
                date = currentState.logDate
            )
            _state.update { it.copy(isSaved = true) }
        }
    }

    override fun onLater() {
        saveScannedFood { food ->
            scannedFoodRequest.request(foodName = food.name)
            _state.update { it.copy(isClosed = true) }
        }
    }

    override fun onEditFood() {
        saveScannedFood { food ->
            _state.update { it.copy(destinationRoute = AppDestinations.Root.CreateFood.createRoute(foodName = food.name)) }
        }
    }

    override fun onCreateFood() {
        val barcode = _state.value.barcode ?: return
        _state.update {
            it.copy(destinationRoute = AppDestinations.Root.CreateFood.createRoute(foodName = "", barcode = barcode))
        }
    }

    override fun resetNavigateScreen() {
        _state.update { it.copy(destinationRoute = null) }
    }

    private fun lookUp(barcode: String) {
        lookUpJob?.cancel()
        _state.update { it.copy(mode = ScanMode.LOOKING_UP, barcode = barcode, scanned = null) }
        lookUpJob = viewModelScope.launch {
            val scanned = when (val result = lookUpBarcodeUseCase(barcode = barcode)) {
                is BarcodeLookup.Saved -> savedScannedFood(food = result.food)
                // Only added to the user's foods once they save or log it.
                is BarcodeLookup.Product -> ScannedFood(
                    food = result.product.food,
                    brand = result.product.brand,
                    isInMyFoods = false,
                    servingLabel = result.product.servingLabel,
                    servings = productServings(
                        servingLabel = result.product.servingLabel,
                        servingGrams = result.product.servingGrams
                    )
                )
                BarcodeLookup.NotFound -> null
                BarcodeLookup.Unavailable -> {
                    _state.update { it.copy(mode = ScanMode.UNAVAILABLE) }
                    return@launch
                }
            }
            if (scanned == null) {
                _state.update { it.copy(mode = ScanMode.NOT_FOUND) }
            } else {
                showFound(scanned = scanned, amount = scanned.servings.first().amount)
            }
        }
    }

    /**
     * Keeps the sheet in step with saved foods: a food created for a barcode that wasn't found shows up as soon as
     * it's saved, and an edited food shows its new values.
     */
    private fun onSavedFoodsChanged(foods: List<Food>) {
        val currentState = _state.value
        val barcode = currentState.barcode ?: return
        val saved = foods.find { it.barcode == barcode } ?: return
        when (currentState.mode) {
            ScanMode.NOT_FOUND, ScanMode.UNAVAILABLE -> showFound(scanned = savedScannedFood(food = saved), amount = saved.weight)
            ScanMode.FOUND -> currentState.scanned?.takeIf { it.food != saved || !it.isInMyFoods }?.let { scanned ->
                showFound(scanned = scanned.copy(food = saved, isInMyFoods = true), amount = currentState.amount)
            }
            else -> Unit
        }
    }

    private fun showFound(scanned: ScannedFood, amount: Double) {
        _state.update {
            it.copy(
                mode = ScanMode.FOUND,
                scanned = scanned,
                name = scanned.food.name,
                amount = amount,
                portionMacros = portionMacros(food = scanned.food, amount = amount)
            )
        }
    }

    private fun savedScannedFood(food: Food) = ScannedFood(
        food = food,
        brand = null,
        isInMyFoods = true,
        servingLabel = null,
        servings = listOfNotNull(
            ServingOption(amount = food.weight),
            ServingOption(amount = FULL_LABEL_AMOUNT).takeIf {
                food.unit.lowercase() in setOf("g", "ml") && food.weight != FULL_LABEL_AMOUNT
            },
            ServingOption(amount = (food.weight / 2).roundToTenth(), isHalf = true)
        )
    )

    /** The package's serving, 100 g, and half the package's serving; just 100 g when the label has no serving. */
    private fun productServings(servingLabel: String?, servingGrams: Double?): List<ServingOption> =
        if (servingGrams == null) {
            listOf(ServingOption(amount = FULL_LABEL_AMOUNT))
        } else {
            listOfNotNull(
                ServingOption(amount = servingGrams, label = servingLabel),
                ServingOption(amount = FULL_LABEL_AMOUNT).takeIf { servingGrams != FULL_LABEL_AMOUNT },
                ServingOption(amount = (servingGrams / 2).roundToTenth(), isHalf = true)
            )
        }

    private fun portionMacros(food: Food, amount: Double) =
        calculateAdjustMacrosUseCase(portionSize = amount, originalFood = food)

    /**
     * Adds an Open Food Facts product to the user's foods under the name on the sheet, so the next scan finds it
     * offline, then hands the saved food to [onSaved]. A food that's already saved goes straight to [onSaved].
     */
    private fun saveScannedFood(onSaved: suspend (Food) -> Unit) {
        val currentState = _state.value
        val scanned = currentState.scanned ?: return
        if (!currentState.canSave || saveJob?.isActive == true) return
        saveJob = viewModelScope.launch {
            val food = if (scanned.isInMyFoods) {
                scanned.food
            } else {
                savedFoods.find { it.barcode == scanned.food.barcode } ?: scanned.food.copy(
                    name = uniqueName(name = currentState.name.trim(), brand = scanned.brand, barcode = scanned.food.barcode)
                ).also { insertFoodUseCase(food = it) }
            }
            _state.update { it.copy(scanned = scanned.copy(food = food, isInMyFoods = true), name = food.name) }
            onSaved(food)
        }
    }

    /** Foods are keyed by name, so a product never replaces a saved food that happens to share its name. */
    private fun uniqueName(name: String, brand: String?, barcode: String?): String {
        val taken = savedFoods.map { it.name.lowercase() }.toSet()
        return listOfNotNull(
            name,
            brand?.let { "$name ($it)" },
            barcode?.let { "$name (${it.takeLast(n = 4)})" }
        ).firstOrNull { it.lowercase() !in taken } ?: "$name ($barcode)"
    }

    private fun Double.roundToTenth(): Double = (this * 10).roundToInt() / 10.0

    private companion object {
        const val FULL_LABEL_AMOUNT = 100.0
        const val MAX_BARCODE_LENGTH = 14
    }
}
