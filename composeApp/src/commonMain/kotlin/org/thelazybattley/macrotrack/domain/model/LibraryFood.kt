package org.thelazybattley.macrotrack.domain.model

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.library_category_dairy
import macrotrack.composeapp.generated.resources.library_category_drinks
import macrotrack.composeapp.generated.resources.library_category_fats_oils
import macrotrack.composeapp.generated.resources.library_category_fruit
import macrotrack.composeapp.generated.resources.library_category_grains
import macrotrack.composeapp.generated.resources.library_category_legumes_nuts
import macrotrack.composeapp.generated.resources.library_category_meals
import macrotrack.composeapp.generated.resources.library_category_protein
import macrotrack.composeapp.generated.resources.library_category_sweets_snacks
import macrotrack.composeapp.generated.resources.library_category_vegetables
import org.jetbrains.compose.resources.StringResource

/**
 * A built-in, read-only food from the food library. [food] holds the macros for one serving ([Food.weight] in
 * [Food.unit]). Library foods are never saved to the user's foods; logging one stores only its name and portion.
 */
data class LibraryFood(
    val food: Food,
    val category: LibraryCategory,
    val isPopular: Boolean,
    val fiber: Double?,
    val sugar: Double?,
    /** Milligrams per serving. */
    val sodium: Int?
)

enum class LibraryCategory(
    val title: StringResource,
    /** The category's name in food_library.json. */
    val key: String
) {
    PROTEIN(title = Res.string.library_category_protein, key = "Protein"),
    FRUIT(title = Res.string.library_category_fruit, key = "Fruit"),
    VEGETABLES(title = Res.string.library_category_vegetables, key = "Vegetables"),
    GRAINS(title = Res.string.library_category_grains, key = "Grains"),
    DAIRY(title = Res.string.library_category_dairy, key = "Dairy"),
    LEGUMES_NUTS(title = Res.string.library_category_legumes_nuts, key = "Legumes & nuts"),
    MEALS(title = Res.string.library_category_meals, key = "Meals"),
    DRINKS(title = Res.string.library_category_drinks, key = "Drinks"),
    SWEETS_SNACKS(title = Res.string.library_category_sweets_snacks, key = "Sweets & snacks"),
    FATS_OILS(title = Res.string.library_category_fats_oils, key = "Fats & oils")
}
