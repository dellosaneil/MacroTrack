package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.FoodEntity

data class Food(
    val macros: FoodMacros,
    val name: String,
    val weight: Double,
    val dominantMacro: MacroType,
    val unit: String,
    val barcode: String? = null
)

fun Food.toEntity() = FoodEntity(
    name = name,
    protein = macros.protein,
    carbs = macros.carbs,
    fat = macros.fat,
    calories = macros.calories,
    weight = weight,
    dominantMacro = dominantMacro.name,
    unit = unit,
    barcode = barcode
)

/**
 * Foods are keyed by name, so a packaged food never replaces a saved food that happens to share its name: it takes
 * its brand, then the end of its barcode, as needed.
 */
fun uniqueFoodName(name: String, brand: String?, barcode: String?, takenNames: Collection<String>): String {
    val taken = takenNames.map { it.lowercase() }.toSet()
    return listOfNotNull(
        name,
        brand?.let { "$name ($it)" },
        barcode?.let { "$name (${it.takeLast(n = 4)})" }
    ).firstOrNull { it.lowercase() !in taken } ?: "$name ($barcode)"
}

val dummyFood = Food(
    name = "Rice",
    weight = 100.0,
    macros = FoodMacros(
        calories = 242,
        carbs = 10.0,
        protein = 34.2,
        fat = 12.3
    ),
    dominantMacro = MacroType.PROTEIN,
    unit = "g"
)
