package org.thelazybattley.macrotrack.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.ScannedProduct
import org.thelazybattley.macrotrack.domain.repository.ProductRepository
import kotlin.math.roundToInt

/** Looks products up in Open Food Facts (https://openfoodfacts.org), a free, open database of packaged foods. */
class OpenFoodFactsRepositoryImpl(private val client: HttpClient) : ProductRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getProduct(barcode: String): Result<ScannedProduct?> = try {
        val response = client.get(urlString = "$BASE_URL/$barcode?fields=$FIELDS") {
            // Open Food Facts asks every app to name itself.
            header(key = HttpHeaders.UserAgent, value = USER_AGENT)
        }
        val product = when (response.status) {
            HttpStatusCode.NotFound -> null
            HttpStatusCode.OK -> json.parseToJsonElement(string = response.bodyAsText())
                .jsonObject["product"]
                ?.let { it as? JsonObject }
                ?.toScannedProduct(barcode = barcode)
            else -> error("Open Food Facts answered ${response.status}")
        }
        Result.success(value = product)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception = exception)
    }

    private fun JsonObject.toScannedProduct(barcode: String): ScannedProduct? {
        val nutriments = this["nutriments"] as? JsonObject ?: return null
        val protein = nutriments.number(key = "proteins_100g")
        val carbs = nutriments.number(key = "carbohydrates_100g")
        val fat = nutriments.number(key = "fat_100g")
        if (protein == null && carbs == null && fat == null) return null
        val calories = nutriments.number(key = "energy-kcal_100g")
            ?: nutriments.number(key = "energy_100g")?.let { kilojoules -> kilojoules / KILOJOULES_PER_KCAL }
            ?: ((protein ?: 0.0) * 4 + (carbs ?: 0.0) * 4 + (fat ?: 0.0) * 9)
        val brand = text(key = "brands")?.substringBefore(delimiter = ",")?.trim()?.ifBlank { null }
        val name = text(key = "product_name") ?: text(key = "generic_name") ?: brand ?: return null
        val macros = FoodMacros(
            calories = calories.roundToInt(),
            protein = protein ?: 0.0,
            carbs = carbs ?: 0.0,
            fat = fat ?: 0.0
        )
        return ScannedProduct(
            food = Food(
                macros = macros,
                name = name.trim(),
                weight = PER_100,
                dominantMacro = macros.dominantMacro(),
                unit = unit(),
                barcode = barcode
            ),
            brand = brand,
            servingLabel = text(key = "serving_size"),
            servingGrams = number(key = "serving_quantity")?.takeIf { it > 0 }
        )
    }

    /** Drinks are labelled per 100 ml, everything else per 100 g; Open Food Facts' `_100g` values follow the label. */
    private fun JsonObject.unit(): String {
        val unit = text(key = "serving_quantity_unit") ?: text(key = "product_quantity_unit")
        return if (unit.equals(other = MILLILITRES, ignoreCase = true)) MILLILITRES else GRAMS
    }

    private fun JsonObject.text(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.trim()?.ifBlank { null }

    /** Open Food Facts sends numbers as either JSON numbers or strings. */
    private fun JsonObject.number(key: String): Double? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()?.takeIf { it >= 0 }

    private fun FoodMacros.dominantMacro(): MacroType = listOf(
        MacroType.PROTEIN to protein * 4,
        MacroType.CARBS to carbs * 4,
        MacroType.FAT to fat * 9
    ).maxBy { it.second }.first

    private companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/api/v2/product"
        const val FIELDS = "product_name,generic_name,brands,nutriments,serving_size,serving_quantity,serving_quantity_unit,product_quantity_unit"
        const val USER_AGENT = "MacroTrack/1.0"
        const val KILOJOULES_PER_KCAL = 4.184
        const val PER_100 = 100.0
        const val GRAMS = "g"
        const val MILLILITRES = "ml"
    }
}
