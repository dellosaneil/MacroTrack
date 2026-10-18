package org.thelazybattley.macrotrack.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.dao.BackupDao
import org.thelazybattley.macrotrack.data.local.dao.FavoriteDao
import org.thelazybattley.macrotrack.data.local.dao.FoodDao
import org.thelazybattley.macrotrack.data.local.dao.FoodLogDao
import org.thelazybattley.macrotrack.data.local.dao.RecipeDao
import org.thelazybattley.macrotrack.data.local.dao.UserDetailsDao
import org.thelazybattley.macrotrack.data.local.dao.DailyGoalDao
import org.thelazybattley.macrotrack.data.local.dao.MeasurementDao
import org.thelazybattley.macrotrack.data.local.dao.WeightDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.data.local.typeconverters.RoomConverters

@Database(
    entities = [FoodEntity::class, RecipeEntity::class, UserDetailsEntity::class, FoodLogEntity::class, WeightEntity::class, MeasurementEntity::class, DailyGoalEntity::class, FavoriteEntity::class, AppFlagEntity::class],
    version = 13, exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
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
    abstract fun measurementDao(): MeasurementDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun appFlagDao(): AppFlagDao
    abstract fun backupDao(): BackupDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE FoodEntity ADD COLUMN unit TEXT NOT NULL DEFAULT 'g'"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE UserDetailsEntity ADD COLUMN targetWeight REAL"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE UserDetailsEntity ADD COLUMN targetStartWeight REAL"
                )
                connection.execSQL(
                    "ALTER TABLE UserDetailsEntity ADD COLUMN targetSetDate TEXT"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `MeasurementEntity` (`date` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                        "`valueCm` REAL NOT NULL, `note` TEXT, PRIMARY KEY(`date`, `type`))"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `DailyGoalEntity` (`date` TEXT NOT NULL, `goal` TEXT NOT NULL, " +
                        "`calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, " +
                        "`fat` INTEGER NOT NULL, PRIMARY KEY(`date`))"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `FavoriteEntity` (`key` TEXT NOT NULL, PRIMARY KEY(`key`))"
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `AppFlagEntity` (`key` TEXT NOT NULL, PRIMARY KEY(`key`))"
                )
                // People who already set up the app know their way around, so they skip the first-run tour.
                connection.execSQL(
                    "INSERT OR IGNORE INTO `AppFlagEntity` (`key`) SELECT 'tour_completed' FROM `UserDetailsEntity` LIMIT 1"
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `AppFlagEntity` ADD COLUMN `value` TEXT")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `FoodEntity` ADD COLUMN `barcode` TEXT")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `FoodLogEntity` ADD COLUMN `isQuickAdd` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Adds the weight-change pace; everyone who set up the app before keeps the old ±300 kcal (1.2 kg a month). */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `UserDetailsEntity` ADD COLUMN `goalPace` REAL NOT NULL DEFAULT 1.2")
            }
        }
    }

}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
