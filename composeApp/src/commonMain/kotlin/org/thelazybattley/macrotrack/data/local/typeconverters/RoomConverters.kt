package org.thelazybattley.macrotrack.data.local.typeconverters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.domain.model.Ingredient

class RoomConverters {
    @TypeConverter
    fun fromFoodEntityList(value: List<FoodEntity>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toFoodEntityList(value: String): List<FoodEntity> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> {
        return Json.decodeFromString(value)
    }
}
