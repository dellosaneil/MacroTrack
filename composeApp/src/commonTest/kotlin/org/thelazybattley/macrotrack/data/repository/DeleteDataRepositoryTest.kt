package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.thelazybattley.macrotrack.core.platform.PhotoReminderScheduler
import org.thelazybattley.macrotrack.data.local.dao.BackupDao
import org.thelazybattley.macrotrack.data.local.entity.ActivitySessionEntity
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.PlannedFoodEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.RepeatingMealEntity
import org.thelazybattley.macrotrack.data.local.entity.StepHourEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.domain.model.DeleteDataStep
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.repository.ProgressPhotoRepository
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
        override suspend fun getPlannedFoods() = emptyList<PlannedFoodEntity>()
        override suspend fun getRepeatingMeals() = emptyList<RepeatingMealEntity>()
        override suspend fun getStepHours() = emptyList<StepHourEntity>()
        override suspend fun getActivitySessions() = emptyList<ActivitySessionEntity>()
        override suspend fun deleteFoods() { cleared += "foods" }
        override suspend fun deleteRecipes() { cleared += "recipes" }
        override suspend fun deleteUserDetails() { cleared += "userDetails" }
        override suspend fun deleteFoodLogs() { cleared += "foodLogs" }
        override suspend fun deleteWeights() { cleared += "weights" }
        override suspend fun deleteMeasurements() { cleared += "measurements" }
        override suspend fun deleteDailyGoals() { cleared += "dailyGoals" }
        override suspend fun deleteFavorites() { cleared += "favorites" }
        override suspend fun deletePlannedFoods() { cleared += "plannedFoods" }
        override suspend fun deleteRepeatingMeals() { cleared += "repeatingMeals" }
        override suspend fun deleteStepHours() { cleared += "stepHours" }
        override suspend fun deleteActivitySessions() { cleared += "activitySessions" }
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
        override suspend fun insertPlannedFoods(plannedFoods: List<PlannedFoodEntity>) {}
        override suspend fun insertRepeatingMeals(repeatingMeals: List<RepeatingMealEntity>) {}
        override suspend fun insertStepHours(stepHours: List<StepHourEntity>) {}
        override suspend fun insertActivitySessions(activitySessions: List<ActivitySessionEntity>) {}
    }

    /** Records that the photos were deleted, into the same list as the tables. */
    private class FakePhotoRepository(private val cleared: MutableSet<String>) : ProgressPhotoRepository {
        override fun getAlbums(): Flow<List<PhotoAlbum>> = flowOf(value = emptyList())
        override fun getPhotos(): Flow<List<ProgressPhoto>> = flowOf(value = emptyList())
        override suspend fun saveAlbum(album: PhotoAlbum) = 0L
        override suspend fun deleteAlbum(id: Long) {}
        override suspend fun insertPhotos(photos: List<ProgressPhoto>) = emptyList<Long>()
        override suspend fun deletePhoto(photo: ProgressPhoto) {}
        override suspend fun deleteAll() { cleared += "photos" }
        override suspend fun deleteUnusedImages(alsoKeep: Set<String>) {}
    }

    private class FakeReminders : PhotoReminderScheduler {
        var albums: List<PhotoAlbum>? = null
        override suspend fun schedule(albums: List<PhotoAlbum>) { this.albums = albums }
    }

    private fun repository(dao: FakeBackupDao, reminders: FakeReminders = FakeReminders()) = DeleteDataRepositoryImpl(
        backupDao = dao,
        photoRepository = FakePhotoRepository(cleared = dao.cleared),
        photoReminders = reminders
    )

    @Test
    fun everyStepTogetherEmptiesEveryTable() = runSync {
        val dao = FakeBackupDao()
        val repository = repository(dao = dao)
        DeleteDataStep.entries.forEach { repository.delete(step = it) }
        assertEquals(
            setOf(
                "foods", "recipes", "userDetails", "foodLogs", "weights", "measurements", "dailyGoals", "favorites",
                "plannedFoods", "repeatingMeals", "stepHours", "activitySessions", "photos"
            ),
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
        repository(dao = dao).delete(step = DeleteDataStep.GOALS_AND_SETTINGS)
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

    @Test
    fun deletingBodyRecordsTurnsOffPhotoReminders() = runSync {
        val reminders = FakeReminders()
        repository(dao = FakeBackupDao(), reminders = reminders).delete(step = DeleteDataStep.BODY_RECORDS)
        assertEquals(emptyList(), reminders.albums)
    }
}
