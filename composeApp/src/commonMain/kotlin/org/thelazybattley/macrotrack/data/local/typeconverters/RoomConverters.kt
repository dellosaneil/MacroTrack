package org.thelazybattley.macrotrack.data.local.typeconverters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.IngredientEntity

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
    fun fromIngredientEntityList(value: List<IngredientEntity>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toIngredientEntityList(value: String): List<IngredientEntity> {
        return Json.decodeFromString(value)
    }
}
