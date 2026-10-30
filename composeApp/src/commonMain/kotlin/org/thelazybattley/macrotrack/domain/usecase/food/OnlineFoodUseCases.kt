package org.thelazybattley.macrotrack.domain.usecase.food

import kotlinx.coroutines.flow.first
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.OnlineFood
import org.thelazybattley.macrotrack.domain.model.OnlineSearchPage
import org.thelazybattley.macrotrack.domain.model.uniqueFoodName
import org.thelazybattley.macrotrack.domain.repository.FoodRepository
import org.thelazybattley.macrotrack.domain.repository.ProductRepository

/** Searches packaged foods online; only the typed words are sent. Fails when offline. */
class SearchOnlineFoodsUseCase(
    private val productRepository: ProductRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(query: String, page: Int): Result<OnlineSearchPage> =
        productRepository.searchProducts(query = query.trim(), page = page).also { result ->
            if (page == 1) {
                telemetry.logEvent(
                    name = TelemetryEvent.ONLINE_SEARCH,
                    params = mapOf(TelemetryEvent.PARAM_RESULT to if (result.isSuccess) "ok" else "unavailable")
                )
            }
        }
}

/** An online food with its label serving, which searches don't send; [food] as it was when that can't be had. */
class GetOnlineFoodDetailsUseCase(private val productRepository: ProductRepository) {

    suspend operator fun invoke(food: OnlineFood): OnlineFood =
        productRepository.getOnlineFood(barcode = food.barcode).getOrNull()?.let { details ->
            // Keeps the name and numbers already shown; only the serving is new.
            food.copy(servingLabel = details.servingLabel, servingGrams = details.servingGrams, food = food.food.copy(unit = details.food.unit))
        } ?: food
}

/** What saving an online food did: the food in My foods, and whether it was added just now (so Undo can take it away). */
data class SavedOnlineFood(val food: Food, val isNew: Boolean)

/**
 * Keeps an online food in My foods, so it works offline next time. One already saved with the same barcode is used
 * as it is, with any changes the user made to it.
 */
class SaveOnlineFoodUseCase(
    private val foodRepository: FoodRepository,
    private val insertFoodUseCase: InsertFoodUseCase,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(food: OnlineFood): SavedOnlineFood {
        foodRepository.getFoodByBarcode(barcode = food.barcode)?.let { return SavedOnlineFood(food = it, isNew = false) }
        val taken = foodRepository.getAllFoods().first().map { it.name }
        val saved = food.food.copy(
            name = uniqueFoodName(name = food.food.name, brand = food.brand, barcode = food.barcode, takenNames = taken)
        )
        insertFoodUseCase(food = saved)
        telemetry.logEvent(name = TelemetryEvent.ONLINE_FOOD_SAVED, params = mapOf(TelemetryEvent.PARAM_TYPE to food.source.name.lowercase()))
        return SavedOnlineFood(food = saved, isNew = true)
    }
}
