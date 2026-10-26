package org.thelazybattley.macrotrack.core.di

import io.ktor.client.HttpClient
import org.thelazybattley.macrotrack.domain.usecase.backup.RecordBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.GetLastBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.RestoreBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.ReadBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.backup.CreateBackupUseCase
import org.thelazybattley.macrotrack.domain.repository.BackupRepository
import org.thelazybattley.macrotrack.data.repository.BackupRepositoryImpl
import org.thelazybattley.macrotrack.features.backup.BackupViewModel
import org.thelazybattley.macrotrack.data.repository.DeleteDataRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.DeleteDataRepository
import org.thelazybattley.macrotrack.domain.usecase.deletedata.DeleteAllDataUseCase
import org.thelazybattley.macrotrack.features.deletedata.DeleteDataViewModel
import org.thelazybattley.macrotrack.features.quickadd.QuickAddViewModel
import org.thelazybattley.macrotrack.features.checkin.CheckInViewModel
import org.thelazybattley.macrotrack.domain.usecase.checkin.CompleteCheckInUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.GetWeekBalanceUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.GetWeekCardUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.GetWeeklyCheckInUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.MarkCheckInSeenUseCase
import io.ktor.client.plugins.HttpTimeout
import org.thelazybattley.macrotrack.data.repository.FoodLibraryRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.OpenFoodFactsRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.FoodLibraryRepository
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.features.foodlibrary.FoodLibraryViewModel
import org.thelazybattley.macrotrack.domain.repository.ProductRepository
import org.thelazybattley.macrotrack.domain.usecase.food.LookUpBarcodeUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetRemainingCaloriesUseCase
import org.thelazybattley.macrotrack.features.scanbarcode.ScanBarcodeViewModel
import org.thelazybattley.macrotrack.features.scanbarcode.ScannedFoodRequest
import org.thelazybattley.macrotrack.core.isPaywallEnabled
import org.thelazybattley.macrotrack.data.repository.AppFlagRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.ProRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.ProRepository
import org.thelazybattley.macrotrack.domain.usecase.pro.DismissTrialEndedNoticeUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.GetProStatusUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.IsTrialEndedNoticeDismissedUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.SetProPurchasedUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.StartProTrialUseCase
import org.thelazybattley.macrotrack.features.pro.ProViewModel
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.usecase.tour.IsTourCompletedUseCase
import org.thelazybattley.macrotrack.domain.usecase.tour.SetTourCompletedUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.SetUnitSystemUseCase
import org.thelazybattley.macrotrack.features.tour.TourViewModel
import org.thelazybattley.macrotrack.features.insights.InsightsViewModel
import org.thelazybattley.macrotrack.data.repository.DailyGoalRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.DailyGoalRepository
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.SaveDailyGoalUseCase
import org.thelazybattley.macrotrack.features.logweight.LogWeightViewModel
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnitPreference
import org.thelazybattley.macrotrack.features.measurements.detail.MeasurementDetailViewModel
import org.thelazybattley.macrotrack.features.measurements.log.MeasurementsLogViewModel
import org.thelazybattley.macrotrack.features.measurements.overview.MeasurementsViewModel
import org.thelazybattley.macrotrack.features.progress.ProgressViewModel
import org.thelazybattley.macrotrack.data.repository.MeasurementRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.MeasurementRepository
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.domain.usecase.measurement.InsertMeasurementsUseCase
import getRoomDatabase
import org.koin.core.context.startKoin
import org.thelazybattley.macrotrack.domain.usecase.telemetry.IsTelemetryEnabledUseCase
import org.thelazybattley.macrotrack.domain.usecase.telemetry.SetTelemetryEnabledUseCase
import org.thelazybattley.macrotrack.domain.usecase.review.GetReviewHistoryUseCase
import org.thelazybattley.macrotrack.domain.usecase.review.RecordReviewAskedUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.ApplyPastGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.UpdateFoodLogUseCase
import org.thelazybattley.macrotrack.features.calendar.CalendarViewModel
import org.thelazybattley.macrotrack.features.profile.pastgoal.PastGoalViewModel
import org.thelazybattley.macrotrack.features.calendar.LogDateRequest
import org.koin.dsl.module
import org.koin.core.qualifier.named
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
import org.thelazybattley.macrotrack.data.local.tour.TourActivityDao
import org.thelazybattley.macrotrack.data.local.tour.TourDailyGoalDao
import org.thelazybattley.macrotrack.data.local.tour.TourDatabaseSwitch
import org.thelazybattley.macrotrack.data.local.tour.TourFavoriteDao
import org.thelazybattley.macrotrack.data.local.tour.TourFoodDao
import org.thelazybattley.macrotrack.data.local.tour.TourFoodLogDao
import org.thelazybattley.macrotrack.data.local.tour.TourMealPlanDao
import org.thelazybattley.macrotrack.data.local.tour.TourMeasurementDao
import org.thelazybattley.macrotrack.data.local.tour.TourProgressPhotoDao
import org.thelazybattley.macrotrack.data.local.tour.TourRecipeDao
import org.thelazybattley.macrotrack.data.local.tour.TourSampleData
import org.thelazybattley.macrotrack.data.local.tour.TourUserDetailsDao
import org.thelazybattley.macrotrack.data.local.tour.TourWeightDao
import org.thelazybattley.macrotrack.data.local.AppDatabase
import org.thelazybattley.macrotrack.data.repository.FoodLogRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.FoodRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.RecipeRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.UserDetailsRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.WeightRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository
import org.thelazybattley.macrotrack.domain.repository.FoodRepository
import org.thelazybattley.macrotrack.domain.repository.RecipeRepository
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository
import org.thelazybattley.macrotrack.domain.repository.WeightRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateBMIUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateCaloriesFromMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacroPercentageUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.DeleteFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetFoodByNameUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.InsertFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.UpdateFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.DeleteFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetFoodLogByDateUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.CopyFoodLogsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.UndoFoodLogCopyUseCase
import org.thelazybattley.macrotrack.features.foodlog.FoodLogCopyEvents
import org.thelazybattley.macrotrack.features.copy.CopyToViewModel
import org.thelazybattley.macrotrack.features.copy.CopyFromViewModel
import org.thelazybattley.macrotrack.data.repository.MealPlanRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.MealPlanRepository
import org.thelazybattley.macrotrack.domain.usecase.mealplan.DeleteRepeatingMealUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetPlannedFoodsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetPlannedTodayUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.PlannedFoodActions
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.MarkPlannedEatenUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.PlanFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.RemovePlannedFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SaveRepeatingMealUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SetRepeatingMealActiveUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SkipPlannedFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SyncRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.UndoPlannedFoodUseCase
import org.thelazybattley.macrotrack.features.mealplan.MealPlanViewModel
import org.thelazybattley.macrotrack.features.mealplan.RepeatMealViewModel
import org.thelazybattley.macrotrack.features.mealplan.RepeatingMealsViewModel
import org.thelazybattley.macrotrack.data.repository.ActivityRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import org.thelazybattley.macrotrack.domain.usecase.activity.DeleteActivitySessionUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetActivitySessionUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetActivitySessionsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBudgetBurnUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetDayActivityUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepHoursUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.IsBurnAddedToBudgetUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.IsStepNudgeEnabledUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.RenameActivitySessionUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SaveActivitySessionUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.ScheduleStepNudgeUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepNotificationSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.IsStepCountingOnUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetStepCountingUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.UpdateStepNotificationSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetBurnAddedToBudgetUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetStepGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetStepNudgeEnabledUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.TrackStepsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetTodayActivityUseCase
import org.thelazybattley.macrotrack.features.activity.ActivityDetailViewModel
import org.thelazybattley.macrotrack.features.activity.ActivityHistoryViewModel
import org.thelazybattley.macrotrack.features.activity.ActivityRecorder
import org.thelazybattley.macrotrack.core.platform.AppLinks
import org.thelazybattley.macrotrack.features.activity.CaloriesBurnedViewModel
import org.thelazybattley.macrotrack.features.activity.RecordViewModel
import org.thelazybattley.macrotrack.features.activity.StepsViewModel
import org.thelazybattley.macrotrack.features.activity.StepsSettingsViewModel
import org.thelazybattley.macrotrack.domain.usecase.recipe.DeleteRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetRecipeByNameUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.InsertRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.UpdateRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.InsertWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.favorite.GetFavoriteKeysUseCase
import org.thelazybattley.macrotrack.domain.usecase.favorite.SetFavoriteUseCase
import org.thelazybattley.macrotrack.data.repository.FavoriteRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.FavoriteRepository
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.createfood.CreateFoodViewModel
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewModel
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewModel
import org.thelazybattley.macrotrack.features.profile.ProfileViewModel
import org.thelazybattley.macrotrack.features.profile.nutritiongoals.NutritionGoalsViewModel
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewModel
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewModel
import org.thelazybattley.macrotrack.features.splash.SplashViewModel
import org.thelazybattley.macrotrack.data.repository.ProgressPhotoRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.ProgressPhotoRepository
import org.thelazybattley.macrotrack.domain.usecase.photo.DeletePhotoAlbumUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.DeleteProgressPhotoUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetPhotoAlbumsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.SavePhotoAlbumUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.SaveProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.SchedulePhotoRemindersUseCase
import org.thelazybattley.macrotrack.features.photos.PhotoDrafts
import org.thelazybattley.macrotrack.features.photos.PhotoAlbumViewModel
import org.thelazybattley.macrotrack.features.photos.PhotoCameraViewModel
import org.thelazybattley.macrotrack.features.photos.PhotoCompareViewModel
import org.thelazybattley.macrotrack.features.photos.PhotoSaveViewModel
import org.thelazybattley.macrotrack.features.photos.ProgressPhotosViewModel

expect val platformModule: Module

/** Names the in-memory database builder for the tour's example log; each platform provides it as a factory. */
const val SAMPLE_DATABASE = "sample_database"

val sharedModule = module {
    single<AppDatabase> { getRoomDatabase(get()) }
    // The first-run tour's example log, in a fresh in-memory database each time the tour starts.
    single {
        val scope = this
        TourSampleData(
            newDatabase = { getRoomDatabase(scope.get(qualifier = named(name = SAMPLE_DATABASE))) },
            calculateMacrosGoalUseCase = get()
        )
    }
    single { TourDatabaseSwitch(real = get(), sample = get()) }
    // The DAOs behind the screens the tour shows follow the switch. Backup and app flags are always the user's own.
    single<FoodDao> { TourFoodDao(db = get()) }
    single<RecipeDao> { TourRecipeDao(db = get()) }
    single<UserDetailsDao> { TourUserDetailsDao(db = get()) }
    single<FoodLogDao> { TourFoodLogDao(db = get()) }
    single<WeightDao> { TourWeightDao(db = get()) }
    single<MeasurementDao> { TourMeasurementDao(db = get()) }
    single<DailyGoalDao> { TourDailyGoalDao(db = get()) }
    single<FavoriteDao> { TourFavoriteDao(db = get()) }
    single { get<AppDatabase>().appFlagDao() }
    single { get<AppDatabase>().backupDao() }
    single<MealPlanDao> { TourMealPlanDao(db = get()) }
    single<ActivityDao> { TourActivityDao(db = get()) }
    single<ProgressPhotoDao> { TourProgressPhotoDao(db = get()) }
    singleOf(::MeasurementUnitPreference)
    singleOf(::AppLinks)
    single {
        HttpClient {
            install(plugin = HttpTimeout) {
                requestTimeoutMillis = 10_000
            }
        }
    }
}

val repositoryModule = module {
    singleOf(::FoodRepositoryImpl) { bind<FoodRepository>() }
    singleOf(::RecipeRepositoryImpl) { bind<RecipeRepository>() }
    singleOf(::UserDetailsRepositoryImpl) { bind<UserDetailsRepository>() }
    singleOf(::FoodLogRepositoryImpl) { bind<FoodLogRepository>() }
    singleOf(::LogDateRequest)
    singleOf(::FoodLogCopyEvents)
    singleOf(::ScannedFoodRequest)
    singleOf(::WeightRepositoryImpl) { bind<WeightRepository>() }
    singleOf(::MeasurementRepositoryImpl) { bind<MeasurementRepository>() }
    singleOf(::DailyGoalRepositoryImpl) { bind<DailyGoalRepository>() }
    singleOf(::FavoriteRepositoryImpl) { bind<FavoriteRepository>() }
    singleOf(::AppFlagRepositoryImpl) { bind<AppFlagRepository>() }
    singleOf(::ProRepositoryImpl) { bind<ProRepository>() }
    singleOf(::OpenFoodFactsRepositoryImpl) { bind<ProductRepository>() }
    singleOf(::FoodLibraryRepositoryImpl) { bind<FoodLibraryRepository>() }
    singleOf(::BackupRepositoryImpl) { bind<BackupRepository>() }
    singleOf(::DeleteDataRepositoryImpl) { bind<DeleteDataRepository>() }
    singleOf(::MealPlanRepositoryImpl) { bind<MealPlanRepository>() }
    singleOf(::ActivityRepositoryImpl) { bind<ActivityRepository>() }
    singleOf(::ActivityRecorder)
    singleOf(::ProgressPhotoRepositoryImpl) { bind<ProgressPhotoRepository>() }
    singleOf(::PhotoDrafts)
}

val useCaseModule = module {
    factoryOf(::CalculateTDEEUseCase)
    factoryOf(::InsertFoodUseCase)
    factoryOf(::GetAllFoodUseCase)
    factoryOf(::GetFoodByNameUseCase)
    factoryOf(::GetAllRecipeUseCase)
    factoryOf(::GetRecipeByNameUseCase)
    factoryOf(::InsertRecipeUseCase)
    factoryOf(::CalculateMacrosGoalUseCase)
    factoryOf(::InsertUserDetailsUseCase)
    factoryOf(::GetUserDetailsUseCase)
    factoryOf(::DeleteFoodLogUseCase)
    factoryOf(::GetAllFoodLogUseCase)
    factoryOf(::UpdateFoodLogUseCase)
    factoryOf(::InsertFoodLogUseCase)
    factoryOf(::CopyFoodLogsUseCase)
    factoryOf(::UndoFoodLogCopyUseCase)
    factoryOf(::CalculateCaloriesFromMacrosUseCase)
    factoryOf(::CalculateAdjustMacrosUseCase)
    factoryOf(::CalculateMacroPercentageUseCase)
    factoryOf(::GetFoodLogByDateUseCase)
    factoryOf(::UpdateRecipeUseCase)
    factoryOf(::DeleteRecipeUseCase)
    factoryOf(::CalculateBMIUseCase)
    factoryOf(::InsertWeightUseCase)
    factoryOf(::GetAllWeightUseCase)
    factoryOf(::UpdateFoodUseCase)
    factoryOf(::DeleteFoodUseCase)
    factoryOf(::GetAllMeasurementsUseCase)
    factoryOf(::InsertMeasurementsUseCase)
    factoryOf(::GetAllDailyGoalsUseCase)
    factoryOf(::ApplyPastGoalUseCase)
    factoryOf(::SaveDailyGoalUseCase)
    factoryOf(::GetFavoriteKeysUseCase)
    factoryOf(::SetFavoriteUseCase)
    factoryOf(::IsTourCompletedUseCase)
    factoryOf(::SetTourCompletedUseCase)
    factoryOf(::GetUnitSystemUseCase)
    factoryOf(::CreateBackupUseCase)
    factoryOf(::ReadBackupUseCase)
    factoryOf(::RestoreBackupUseCase)
    factoryOf(::GetLastBackupUseCase)
    factoryOf(::RecordBackupUseCase)
    factoryOf(::SetUnitSystemUseCase)
    factory { GetProStatusUseCase(repository = get(), isPaywallEnabled = isPaywallEnabled) }
    factoryOf(::StartProTrialUseCase)
    factoryOf(::SetProPurchasedUseCase)
    factoryOf(::IsTrialEndedNoticeDismissedUseCase)
    factoryOf(::DismissTrialEndedNoticeUseCase)
    factoryOf(::LookUpBarcodeUseCase)
    factoryOf(::GetRemainingCaloriesUseCase)
    factoryOf(::GetWeeklyCheckInUseCase)
    factoryOf(::CompleteCheckInUseCase)
    factoryOf(::GetWeekBalanceUseCase)
    factoryOf(::GetWeekCardUseCase)
    factoryOf(::MarkCheckInSeenUseCase)
    factoryOf(::GetFoodLibraryUseCase)
    factoryOf(::IsTelemetryEnabledUseCase)
    factoryOf(::SetTelemetryEnabledUseCase)
    factoryOf(::GetReviewHistoryUseCase)
    factoryOf(::RecordReviewAskedUseCase)
    factoryOf(::DeleteAllDataUseCase)
    factoryOf(::GetPlannedFoodsUseCase)
    factoryOf(::GetPlannedTodayUseCase)
    factoryOf(::PlannedFoodActions)
    factoryOf(::GetRepeatingMealsUseCase)
    factoryOf(::PlanFoodUseCase)
    factoryOf(::MarkPlannedEatenUseCase)
    factoryOf(::SkipPlannedFoodUseCase)
    factoryOf(::UndoPlannedFoodUseCase)
    factoryOf(::RemovePlannedFoodUseCase)
    factoryOf(::SyncRepeatingMealsUseCase)
    factoryOf(::SaveRepeatingMealUseCase)
    factoryOf(::SetRepeatingMealActiveUseCase)
    factoryOf(::DeleteRepeatingMealUseCase)
    factoryOf(::TrackStepsUseCase)
    factoryOf(::GetStepHoursUseCase)
    factoryOf(::GetActivitySessionsUseCase)
    factoryOf(::GetActivitySessionUseCase)
    factoryOf(::SaveActivitySessionUseCase)
    factoryOf(::RenameActivitySessionUseCase)
    factoryOf(::DeleteActivitySessionUseCase)
    factoryOf(::GetStepGoalUseCase)
    factoryOf(::SetStepGoalUseCase)
    factoryOf(::IsStepNudgeEnabledUseCase)
    factoryOf(::SetStepNudgeEnabledUseCase)
    factoryOf(::ScheduleStepNudgeUseCase)
    factoryOf(::GetStepNotificationSettingsUseCase)
    factoryOf(::IsStepCountingOnUseCase)
    factoryOf(::SetStepCountingUseCase)
    factoryOf(::UpdateStepNotificationSettingsUseCase)
    factoryOf(::IsBurnAddedToBudgetUseCase)
    factoryOf(::SetBurnAddedToBudgetUseCase)
    factoryOf(::GetBurnProfileUseCase)
    factoryOf(::GetDayActivityUseCase)
    factoryOf(::GetBudgetBurnUseCase)
    factoryOf(::GetTodayActivityUseCase)
    factoryOf(::GetPhotoAlbumsUseCase)
    factoryOf(::GetProgressPhotosUseCase)
    factoryOf(::SavePhotoAlbumUseCase)
    factoryOf(::DeletePhotoAlbumUseCase)
    factoryOf(::SaveProgressPhotosUseCase)
    factoryOf(::DeleteProgressPhotoUseCase)
    factoryOf(::SchedulePhotoRemindersUseCase)
}

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::AddMealViewModel)
    viewModelOf(::FoodLogViewModel)
    viewModelOf(::CreateFoodViewModel)
    viewModelOf(::CreateRecipeViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::PersonalInformationViewModel)
    viewModelOf(::NutritionGoalsViewModel)
    viewModelOf(::WeightHistoryViewModel)
    viewModelOf(::MeasurementsViewModel)
    viewModelOf(::MeasurementsLogViewModel)
    viewModelOf(::MeasurementDetailViewModel)
    viewModelOf(::ProgressViewModel)
    viewModelOf(::LogWeightViewModel)
    viewModelOf(::InsightsViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::PastGoalViewModel)
    viewModelOf(::TourViewModel)
    viewModelOf(::ProViewModel)
    viewModelOf(::ScanBarcodeViewModel)
    viewModelOf(::FoodLibraryViewModel)
    viewModelOf(::QuickAddViewModel)
    viewModelOf(::CopyToViewModel)
    viewModelOf(::CopyFromViewModel)
    viewModelOf(::CheckInViewModel)
    viewModelOf(::BackupViewModel)
    viewModelOf(::DeleteDataViewModel)
    viewModelOf(::MealPlanViewModel)
    viewModelOf(::RepeatingMealsViewModel)
    viewModelOf(::RepeatMealViewModel)
    viewModelOf(::StepsSettingsViewModel)
    viewModelOf(::StepsViewModel)
    viewModelOf(::RecordViewModel)
    viewModelOf(::ActivityDetailViewModel)
    viewModelOf(::ActivityHistoryViewModel)
    viewModelOf(::CaloriesBurnedViewModel)
    viewModelOf(::ProgressPhotosViewModel)
    viewModelOf(::PhotoAlbumViewModel)
    viewModelOf(::PhotoCameraViewModel)
    viewModelOf(::PhotoSaveViewModel)
    viewModelOf(::PhotoCompareViewModel)
}

fun initKoin(extraModules: List<Module> = emptyList()) {
    startKoin {
        printLogger()
        modules(listOf(platformModule, sharedModule, viewModelModule, useCaseModule, repositoryModule) + extraModules)
    }
}
