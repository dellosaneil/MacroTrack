package org.thelazybattley.macrotrack.core.di

import org.thelazybattley.macrotrack.data.local.dao.WaterDao
import org.thelazybattley.macrotrack.data.local.dao.ChecklistDao
import org.thelazybattley.macrotrack.data.local.tour.TourChecklistDao
import org.thelazybattley.macrotrack.data.repository.ChecklistRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.ChecklistRepository
import org.thelazybattley.macrotrack.domain.usecase.checklist.DeleteChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetChecklistUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.GetDueChecklistRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.HandleChecklistReminderActionUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.RefillChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.ReorderChecklistUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SaveChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SaveChecklistSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.ScheduleChecklistRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SetChecklistItemPausedUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SnoozeChecklistItemUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.SyncChecklistRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.checklist.UpdateChecklistDayUseCase
import org.thelazybattley.macrotrack.features.checklist.ChecklistFormViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistHistoryViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistItemViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistManageViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistViewModel
import org.thelazybattley.macrotrack.domain.usecase.whatfits.GetFitFoodsUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.GetWhatFitsSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.GetWhatFitsInputsUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.LogFitPicksUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.PlanFitPicksUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.SetWhatFitsSettingsUseCase
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewModel
import org.thelazybattley.macrotrack.features.foodhistory.FoodHistoryViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayAllViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayDinnerViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayMorningViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayWeightViewModel
import org.thelazybattley.macrotrack.domain.usecase.nextday.GetMorningExplanationUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.GetNextDayInputsUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.SetNextDayHeadsUpUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.SetNextDaySettingsUseCase
import org.thelazybattley.macrotrack.data.local.tour.TourWaterDao
import org.thelazybattley.macrotrack.data.repository.WaterRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.WaterRepository
import org.thelazybattley.macrotrack.domain.usecase.water.AddWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.DeleteWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.GetDueWaterReminderUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.GetWaterSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.GetWaterSummaryUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.GetWaterWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.SaveWaterSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.ScheduleWaterRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.SyncWaterRemindersUseCase
import org.thelazybattley.macrotrack.features.water.WaterSettingsViewModel
import org.thelazybattley.macrotrack.features.water.WaterViewModel
import org.thelazybattley.macrotrack.data.repository.LogReminderRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.LogReminderRepository
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetDueLogRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetLogReminderMessageUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetLogReminderSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.LogReminderActions
import org.thelazybattley.macrotrack.domain.usecase.reminder.LogReminderMessages
import org.thelazybattley.macrotrack.domain.usecase.reminder.SaveLogReminderSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.ScheduleLogRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.SendTestLogReminderUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.SyncLogRemindersUseCase
import org.thelazybattley.macrotrack.features.reminders.NotificationRouteRequest
import org.thelazybattley.macrotrack.features.reminders.RemindersViewModel
import org.thelazybattley.macrotrack.domain.usecase.widget.GetWidgetSnapshotUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.IsWidgetWeightShownUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.SetWidgetWeightShownUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.SetWidgetLockNumbersShownUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.SyncHomeWidgetsUseCase
import org.thelazybattley.macrotrack.features.widgets.WidgetsViewModel
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
import org.thelazybattley.macrotrack.domain.usecase.checkin.SetCheckInSettingsUseCase
import io.ktor.client.plugins.HttpTimeout
import org.thelazybattley.macrotrack.data.repository.FoodLibraryRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.OpenFoodFactsRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.FoodLibraryRepository
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.features.foodlibrary.FoodLibraryViewModel
import org.thelazybattley.macrotrack.domain.repository.ProductRepository
import org.thelazybattley.macrotrack.domain.usecase.food.GetOnlineFoodDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.LookUpBarcodeUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.SaveOnlineFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.SearchOnlineFoodsUseCase
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
import org.thelazybattley.macrotrack.domain.usecase.mealplan.RestoreRepeatingMealUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SaveRepeatingMealUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SetRepeatingMealActiveUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SkipPlannedFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SyncRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.UndoPlannedFoodUseCase
import org.thelazybattley.macrotrack.features.mealplan.MealPlanViewModel
import org.thelazybattley.macrotrack.features.mealplan.RepeatMealViewModel
import org.thelazybattley.macrotrack.features.mealplan.RepeatingMealsViewModel
import org.thelazybattley.macrotrack.features.share.ShareCardViewModel
import org.thelazybattley.macrotrack.domain.usecase.recipe.DeleteRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetRecipeByNameUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.InsertRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.UpdateRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.AddCustomMilestoneUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.ApplyDailyCaloriesUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.DeleteCustomMilestoneUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.GetGoalForecastUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.SetForecastSettingsUseCase
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
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewModel
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
    single<WaterDao> { TourWaterDao(db = get()) }
    single<ChecklistDao> { TourChecklistDao(db = get()) }
    single<MeasurementDao> { TourMeasurementDao(db = get()) }
    single<DailyGoalDao> { TourDailyGoalDao(db = get()) }
    single<FavoriteDao> { TourFavoriteDao(db = get()) }
    single { get<AppDatabase>().appFlagDao() }
    single { get<AppDatabase>().backupDao() }
    single<MealPlanDao> { TourMealPlanDao(db = get()) }
    single<ProgressPhotoDao> { TourProgressPhotoDao(db = get()) }
    singleOf(::MeasurementUnitPreference)
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
    singleOf(::ProgressPhotoRepositoryImpl) { bind<ProgressPhotoRepository>() }
    singleOf(::PhotoDrafts)
    singleOf(::LogReminderRepositoryImpl) { bind<LogReminderRepository>() }
    singleOf(::WaterRepositoryImpl) { bind<WaterRepository>() }
    singleOf(::ChecklistRepositoryImpl) { bind<ChecklistRepository>() }
    singleOf(::NotificationRouteRequest)
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
    factoryOf(::GetGoalForecastUseCase)
    factoryOf(::SetForecastSettingsUseCase)
    factoryOf(::GetFitFoodsUseCase)
    factoryOf(::GetWhatFitsSettingsUseCase)
    factoryOf(::GetWhatFitsInputsUseCase)
    factoryOf(::SetWhatFitsSettingsUseCase)
    factoryOf(::LogFitPicksUseCase)
    factoryOf(::PlanFitPicksUseCase)
    factoryOf(::AddCustomMilestoneUseCase)
    factoryOf(::DeleteCustomMilestoneUseCase)
    factoryOf(::ApplyDailyCaloriesUseCase)
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
    factoryOf(::SearchOnlineFoodsUseCase)
    factoryOf(::GetOnlineFoodDetailsUseCase)
    factoryOf(::SaveOnlineFoodUseCase)
    factoryOf(::GetRemainingCaloriesUseCase)
    factoryOf(::GetWeeklyCheckInUseCase)
    factoryOf(::CompleteCheckInUseCase)
    factoryOf(::GetWeekBalanceUseCase)
    factoryOf(::GetWeekCardUseCase)
    factoryOf(::MarkCheckInSeenUseCase)
    factoryOf(::SetCheckInSettingsUseCase)
    factoryOf(::GetNextDayInputsUseCase)
    factoryOf(::GetMorningExplanationUseCase)
    factoryOf(::SetNextDaySettingsUseCase)
    factoryOf(::SetNextDayHeadsUpUseCase)
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
    factoryOf(::RestoreRepeatingMealUseCase)
    factoryOf(::GetPhotoAlbumsUseCase)
    factoryOf(::GetProgressPhotosUseCase)
    factoryOf(::SavePhotoAlbumUseCase)
    factoryOf(::DeletePhotoAlbumUseCase)
    factoryOf(::SaveProgressPhotosUseCase)
    factoryOf(::DeleteProgressPhotoUseCase)
    factoryOf(::SchedulePhotoRemindersUseCase)
    factoryOf(::LogReminderMessages)
    factoryOf(::GetLogReminderSettingsUseCase)
    factoryOf(::SaveLogReminderSettingsUseCase)
    factoryOf(::ScheduleLogRemindersUseCase)
    factoryOf(::SyncLogRemindersUseCase)
    factoryOf(::GetWaterWeightUseCase)
    factoryOf(::GetWaterSummaryUseCase)
    factoryOf(::GetWaterSettingsUseCase)
    factoryOf(::SaveWaterSettingsUseCase)
    factoryOf(::AddWaterUseCase)
    factoryOf(::DeleteWaterUseCase)
    factoryOf(::ScheduleWaterRemindersUseCase)
    factoryOf(::SyncWaterRemindersUseCase)
    factoryOf(::GetDueWaterReminderUseCase)
    factoryOf(::GetChecklistUseCase)
    factoryOf(::GetChecklistItemUseCase)
    factoryOf(::UpdateChecklistDayUseCase)
    factoryOf(::SaveChecklistItemUseCase)
    factoryOf(::DeleteChecklistItemUseCase)
    factoryOf(::SetChecklistItemPausedUseCase)
    factoryOf(::ReorderChecklistUseCase)
    factoryOf(::RefillChecklistItemUseCase)
    factoryOf(::SaveChecklistSettingsUseCase)
    factoryOf(::SnoozeChecklistItemUseCase)
    factoryOf(::ScheduleChecklistRemindersUseCase)
    factoryOf(::SyncChecklistRemindersUseCase)
    factoryOf(::GetDueChecklistRemindersUseCase)
    factoryOf(::HandleChecklistReminderActionUseCase)
    factoryOf(::GetDueLogRemindersUseCase)
    factoryOf(::GetLogReminderMessageUseCase)
    factoryOf(::SendTestLogReminderUseCase)
    factoryOf(::LogReminderActions)
    factoryOf(::GetWidgetSnapshotUseCase)
    factoryOf(::SyncHomeWidgetsUseCase)
    factoryOf(::IsWidgetWeightShownUseCase)
    factoryOf(::SetWidgetWeightShownUseCase)
    factoryOf(::SetWidgetLockNumbersShownUseCase)
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
    viewModelOf(::GoalForecastViewModel)
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
    viewModelOf(::ShareCardViewModel)
    viewModelOf(::ProgressPhotosViewModel)
    viewModelOf(::PhotoAlbumViewModel)
    viewModelOf(::PhotoCameraViewModel)
    viewModelOf(::PhotoSaveViewModel)
    viewModelOf(::PhotoCompareViewModel)
    viewModelOf(::RemindersViewModel)
    viewModelOf(::WidgetsViewModel)
    viewModelOf(::WaterViewModel)
    viewModelOf(::WaterSettingsViewModel)
    viewModelOf(::ChecklistViewModel)
    viewModelOf(::ChecklistFormViewModel)
    viewModelOf(::ChecklistItemViewModel)
    viewModelOf(::ChecklistHistoryViewModel)
    viewModelOf(::ChecklistManageViewModel)
    viewModelOf(::WhatFitsViewModel)
    viewModelOf(::FoodHistoryViewModel)
    viewModelOf(::NextDayWeightViewModel)
    viewModelOf(::NextDayAllViewModel)
    viewModelOf(::NextDayDinnerViewModel)
    viewModelOf(::NextDayMorningViewModel)
}

fun initKoin(extraModules: List<Module> = emptyList()) {
    startKoin {
        printLogger()
        modules(listOf(platformModule, sharedModule, viewModelModule, useCaseModule, repositoryModule) + extraModules)
    }
}
