package org.thelazybattley.macrotrack.data.local.tour

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.dao.ActivityDao
import org.thelazybattley.macrotrack.data.local.dao.DailyGoalDao
import org.thelazybattley.macrotrack.data.local.dao.FavoriteDao
import org.thelazybattley.macrotrack.data.local.dao.FoodDao
import org.thelazybattley.macrotrack.data.local.dao.FoodLogDao
import org.thelazybattley.macrotrack.data.local.dao.MealPlanDao
import org.thelazybattley.macrotrack.data.local.dao.MeasurementDao
import org.thelazybattley.macrotrack.data.local.dao.ProgressPhotoDao
import org.thelazybattley.macrotrack.data.local.dao.RecipeDao
import org.thelazybattley.macrotrack.data.local.dao.UserDetailsDao
import org.thelazybattley.macrotrack.data.local.dao.WeightDao
import org.thelazybattley.macrotrack.data.local.entity.ActivitySessionEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.PhotoAlbumEntity
import org.thelazybattley.macrotrack.data.local.entity.PlannedFoodEntity
import org.thelazybattley.macrotrack.data.local.entity.ProgressPhotoEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.RepeatingMealEntity
import org.thelazybattley.macrotrack.data.local.entity.StepHourEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanStatus

/*
 * The DAOs the app uses, each following [TourDatabaseSwitch]: reads and writes go to the tour's sample database
 * while it runs, so nothing done on its screens touches the user's own log.
 */

class TourFoodDao(private val db: TourDatabaseSwitch) : FoodDao {
    override suspend fun insertFood(food: FoodEntity) = db.run { it.foodDao().insertFood(food = food) }
    override fun getAllFoods(): Flow<List<FoodEntity>> = db.flow { it.foodDao().getAllFoods() }
    override fun getFoodByName(name: String): Flow<List<FoodEntity>> = db.flow { it.foodDao().getFoodByName(name = name) }
    override suspend fun getFoodByBarcode(barcode: String): FoodEntity? = db.run { it.foodDao().getFoodByBarcode(barcode = barcode) }
    override suspend fun updateFood(food: FoodEntity) = db.run { it.foodDao().updateFood(food = food) }
    override suspend fun deleteFood(name: String) = db.run { it.foodDao().deleteFood(name = name) }
}

class TourRecipeDao(private val db: TourDatabaseSwitch) : RecipeDao {
    override suspend fun insertRecipe(recipe: RecipeEntity) = db.run { it.recipeDao().insertRecipe(recipe = recipe) }
    override fun getAllRecipes(): Flow<List<RecipeEntity>> = db.flow { it.recipeDao().getAllRecipes() }
    override fun getRecipeByName(name: String): Flow<List<RecipeEntity>> = db.flow { it.recipeDao().getRecipeByName(name = name) }
    override suspend fun updateRecipe(recipe: RecipeEntity) = db.run { it.recipeDao().updateRecipe(recipe = recipe) }
    override suspend fun deleteRecipe(name: String) = db.run { it.recipeDao().deleteRecipe(name = name) }
}

class TourUserDetailsDao(private val db: TourDatabaseSwitch) : UserDetailsDao {
    override suspend fun insertUserDetails(userDetails: UserDetailsEntity) =
        db.run { it.userDetailsDao().insertUserDetails(userDetails = userDetails) }
    override fun getUserDetails(): Flow<UserDetailsEntity?> = db.flow { it.userDetailsDao().getUserDetails() }
}

class TourFoodLogDao(private val db: TourDatabaseSwitch) : FoodLogDao {
    override suspend fun insertFoodLog(foodLogEntity: FoodLogEntity): Long =
        db.run { it.foodLogDao().insertFoodLog(foodLogEntity = foodLogEntity) }
    override suspend fun updateFoodLog(id: Long, mealType: MealType, calories: Int, protein: Double, carbs: Double, fat: Double, weight: Double) =
        db.run { it.foodLogDao().updateFoodLog(id = id, mealType = mealType, calories = calories, protein = protein, carbs = carbs, fat = fat, weight = weight) }
    override suspend fun deleteFoodLog(id: Long) = db.run { it.foodLogDao().deleteFoodLog(id = id) }
    override fun getAllFoodLogs(): Flow<List<FoodLogEntity>> = db.flow { it.foodLogDao().getAllFoodLogs() }
    override fun getFoodLogByDate(date: LocalDate): Flow<List<FoodLogEntity>> = db.flow { it.foodLogDao().getFoodLogByDate(date = date) }
}

class TourWeightDao(private val db: TourDatabaseSwitch) : WeightDao {
    override suspend fun insertWeight(weight: WeightEntity) = db.run { it.weightDao().insertWeight(weight = weight) }
    override fun getAllWeight(): Flow<List<WeightEntity>> = db.flow { it.weightDao().getAllWeight() }
}

class TourMeasurementDao(private val db: TourDatabaseSwitch) : MeasurementDao {
    override suspend fun insertMeasurements(measurements: List<MeasurementEntity>) =
        db.run { it.measurementDao().insertMeasurements(measurements = measurements) }
    override fun getAllMeasurements(): Flow<List<MeasurementEntity>> = db.flow { it.measurementDao().getAllMeasurements() }
}

class TourDailyGoalDao(private val db: TourDatabaseSwitch) : DailyGoalDao {
    override suspend fun upsertDailyGoal(dailyGoal: DailyGoalEntity) = db.run { it.dailyGoalDao().upsertDailyGoal(dailyGoal = dailyGoal) }
    override suspend fun deleteDailyGoals(start: LocalDate, end: LocalDate) = db.run { it.dailyGoalDao().deleteDailyGoals(start = start, end = end) }
    override fun getAllDailyGoals(): Flow<List<DailyGoalEntity>> = db.flow { it.dailyGoalDao().getAllDailyGoals() }
}

class TourFavoriteDao(private val db: TourDatabaseSwitch) : FavoriteDao {
    override suspend fun insertFavorite(favorite: FavoriteEntity) = db.run { it.favoriteDao().insertFavorite(favorite = favorite) }
    override suspend fun deleteFavorite(key: String) = db.run { it.favoriteDao().deleteFavorite(key = key) }
    override fun getAllFavorites(): Flow<List<FavoriteEntity>> = db.flow { it.favoriteDao().getAllFavorites() }
}

class TourMealPlanDao(private val db: TourDatabaseSwitch) : MealPlanDao {
    override suspend fun insertPlannedFood(plannedFood: PlannedFoodEntity): Long = db.run { it.mealPlanDao().insertPlannedFood(plannedFood = plannedFood) }
    override suspend fun updatePlannedStatus(id: Long, status: PlanStatus, logId: Long?) =
        db.run { it.mealPlanDao().updatePlannedStatus(id = id, status = status, logId = logId) }
    override suspend fun deletePlannedFood(id: Long) = db.run { it.mealPlanDao().deletePlannedFood(id = id) }
    override suspend fun deletePendingFrom(repeatingMealId: Long, fromDate: LocalDate) =
        db.run { it.mealPlanDao().deletePendingFrom(repeatingMealId = repeatingMealId, fromDate = fromDate) }
    override fun getPlannedFoods(): Flow<List<PlannedFoodEntity>> = db.flow { it.mealPlanDao().getPlannedFoods() }
    override suspend fun upsertRepeatingMeal(repeatingMeal: RepeatingMealEntity): Long =
        db.run { it.mealPlanDao().upsertRepeatingMeal(repeatingMeal = repeatingMeal) }
    override suspend fun setRepeatingMealActive(id: Long, isActive: Boolean, updatedOn: LocalDate) =
        db.run { it.mealPlanDao().setRepeatingMealActive(id = id, isActive = isActive, updatedOn = updatedOn) }
    override suspend fun deleteRepeatingMeal(id: Long) = db.run { it.mealPlanDao().deleteRepeatingMeal(id = id) }
    override fun getRepeatingMeals(): Flow<List<RepeatingMealEntity>> = db.flow { it.mealPlanDao().getRepeatingMeals() }
}

/** Reads follow the tour; steps and recorded sessions always save to the user's own log, since they keep coming. */
class TourActivityDao(private val db: TourDatabaseSwitch) : ActivityDao {
    private val real get() = db.realDatabase.activityDao()
    override fun getStepHours(fromDate: LocalDate): Flow<List<StepHourEntity>> = db.flow { it.activityDao().getStepHours(fromDate = fromDate) }
    override suspend fun addSteps(date: LocalDate, hour: Int, steps: Int) = real.addSteps(date = date, hour = hour, steps = steps)
    override suspend fun setSteps(stepHour: StepHourEntity) = real.setSteps(stepHour = stepHour)
    override suspend fun insertSession(session: ActivitySessionEntity): Long = real.insertSession(session = session)
    override suspend fun renameSession(id: Long, name: String?, type: ActivityType) = real.renameSession(id = id, name = name, type = type)
    override suspend fun deleteSession(id: Long) = real.deleteSession(id = id)
    override fun getSessions(): Flow<List<ActivitySessionEntity>> = db.flow { it.activityDao().getSessions() }
    override fun getSession(id: Long): Flow<ActivitySessionEntity?> = db.flow { it.activityDao().getSession(id = id) }
}

class TourProgressPhotoDao(private val db: TourDatabaseSwitch) : ProgressPhotoDao() {
    override fun getAlbums(): Flow<List<PhotoAlbumEntity>> = db.flow { it.progressPhotoDao().getAlbums() }
    override fun getPhotos(): Flow<List<ProgressPhotoEntity>> = db.flow { it.progressPhotoDao().getPhotos() }

    // Always the user's own: these say which image files are in use, and the rest get deleted.
    override suspend fun getFileNames(): List<String> = db.realDatabase.progressPhotoDao().getFileNames()
    override suspend fun getFileNames(albumId: Long): List<String> = db.realDatabase.progressPhotoDao().getFileNames(albumId = albumId)

    override suspend fun upsertAlbum(album: PhotoAlbumEntity): Long = db.run { it.progressPhotoDao().upsertAlbum(album = album) }
    override suspend fun insertPhotos(photos: List<ProgressPhotoEntity>): List<Long> = db.run { it.progressPhotoDao().insertPhotos(photos = photos) }
    override suspend fun deletePhoto(id: Long) = db.run { it.progressPhotoDao().deletePhoto(id = id) }
    override suspend fun deleteAlbum(id: Long) = db.run { it.progressPhotoDao().deleteAlbum(id = id) }
    override suspend fun deleteAll() = db.run { it.progressPhotoDao().deleteAll() }

    // Only used by the deletes above, which go to the database itself.
    override suspend fun deletePhotosOf(albumId: Long) = error("Not used")
    override suspend fun deleteAlbumRow(id: Long) = error("Not used")
    override suspend fun deletePhotos() = error("Not used")
    override suspend fun deleteAlbums() = error("Not used")
}
