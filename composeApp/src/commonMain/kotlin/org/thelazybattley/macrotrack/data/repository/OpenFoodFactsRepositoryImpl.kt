package org.thelazybattley.macrotrack.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.OnlineFood
import org.thelazybattley.macrotrack.domain.model.OnlineFoodSource
import org.thelazybattley.macrotrack.domain.model.OnlineNutrient
import org.thelazybattley.macrotrack.domain.model.OnlineSearchPage
import org.thelazybattley.macrotrack.domain.model.ScannedProduct
import org.thelazybattley.macrotrack.domain.repository.ProductRepository
import kotlin.math.roundToInt

/** Looks products up in Open Food Facts (https://openfoodfacts.org), a free, open database of packaged foods. */
class OpenFoodFactsRepositoryImpl(private val client: HttpClient) : ProductRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getProduct(barcode: String): Result<ScannedProduct?> =
        getOnlineFood(barcode = barcode).map { it?.toScannedProduct() }

    override suspend fun getOnlineFood(barcode: String): Result<OnlineFood?> = request {
        val response = client.get(urlString = "$PRODUCT_URL/$barcode?fields=$PRODUCT_FIELDS") { named() }
        when (response.status) {
            HttpStatusCode.NotFound -> null
            HttpStatusCode.OK -> json.parseToJsonElement(string = response.bodyAsText())
                .jsonObject["product"]
                ?.let { it as? JsonObject }
                ?.toOnlineFood(barcode = barcode)
            else -> error("Open Food Facts answered ${response.status}")
        }
    }

    override suspend fun searchProducts(query: String, page: Int): Result<OnlineSearchPage> = request {
        val response: HttpResponse = client.get(urlString = SEARCH_URL) {
            named()
            parameter(key = "q", value = query)
            parameter(key = "page", value = page)
            parameter(key = "page_size", value = PAGE_SIZE)
            parameter(key = "fields", value = SEARCH_FIELDS)
        }
        if (response.status != HttpStatusCode.OK) error("Open Food Facts answered ${response.status}")
        val body = json.parseToJsonElement(string = response.bodyAsText()).jsonObject
        val foods = (body["hits"] as? JsonArray).orEmpty()
            .mapNotNull { hit ->
                val product = hit as? JsonObject ?: return@mapNotNull null
                val barcode = product.text(key = "code") ?: return@mapNotNull null
                product.toOnlineFood(barcode = barcode)
            }
            .distinctBy { it.barcode }
        OnlineSearchPage(foods = foods, totalCount = body.number(key = "count")?.toInt() ?: foods.size)
    }

    private suspend fun <T> request(block: suspend () -> T): Result<T> = try {
        Result.success(value = block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception = exception)
    }

    // Open Food Facts asks every app to name itself.
    private fun HttpRequestBuilder.named() {
        header(key = HttpHeaders.UserAgent, value = USER_AGENT)
    }

    private fun JsonObject.toOnlineFood(barcode: String): OnlineFood? {
        val nutriments = this["nutriments"] as? JsonObject ?: return null
        val protein = nutriments.number(key = "proteins_100g")
        val carbs = nutriments.number(key = "carbohydrates_100g")
        val fat = nutriments.number(key = "fat_100g")
        if (protein == null && carbs == null && fat == null) return null
        val calories = nutriments.number(key = "energy-kcal_100g")
            ?: nutriments.number(key = "energy_100g")?.let { kilojoules -> kilojoules / KILOJOULES_PER_KCAL }
            ?: ((protein ?: 0.0) * 4 + (carbs ?: 0.0) * 4 + (fat ?: 0.0) * 9)
        val brand = brand()
        val name = text(key = "product_name") ?: text(key = "product_name_en") ?: text(key = "generic_name") ?: brand ?: return null
        val macros = FoodMacros(
            calories = calories.roundToInt(),
            protein = protein ?: 0.0,
            carbs = carbs ?: 0.0,
            fat = fat ?: 0.0
        )
        val fiber = nutriments.number(key = "fiber_100g")
        val sugar = nutriments.number(key = "sugars_100g")
        val sodium = nutriments.number(key = "sodium_100g")?.let { grams -> grams * MILLIGRAMS_PER_GRAM }
        val missing = listOf(
            OnlineNutrient.PROTEIN to protein,
            OnlineNutrient.CARBS to carbs,
            OnlineNutrient.FAT to fat,
            OnlineNutrient.FIBER to fiber,
            OnlineNutrient.SUGAR to sugar,
            OnlineNutrient.SODIUM to sodium
        ).filter { it.second == null }.map { it.first }
        val isLabelComplete = (this["states_tags"] as? JsonArray)?.any { (it as? JsonPrimitive)?.contentOrNull == NUTRITION_COMPLETED } == true
        return OnlineFood(
            food = Food(
                macros = macros,
                name = name.trim(),
                weight = OnlineFood.PER_100,
                dominantMacro = macros.dominantMacro(),
                unit = unit(),
                barcode = barcode
            ),
            brand = brand,
            source = if (brand != null && isLabelComplete && OnlineNutrient.PROTEIN !in missing) OnlineFoodSource.BRAND else OnlineFoodSource.COMMUNITY,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium,
            servingLabel = text(key = "serving_size"),
            servingGrams = number(key = "serving_quantity")?.takeIf { it > 0 },
            missing = missing
        )
    }

    /** A product sends its brands as "A, B"; search results send a list. The first one is shown. */
    private fun JsonObject.brand(): String? {
        val first = when (val brands = this["brands"]) {
            is JsonArray -> (brands.firstOrNull() as? JsonPrimitive)?.contentOrNull
            is JsonPrimitive -> brands.contentOrNull?.substringBefore(delimiter = ",")
            else -> null
        }
        return first?.trim()?.ifBlank { null }
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
        const val PRODUCT_URL = "https://world.openfoodfacts.org/api/v2/product"
        const val SEARCH_URL = "https://search.openfoodfacts.org/search"
        const val PRODUCT_FIELDS = "product_name,generic_name,brands,nutriments,serving_size,serving_quantity," +
            "serving_quantity_unit,product_quantity_unit,states_tags"
        const val SEARCH_FIELDS = "code,product_name,product_name_en,brands,nutriments,states_tags"
        const val PAGE_SIZE = 20
        const val NUTRITION_COMPLETED = "en:nutrition-facts-completed"
        const val USER_AGENT = "FitMetric/1.0"
        const val KILOJOULES_PER_KCAL = 4.184
        const val MILLIGRAMS_PER_GRAM = 1000.0
        const val GRAMS = "g"
        const val MILLILITRES = "ml"
    }
}
