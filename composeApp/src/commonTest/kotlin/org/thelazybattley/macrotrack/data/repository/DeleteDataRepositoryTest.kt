package org.thelazybattley.macrotrack.data.repository

import org.thelazybattley.macrotrack.data.local.dao.BackupDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.domain.model.DeleteDataStep
import org.thelazybattley.macrotrack.runSync
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteDataRepositoryTest {

    /** Records which tables were emptied, and keeps app flags so the kept ones can be checked. */
    private class FakeBackupDao : BackupDao() {
        val cleared = mutableSetOf<String>()
        var flags = listOf<AppFlagEntity>()

        override suspend fun getFoods() = emptyList<FoodEntity>()
        override suspend fun getRecipes() = emptyList<RecipeEntity>()
        override suspend fun getUserDetails() = emptyList<UserDetailsEntity>()
        override suspend fun getFoodLogs() = emptyList<FoodLogEntity>()
        override suspend fun getWeights() = emptyList<WeightEntity>()
        override suspend fun getMeasurements() = emptyList<MeasurementEntity>()
        override suspend fun getDailyGoals() = emptyList<DailyGoalEntity>()
        override suspend fun getFavorites() = emptyList<FavoriteEntity>()
        override suspend fun getAppFlags() = flags
        override suspend fun deleteFoods() { cleared += "foods" }
        override suspend fun deleteRecipes() { cleared += "recipes" }
        override suspend fun deleteUserDetails() { cleared += "userDetails" }
        override suspend fun deleteFoodLogs() { cleared += "foodLogs" }
        override suspend fun deleteWeights() { cleared += "weights" }
        override suspend fun deleteMeasurements() { cleared += "measurements" }
        override suspend fun deleteDailyGoals() { cleared += "dailyGoals" }
        override suspend fun deleteFavorites() { cleared += "favorites" }
        override suspend fun deleteAppFlagsExcept(keepKeys: List<String>) { flags = flags.filter { it.key in keepKeys } }
        override suspend fun insertFoods(foods: List<FoodEntity>) {}
        override suspend fun insertRecipes(recipes: List<RecipeEntity>) {}
        override suspend fun insertUserDetails(userDetails: List<UserDetailsEntity>) {}
        override suspend fun insertFoodLogs(foodLogs: List<FoodLogEntity>) {}
        override suspend fun insertWeights(weights: List<WeightEntity>) {}
        override suspend fun insertMeasurements(measurements: List<MeasurementEntity>) {}
        override suspend fun insertDailyGoals(dailyGoals: List<DailyGoalEntity>) {}
        override suspend fun insertFavorites(favorites: List<FavoriteEntity>) {}
        override suspend fun insertAppFlags(appFlags: List<AppFlagEntity>) {}
    }

    @Test
    fun everyStepTogetherEmptiesEveryTable() = runSync {
        val dao = FakeBackupDao()
        val repository = DeleteDataRepositoryImpl(backupDao = dao)
        DeleteDataStep.entries.forEach { repository.delete(step = it) }
        assertEquals(
            setOf("foods", "recipes", "userDetails", "foodLogs", "weights", "measurements", "dailyGoals", "favorites"),
            dao.cleared
        )
    }

    @Test
    fun proReviewAndUsageChoicesOutliveTheDelete() = runSync {
        val dao = FakeBackupDao()
        dao.flags = listOf(
            AppFlagEntity(key = ProRepositoryImpl.PRO_PURCHASED),
            AppFlagEntity(key = ProRepositoryImpl.PRO_TRIAL_START, value = "2026-10-01"),
            AppFlagEntity(key = AppFlagRepositoryImpl.REVIEW_TIMES_ASKED, value = "1"),
            AppFlagEntity(key = AppFlagRepositoryImpl.TELEMETRY_OPTED_OUT),
            AppFlagEntity(key = AppFlagRepositoryImpl.TOUR_COMPLETED),
            AppFlagEntity(key = AppFlagRepositoryImpl.UNIT_SYSTEM, value = "IMPERIAL"),
            AppFlagEntity(key = BackupRepositoryImpl.LAST_BACKUP_AT, value = "1")
        )
        DeleteDataRepositoryImpl(backupDao = dao).delete(step = DeleteDataStep.GOALS_AND_SETTINGS)
        assertEquals(
            setOf(
                ProRepositoryImpl.PRO_PURCHASED,
                ProRepositoryImpl.PRO_TRIAL_START,
                AppFlagRepositoryImpl.REVIEW_TIMES_ASKED,
                AppFlagRepositoryImpl.TELEMETRY_OPTED_OUT
            ),
            dao.flags.map { it.key }.toSet()
        )
    }
}
