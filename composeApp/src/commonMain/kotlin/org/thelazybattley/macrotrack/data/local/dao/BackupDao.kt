package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity

/**
 * Reads every table at once for a backup, swaps them all for a backup's contents in one transaction, and empties
 * them when the user deletes all their data.
 */
@Dao
abstract class BackupDao {

    @Query("SELECT * FROM FoodEntity")
    abstract suspend fun getFoods(): List<FoodEntity>

    @Query("SELECT * FROM RecipeEntity")
    abstract suspend fun getRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM UserDetailsEntity")
    abstract suspend fun getUserDetails(): List<UserDetailsEntity>

    @Query("SELECT * FROM FoodLogEntity")
    abstract suspend fun getFoodLogs(): List<FoodLogEntity>

    @Query("SELECT * FROM WeightEntity")
    abstract suspend fun getWeights(): List<WeightEntity>

    @Query("SELECT * FROM MeasurementEntity")
    abstract suspend fun getMeasurements(): List<MeasurementEntity>

    @Query("SELECT * FROM DailyGoalEntity")
    abstract suspend fun getDailyGoals(): List<DailyGoalEntity>

    @Query("SELECT * FROM FavoriteEntity")
    abstract suspend fun getFavorites(): List<FavoriteEntity>

    @Query("SELECT * FROM AppFlagEntity")
    abstract suspend fun getAppFlags(): List<AppFlagEntity>

    @Query("DELETE FROM FoodEntity")
    protected abstract suspend fun deleteFoods()

    @Query("DELETE FROM RecipeEntity")
    protected abstract suspend fun deleteRecipes()

    @Query("DELETE FROM UserDetailsEntity")
    protected abstract suspend fun deleteUserDetails()

    @Query("DELETE FROM FoodLogEntity")
    abstract suspend fun deleteFoodLogs()

    @Query("DELETE FROM WeightEntity")
    protected abstract suspend fun deleteWeights()

    @Query("DELETE FROM MeasurementEntity")
    protected abstract suspend fun deleteMeasurements()

    @Query("DELETE FROM DailyGoalEntity")
    protected abstract suspend fun deleteDailyGoals()

    @Query("DELETE FROM FavoriteEntity")
    protected abstract suspend fun deleteFavorites()

    @Query("DELETE FROM AppFlagEntity WHERE `key` NOT IN (:keepKeys)")
    protected abstract suspend fun deleteAppFlagsExcept(keepKeys: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertFoods(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertUserDetails(userDetails: List<UserDetailsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertFoodLogs(foodLogs: List<FoodLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertWeights(weights: List<WeightEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertMeasurements(measurements: List<MeasurementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertDailyGoals(dailyGoals: List<DailyGoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAppFlags(appFlags: List<AppFlagEntity>)

    @Transaction
    open suspend fun deleteBodyRecords() {
        deleteWeights()
        deleteMeasurements()
    }

    @Transaction
    open suspend fun deleteSavedFoods() {
        deleteFoods()
        deleteRecipes()
        deleteFavorites()
    }

    /** Goals, the user's details and app settings; flags named in [keepFlagKeys] stay. */
    @Transaction
    open suspend fun deleteGoalsAndSettings(keepFlagKeys: List<String>) {
        deleteDailyGoals()
        deleteUserDetails()
        deleteAppFlagsExcept(keepKeys = keepFlagKeys)
    }

    /**
     * Replaces everything with [snapshot]. App flags named in [deviceFlagKeys] (Pro, the trial and backup
     * bookkeeping) belong to this phone, so they're kept and never taken from the backup.
     */
    @Transaction
    open suspend fun replaceAll(snapshot: DatabaseSnapshot, deviceFlagKeys: List<String>) {
        deleteFoods()
        deleteRecipes()
        deleteUserDetails()
        deleteFoodLogs()
        deleteWeights()
        deleteMeasurements()
        deleteDailyGoals()
        deleteFavorites()
        deleteAppFlagsExcept(keepKeys = deviceFlagKeys)
        insertFoods(foods = snapshot.foods)
        insertRecipes(recipes = snapshot.recipes)
        insertUserDetails(userDetails = snapshot.userDetails)
        insertFoodLogs(foodLogs = snapshot.foodLogs)
        insertWeights(weights = snapshot.weights)
        insertMeasurements(measurements = snapshot.measurements)
        insertDailyGoals(dailyGoals = snapshot.dailyGoals)
        insertFavorites(favorites = snapshot.favorites)
        insertAppFlags(appFlags = snapshot.appFlags.filter { it.key !in deviceFlagKeys })
    }
}

/** Every table's rows. */
data class DatabaseSnapshot(
    val foods: List<FoodEntity>,
    val recipes: List<RecipeEntity>,
    val userDetails: List<UserDetailsEntity>,
    val foodLogs: List<FoodLogEntity>,
    val weights: List<WeightEntity>,
    val measurements: List<MeasurementEntity>,
    val dailyGoals: List<DailyGoalEntity>,
    val favorites: List<FavoriteEntity>,
    val appFlags: List<AppFlagEntity>
)
