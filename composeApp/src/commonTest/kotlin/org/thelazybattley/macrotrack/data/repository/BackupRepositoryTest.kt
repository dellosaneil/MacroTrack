package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.dao.BackupDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.PlannedFoodEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.RepeatingMealEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.domain.model.BackupException
import org.thelazybattley.macrotrack.domain.model.BackupProblem
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.runSync
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BackupRepositoryTest {

    private class FakeBackupDao : BackupDao() {
        var foods = listOf<FoodEntity>()
        var foodLogs = listOf<FoodLogEntity>()
        var weights = listOf<WeightEntity>()
        var flags = listOf<AppFlagEntity>()
        var repeatingMeals = listOf<RepeatingMealEntity>()

        override suspend fun getFoods() = foods
        override suspend fun getRecipes() = emptyList<RecipeEntity>()
        override suspend fun getUserDetails() = emptyList<UserDetailsEntity>()
        override suspend fun getFoodLogs() = foodLogs
        override suspend fun getWeights() = weights
        override suspend fun getMeasurements() = emptyList<MeasurementEntity>()
        override suspend fun getDailyGoals() = emptyList<DailyGoalEntity>()
        override suspend fun getFavorites() = emptyList<FavoriteEntity>()
        override suspend fun getAppFlags() = flags
        override suspend fun getPlannedFoods() = emptyList<PlannedFoodEntity>()
        override suspend fun getRepeatingMeals() = repeatingMeals
        override suspend fun deleteFoods() { foods = emptyList() }
        override suspend fun deleteRecipes() {}
        override suspend fun deleteUserDetails() {}
        override suspend fun deleteFoodLogs() { foodLogs = emptyList() }
        override suspend fun deleteWeights() { weights = emptyList() }
        override suspend fun deleteMeasurements() {}
        override suspend fun deleteDailyGoals() {}
        override suspend fun deleteFavorites() {}
        override suspend fun deletePlannedFoods() {}
        override suspend fun deleteRepeatingMeals() { repeatingMeals = emptyList() }
        override suspend fun deleteAppFlagsExcept(keepKeys: List<String>) { flags = flags.filter { it.key in keepKeys } }
        override suspend fun insertFoods(foods: List<FoodEntity>) { this.foods += foods }
        override suspend fun insertRecipes(recipes: List<RecipeEntity>) {}
        override suspend fun insertUserDetails(userDetails: List<UserDetailsEntity>) {}
        override suspend fun insertFoodLogs(foodLogs: List<FoodLogEntity>) { this.foodLogs += foodLogs }
        override suspend fun insertWeights(weights: List<WeightEntity>) { this.weights += weights }
        override suspend fun insertMeasurements(measurements: List<MeasurementEntity>) {}
        override suspend fun insertDailyGoals(dailyGoals: List<DailyGoalEntity>) {}
        override suspend fun insertFavorites(favorites: List<FavoriteEntity>) {}
        override suspend fun insertAppFlags(appFlags: List<AppFlagEntity>) { flags += appFlags }
        override suspend fun insertPlannedFoods(plannedFoods: List<PlannedFoodEntity>) {}
        override suspend fun insertRepeatingMeals(repeatingMeals: List<RepeatingMealEntity>) { this.repeatingMeals += repeatingMeals }
    }

    private class FakeAppFlagDao : AppFlagDao {
        val flags = MutableStateFlow(value = mapOf<String, String?>())
        override suspend fun insertFlag(flag: AppFlagEntity) { flags.value += flag.key to flag.value }
        override suspend fun insertFlagIfAbsent(flag: AppFlagEntity) { if (flag.key !in flags.value) insertFlag(flag = flag) }
        override suspend fun deleteFlag(key: String) { flags.value -= key }
        override fun isFlagSet(key: String): Flow<Boolean> = flags.map { key in it }
        override fun getFlagValue(key: String): Flow<String?> = flags.map { it[key] }
    }

    private val log = FoodLogEntity(
        id = 7, date = LocalDate(2026, 10, 15), foodName = "Restaurant lunch", mealType = MealType.LUNCH,
        calories = 600, protein = 30.0, carbs = 60.0, fat = 20.0, weight = 0.0, dominantMacro = "CARBS", isQuickAdd = true
    )

    private val breakfast = RepeatingMealEntity(
        id = 3, name = "Weekday breakfast", mealType = MealType.BREAKFAST, days = "1,2,3,4,5",
        foods = listOf(
            PlanFood(
                foodName = "Greek yogurt", calories = 190, protein = 20.0, carbs = 8.0, fat = 10.0, weight = 200.0,
                dominantMacro = MacroType.PROTEIN
            )
        ),
        startDate = LocalDate(2026, 10, 9), updatedOn = LocalDate(2026, 10, 8)
    )

    @Test
    fun restoringABackupBringsTheDataBackButKeepsThisPhonesFlags() = runSync {
        val dao = FakeBackupDao().apply {
            foodLogs = listOf(log)
            weights = listOf(WeightEntity(date = LocalDate(2026, 10, 15), weight = 74.2))
            repeatingMeals = listOf(breakfast)
            flags = listOf(
                AppFlagEntity(key = "unit_system", value = "IMPERIAL"),
                AppFlagEntity(key = ProRepositoryImpl.PRO_PURCHASED)
            )
        }
        val repository = BackupRepositoryImpl(backupDao = dao, appFlagDao = FakeAppFlagDao())
        val backup = repository.createBackup()
        // Pro is bought per store account, so it never travels in a backup.
        assertTrue(!backup.decodeToString().contains(ProRepositoryImpl.PRO_PURCHASED))
        assertEquals(1, repository.readBackup(bytes = backup).foodLogCount)

        dao.foodLogs = emptyList()
        dao.weights = emptyList()
        dao.repeatingMeals = emptyList()
        dao.flags = listOf(AppFlagEntity(key = ProRepositoryImpl.PRO_TRIAL_START, value = "2026-10-01"))
        repository.restore(bytes = backup)
        assertEquals(listOf(log), dao.foodLogs)
        assertEquals(1, dao.weights.size)
        assertEquals(listOf(breakfast), dao.repeatingMeals)
        assertEquals(setOf(ProRepositoryImpl.PRO_TRIAL_START, "unit_system"), dao.flags.map { it.key }.toSet())
    }

    @Test
    fun otherFilesAreRefused() = runSync {
        val repository = BackupRepositoryImpl(backupDao = FakeBackupDao(), appFlagDao = FakeAppFlagDao())
        assertEquals(
            BackupProblem.NOT_A_BACKUP,
            assertFailsWith<BackupException> { repository.readBackup(bytes = "not json".encodeToByteArray()) }.problem
        )
        assertEquals(
            BackupProblem.NOT_A_BACKUP,
            assertFailsWith<BackupException> { repository.readBackup(bytes = "{\"foodLog\": []}".encodeToByteArray()) }.problem
        )
        assertEquals(
            BackupProblem.TOO_NEW,
            assertFailsWith<BackupException> {
                repository.readBackup(bytes = "{\"format\": \"macrotrack-backup\", \"version\": 99}".encodeToByteArray())
            }.problem
        )
    }
}
