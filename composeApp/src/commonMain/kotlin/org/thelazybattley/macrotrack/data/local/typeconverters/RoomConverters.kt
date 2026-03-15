package org.thelazybattley.macrotrack.data.local.typeconverters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity

class RoomConverters {
    @TypeConverter
    fun fromFoodEntityList(value: List<FoodEntity>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toFoodEntityList(value: String): List<FoodEntity> {
        return Json.decodeFromString(value)
    }
}
