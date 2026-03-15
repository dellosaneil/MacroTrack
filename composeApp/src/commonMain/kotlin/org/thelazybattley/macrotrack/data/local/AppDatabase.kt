package org.thelazybattley.macrotrack.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.thelazybattley.macrotrack.data.local.dao.dao.FoodDao
import org.thelazybattley.macrotrack.data.local.entity.food.FoodEntity

@Database(entities = [FoodEntity::class], version = 1, exportSchema = true)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
