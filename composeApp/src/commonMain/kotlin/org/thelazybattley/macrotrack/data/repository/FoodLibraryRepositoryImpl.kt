package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import macrotrack.composeapp.generated.resources.Res
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.LibraryCategory
import org.thelazybattley.macrotrack.domain.model.LibraryFood
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.repository.FoodLibraryRepository

/**
 * Reads the food library bundled with the app: everyday foods with per-serving values from USDA FoodData Central
 * (SR Legacy, public domain). tools/food_library builds the file.
 */
class FoodLibraryRepositoryImpl : FoodLibraryRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var foods: List<LibraryFood>? = null

    override suspend fun getFoods(): List<LibraryFood> = mutex.withLock {
        foods ?: json.decodeFromString<List<LibraryFoodDto>>(string = Res.readBytes(path = FILE).decodeToString())
            .mapNotNull { it.toLibraryFood() }
            .sortedBy { it.food.name.lowercase() }
            .also { foods = it }
    }

    private fun LibraryFoodDto.toLibraryFood(): LibraryFood? {
        val category = LibraryCategory.entries.find { it.key == category } ?: return null
        return LibraryFood(
            food = Food(
                macros = FoodMacros(calories = calories, protein = protein, carbs = carbs, fat = fat),
                name = name,
                weight = serving,
                dominantMacro = listOf(
                    MacroType.PROTEIN to protein * MacroType.PROTEIN.calories,
                    MacroType.CARBS to carbs * MacroType.CARBS.calories,
                    MacroType.FAT to fat * MacroType.FAT.calories
                ).maxBy { it.second }.first,
                unit = unit
            ),
            category = category,
            isPopular = popular,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium
        )
    }

    @Serializable
    private data class LibraryFoodDto(
        val name: String,
        val category: String,
        val popular: Boolean = false,
        val serving: Double,
        val unit: String,
        val calories: Int,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val fiber: Double? = null,
        val sugar: Double? = null,
        val sodium: Int? = null
    )

    private companion object {
        const val FILE = "files/food_library.json"
    }
}
