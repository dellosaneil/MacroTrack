package org.thelazybattley.macrotrack.data.local.tour

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.data.local.AppDatabase
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.PhotoAlbumEntity
import org.thelazybattley.macrotrack.data.local.entity.ProgressPhotoEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.StepHourEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.Ingredient
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import kotlin.math.PI
import kotlin.math.round
import kotlin.math.sin

/** Sample photos have no file; the photo views draw a stand-in for any name with this prefix. */
const val TOUR_SAMPLE_PHOTO_PREFIX = "tour-sample-"

/**
 * The example log the first-run tour shows: a sample user a couple of months in, so every screen it visits has
 * something to point at. It lives in its own in-memory database, made fresh each time the tour starts, and the
 * user's own data is never read or changed while it's on.
 */
class TourSampleData(
    private val newDatabase: () -> AppDatabase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) {
    private val _database = MutableStateFlow<AppDatabase?>(value = null)

    /** The sample database while the tour runs; null the rest of the time. */
    val database: StateFlow<AppDatabase?> = _database.asStateFlow()

    /** Kept until the next tour, so queries still running when it ended don't hit a closed database. */
    private var previous: AppDatabase? = null

    suspend fun start() {
        if (_database.value != null) return
        previous?.close()
        previous = null
        val database = newDatabase()
        seed(database = database, today = getCurrentDate())
        _database.value = database
    }

    fun stop() {
        previous = _database.value
        _database.value = null
    }

    private suspend fun seed(database: AppDatabase, today: LocalDate) {
        fun daysAgo(days: Int) = today.minus(value = days, unit = DateTimeUnit.DAY)

        val foods = SampleFood.entries.map { it.entity }
        foods.forEach { database.foodDao().insertFood(food = it) }
        database.recipeDao().insertRecipe(
            recipe = RecipeEntity(
                name = "Chicken rice bowl",
                ingredients = listOf(
                    Ingredient(name = SampleFood.CHICKEN.entity.name, weight = 150.0),
                    Ingredient(name = SampleFood.RICE.entity.name, weight = 180.0),
                    Ingredient(name = SampleFood.BROCCOLI.entity.name, weight = 80.0)
                ),
                dominantMacro = "CARBS"
            )
        )
        database.favoriteDao().insertFavorite(favorite = FavoriteEntity(key = "food:" + SampleFood.YOGURT.entity.name))

        // Steady progress from 77 kg two months ago, with the usual day-to-day swings.
        fun weightOn(days: Int) = round((74.2 + 2.8 * days / 60.0 + 0.35 * sin(x = days * PI / 3.5)) * 10) / 10
        val weighIns = (0..60).filter { days -> days <= 28 || days % 3 == 0 } + PHOTO_DAYS
        weighIns.toSet().forEach { days -> database.weightDao().insertWeight(weight = WeightEntity(date = daysAgo(days), weight = weightOn(days))) }

        val user = UserDetailsEntity(
            weight = 74.2,
            age = 29,
            height = 175.0,
            gender = UserSex.MALE,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
            goal = Goal.LOSE_WEIGHT,
            goalPace = 1.2,
            targetWeight = 69.6,
            targetStartWeight = 77.0,
            targetSetDate = daysAgo(days = 60)
        )
        database.userDetailsDao().insertUserDetails(userDetails = user)

        // Maintained for the first few weeks, then a deficit: the weight charts shade both phases.
        listOf(60 to Goal.MAINTAIN_WEIGHT, 42 to Goal.LOSE_WEIGHT, 0 to Goal.LOSE_WEIGHT).forEach { (days, goal) ->
            val macros = calculateMacrosGoalUseCase(
                height = user.height,
                weight = weightOn(days),
                age = user.age,
                gender = user.gender,
                activityLevel = user.activityLevel,
                goal = goal,
                goalPace = user.goalPace,
                targetWeight = user.targetWeight
            )
            database.dailyGoalDao().upsertDailyGoal(
                dailyGoal = DailyGoalEntity(
                    date = daysAgo(days),
                    goal = goal,
                    calories = macros.calories,
                    protein = macros.protein,
                    carbs = macros.carbs,
                    fat = macros.fat
                )
            )
        }

        // A 4-day streak (a missed day before it), so Today shows a streak without the store rating prompt.
        val foodLog = database.foodLogDao()
        suspend fun log(days: Int, mealType: MealType, food: SampleFood, amount: Double) =
            foodLog.insertFoodLog(foodLogEntity = food.logged(date = daysAgo(days), mealType = mealType, amount = amount))
        (1..27).filter { it != 4 }.forEach { days ->
            val swing = (days % 5 - 2) * 40.0
            log(days, MealType.BREAKFAST, SampleFood.OATS, amount = 80.0)
            log(days, MealType.BREAKFAST, SampleFood.YOGURT, amount = 170.0)
            log(days, MealType.BREAKFAST, SampleFood.BANANA, amount = 118.0)
            log(days, MealType.LUNCH, SampleFood.CHICKEN, amount = 200.0)
            log(days, MealType.LUNCH, SampleFood.RICE, amount = 250.0 + swing)
            log(days, MealType.LUNCH, SampleFood.BROCCOLI, amount = 100.0)
            log(days, MealType.DINNER, SampleFood.SALMON, amount = 180.0)
            log(days, MealType.DINNER, SampleFood.RICE, amount = 200.0 + swing)
            log(days, MealType.DINNER, SampleFood.AVOCADO, amount = 50.0)
            log(days, MealType.SNACK, SampleFood.WHEY, amount = 30.0)
            if (days % 3 == 0) log(days, MealType.SNACK, SampleFood.EGG, amount = 100.0)
        }
        // Today so far: dinner is still to log.
        log(0, MealType.BREAKFAST, SampleFood.YOGURT, amount = 170.0)
        log(0, MealType.BREAKFAST, SampleFood.BANANA, amount = 118.0)
        log(0, MealType.LUNCH, SampleFood.CHICKEN, amount = 150.0)
        log(0, MealType.LUNCH, SampleFood.RICE, amount = 180.0)
        log(0, MealType.SNACK, SampleFood.WHEY, amount = 30.0)

        val measured = mapOf(
            MeasurementType.NECK to (38.5 to 38.2),
            MeasurementType.SHOULDERS to (118.0 to 118.5),
            MeasurementType.CHEST to (103.0 to 102.0),
            MeasurementType.LEFT_BICEP to (35.25 to 35.75),
            MeasurementType.RIGHT_BICEP to (35.5 to 35.75),
            MeasurementType.WAIST to (86.5 to 84.5),
            MeasurementType.HIPS to (98.0 to 97.0),
            MeasurementType.LEFT_THIGH to (57.0 to 56.25),
            MeasurementType.RIGHT_THIGH to (57.25 to 56.5),
            MeasurementType.CALVES to (38.0 to 38.0)
        )
        database.measurementDao().insertMeasurements(
            measurements = measured.flatMap { (type, values) ->
                listOf(
                    MeasurementEntity(date = daysAgo(days = 32), type = type, valueCm = values.first),
                    MeasurementEntity(date = daysAgo(days = 4), type = type, valueCm = values.second)
                )
            }
        )

        val photos = database.progressPhotoDao()
        fun millisOf(days: Int) = daysAgo(days).atStartOfDayIn(timeZone = TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val pumpCheck = photos.upsertAlbum(
            album = PhotoAlbumEntity(
                name = "Push Day Pump Check",
                reminderDays = "MONDAY,THURSDAY",
                reminderMinutes = 18 * 60,
                isDateAndWeightShown = true,
                createdAtMillis = millisOf(days = 50)
            )
        )
        val monthly = photos.upsertAlbum(
            album = PhotoAlbumEntity(
                name = "Monthly check",
                reminderDays = "",
                reminderMinutes = 18 * 60,
                isDateAndWeightShown = true,
                createdAtMillis = millisOf(days = 40)
            )
        )
        var index = 0
        fun photo(albumId: Long, days: Int, pose: PhotoPose) = ProgressPhotoEntity(
            albumId = albumId,
            date = daysAgo(days),
            takenOn = daysAgo(days),
            pose = pose,
            note = null,
            weightDate = daysAgo(days),
            fileName = TOUR_SAMPLE_PHOTO_PREFIX + index++,
            // The album added to most recently leads the photos screen.
            createdAtMillis = millisOf(days) + if (albumId == pumpCheck) 1 else 0
        )
        photos.insertPhotos(photos = PUMP_CHECK_DAYS.map { photo(albumId = pumpCheck, days = it, pose = PhotoPose.FLEX) })
        photos.insertPhotos(
            photos = MONTHLY_DAYS.flatMap { days ->
                listOf(photo(albumId = monthly, days = days, pose = PhotoPose.FRONT), photo(albumId = monthly, days = days, pose = PhotoPose.SIDE))
            }
        )

        // Steps for the last two weeks, with today partway there.
        val activity = database.activityDao()
        (0..13).forEach { days ->
            val hours = if (days == 0) 7..14 else 7..21
            val perHour = 520 + (days % 4) * 90
            hours.forEach { hour -> activity.setSteps(stepHour = StepHourEntity(date = daysAgo(days), hour = hour, steps = perHour + (hour % 3) * 60)) }
        }
    }

    private companion object {
        val PUMP_CHECK_DAYS = listOf(49, 35, 21, 7, 1)
        val MONTHLY_DAYS = listOf(38, 8)
        val PHOTO_DAYS = PUMP_CHECK_DAYS + MONTHLY_DAYS
    }
}

/** The sample user's foods, with macros for [FoodEntity.weight] of each. */
private enum class SampleFood(val entity: FoodEntity) {
    OATS(entity = food(name = "Rolled oats", weight = 40.0, protein = 5.0, carbs = 27.0, fat = 3.0, calories = 150, macro = "CARBS")),
    YOGURT(entity = food(name = "Greek yogurt", weight = 170.0, protein = 17.0, carbs = 6.0, fat = 0.0, calories = 100, macro = "PROTEIN")),
    BANANA(entity = food(name = "Banana", weight = 118.0, protein = 1.0, carbs = 27.0, fat = 0.0, calories = 105, macro = "CARBS")),
    CHICKEN(entity = food(name = "Chicken breast", weight = 100.0, protein = 31.0, carbs = 0.0, fat = 4.0, calories = 165, macro = "PROTEIN")),
    RICE(entity = food(name = "Jasmine rice", weight = 100.0, protein = 3.0, carbs = 28.0, fat = 0.0, calories = 130, macro = "CARBS")),
    BROCCOLI(entity = food(name = "Broccoli", weight = 100.0, protein = 3.0, carbs = 7.0, fat = 0.0, calories = 34, macro = "CARBS")),
    SALMON(entity = food(name = "Salmon fillet", weight = 100.0, protein = 20.0, carbs = 0.0, fat = 13.0, calories = 207, macro = "FAT")),
    AVOCADO(entity = food(name = "Avocado", weight = 50.0, protein = 1.0, carbs = 4.0, fat = 7.0, calories = 80, macro = "FAT")),
    WHEY(entity = food(name = "Whey protein", weight = 30.0, protein = 24.0, carbs = 3.0, fat = 1.0, calories = 120, macro = "PROTEIN")),
    EGG(entity = food(name = "Egg, large", weight = 50.0, protein = 6.0, carbs = 0.0, fat = 5.0, calories = 72, macro = "PROTEIN"));

    fun logged(date: LocalDate, mealType: MealType, amount: Double): FoodLogEntity {
        val ratio = amount / entity.weight
        fun grams(value: Double) = round(value * ratio * 10) / 10
        return FoodLogEntity(
            date = date,
            foodName = entity.name,
            mealType = mealType,
            calories = round(entity.calories * ratio).toInt(),
            protein = grams(value = entity.protein),
            carbs = grams(value = entity.carbs),
            fat = grams(value = entity.fat),
            weight = amount,
            dominantMacro = entity.dominantMacro
        )
    }
}

private fun food(name: String, weight: Double, protein: Double, carbs: Double, fat: Double, calories: Int, macro: String) =
    FoodEntity(name = name, protein = protein, carbs = carbs, fat = fat, calories = calories, weight = weight, dominantMacro = macro, unit = "g")

/** Picks the sample database while the tour runs and the user's own otherwise, for each call. */
class TourDatabaseSwitch(private val real: AppDatabase, private val sample: TourSampleData) {

    /** The real database, for calls that must always see the user's own data. */
    val realDatabase: AppDatabase get() = real

    /** Follows the switch: re-queries when the tour starts or ends. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> flow(query: (AppDatabase) -> Flow<T>): Flow<T> = sample.database.flatMapLatest { database -> query(database ?: real) }

    suspend fun <T> run(call: suspend (AppDatabase) -> T): T = call(sample.database.value ?: real)
}
