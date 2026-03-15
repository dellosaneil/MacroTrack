package org.thelazybattley.macrotrack.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.thelazybattley.macrotrack.data.local.dao.FoodDao
import org.thelazybattley.macrotrack.data.local.dao.RecipeDao
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.typeconverters.RoomConverters

@Database(entities = [FoodEntity::class, RecipeEntity::class], version = 2, exportSchema = true)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
