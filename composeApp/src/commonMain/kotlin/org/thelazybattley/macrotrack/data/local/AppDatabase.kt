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
import org.thelazybattley.macrotrack.data.local.dao.ActivityDao
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.dao.BackupDao
import org.thelazybattley.macrotrack.data.local.dao.FavoriteDao
import org.thelazybattley.macrotrack.data.local.dao.FoodDao
import org.thelazybattley.macrotrack.data.local.dao.FoodLogDao
import org.thelazybattley.macrotrack.data.local.dao.MealPlanDao
import org.thelazybattley.macrotrack.data.local.dao.RecipeDao
import org.thelazybattley.macrotrack.data.local.dao.UserDetailsDao
import org.thelazybattley.macrotrack.data.local.dao.DailyGoalDao
import org.thelazybattley.macrotrack.data.local.dao.MeasurementDao
import org.thelazybattley.macrotrack.data.local.dao.ProgressPhotoDao
import org.thelazybattley.macrotrack.data.local.dao.WeightDao
import org.thelazybattley.macrotrack.data.local.entity.ActivitySessionEntity
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.PhotoAlbumEntity
import org.thelazybattley.macrotrack.data.local.entity.PlannedFoodEntity
import org.thelazybattley.macrotrack.data.local.entity.ProgressPhotoEntity
import org.thelazybattley.macrotrack.data.local.entity.RepeatingMealEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.StepHourEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.data.local.typeconverters.RoomConverters

@Database(
    entities = [FoodEntity::class, RecipeEntity::class, UserDetailsEntity::class, FoodLogEntity::class, WeightEntity::class, MeasurementEntity::class, DailyGoalEntity::class, FavoriteEntity::class, AppFlagEntity::class, PlannedFoodEntity::class, RepeatingMealEntity::class, StepHourEntity::class, ActivitySessionEntity::class, PhotoAlbumEntity::class, ProgressPhotoEntity::class],
    version = 19, exportSchema = true,
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
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun activityDao(): ActivityDao
    abstract fun progressPhotoDao(): ProgressPhotoDao

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

        /** Adds the burn measured by a weekly check-in; nobody has one yet, so targets stay as they were. */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `UserDetailsEntity` ADD COLUMN `measuredMaintenance` INTEGER")
            }
        }

        /** Adds what protein and fat are based on, body fat and custom targets; everyone keeps calculated targets. */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `UserDetailsEntity` ADD COLUMN `macroBasis` TEXT NOT NULL DEFAULT 'TARGET_WEIGHT'")
                connection.execSQL("ALTER TABLE `UserDetailsEntity` ADD COLUMN `bodyFat` REAL")
                listOf("customCalories", "customProtein", "customCarbs", "customFat").forEach { column ->
                    connection.execSQL("ALTER TABLE `UserDetailsEntity` ADD COLUMN `$column` INTEGER")
                }
            }
        }

        /** Adds the meal plan: food planned for later days, and repeating meals that fill it in. */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `PlannedFoodEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`date` TEXT NOT NULL, `mealType` TEXT NOT NULL, `foodName` TEXT NOT NULL, `calories` INTEGER NOT NULL, " +
                        "`protein` REAL NOT NULL, `carbs` REAL NOT NULL, `fat` REAL NOT NULL, `weight` REAL NOT NULL, " +
                        "`dominantMacro` TEXT NOT NULL, `isQuickAdd` INTEGER NOT NULL, `repeatingMealId` INTEGER, " +
                        "`status` TEXT NOT NULL, `logId` INTEGER)"
                )
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `RepeatingMealEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `mealType` TEXT NOT NULL, `days` TEXT NOT NULL, `foods` TEXT NOT NULL, " +
                        "`startDate` TEXT NOT NULL, `endDate` TEXT, `maxTimes` INTEGER, `isAutoLog` INTEGER NOT NULL, " +
                        "`isActive` INTEGER NOT NULL, `updatedOn` TEXT NOT NULL)"
                )
            }
        }

        /** Adds steps counted per hour, and walks and runs recorded with the map. */
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `StepHourEntity` (`date` TEXT NOT NULL, `hour` INTEGER NOT NULL, " +
                        "`steps` INTEGER NOT NULL, PRIMARY KEY(`date`, `hour`))"
                )
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ActivitySessionEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`type` TEXT NOT NULL, `name` TEXT, `startMillis` INTEGER NOT NULL, `endMillis` INTEGER NOT NULL, " +
                        "`movingSeconds` INTEGER NOT NULL, `distanceMeters` REAL NOT NULL, `steps` INTEGER NOT NULL, " +
                        "`climbMeters` REAL NOT NULL, `route` TEXT NOT NULL)"
                )
            }
        }

        /** Adds progress photo albums and the photos in them; the images themselves are files beside the database. */
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `PhotoAlbumEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `reminderDays` TEXT NOT NULL, `isDateAndWeightShown` INTEGER NOT NULL, " +
                        "`createdAtMillis` INTEGER NOT NULL)"
                )
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ProgressPhotoEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`albumId` INTEGER NOT NULL, `date` TEXT NOT NULL, `takenOn` TEXT, `pose` TEXT NOT NULL, " +
                        "`note` TEXT, `weightDate` TEXT, `fileName` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL)"
                )
            }
        }

        /** Adds a time to photo album reminders; albums made before keep 6 PM. */
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `PhotoAlbumEntity` ADD COLUMN `reminderMinutes` INTEGER NOT NULL DEFAULT 1080")
            }
        }
    }

}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
