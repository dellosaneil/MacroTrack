package org.thelazybattley.macrotrack.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.thelazybattley.macrotrack.data.local.dao.FoodDao
import org.thelazybattley.macrotrack.data.local.dao.FoodLogDao
import org.thelazybattley.macrotrack.data.local.dao.RecipeDao
import org.thelazybattley.macrotrack.data.local.dao.UserDetailsDao
import org.thelazybattley.macrotrack.data.local.dao.WeightDao
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.data.local.typeconverters.RoomConverters

@Database(
    entities = [FoodEntity::class, RecipeEntity::class, UserDetailsEntity::class, FoodLogEntity::class, WeightEntity::class],
    version = 2, exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao
    abstract fun userDetailsDao(): UserDetailsDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun weightDao(): WeightDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
