package org.thelazybattley.macrotrack.core.di

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

expect val platformModule: Module

val sharedModule = module {
    single<AppDatabase> { getRoomDatabase(get()) }
    single { get<AppDatabase>().foodDao() }
    single { get<AppDatabase>().recipeDao() }
    single { get<AppDatabase>().userDetailsDao() }
    single { get<AppDatabase>().foodLogDao() }
    single { get<AppDatabase>().weightDao() }
    single { get<AppDatabase>().measurementDao() }
    single { get<AppDatabase>().dailyGoalDao() }
    single { get<AppDatabase>().favoriteDao() }
    single { get<AppDatabase>().appFlagDao() }
    single { MeasurementUnitPreference() }
}

val repositoryModule = module {
    singleOf(::FoodRepositoryImpl) { bind<FoodRepository>() }
    singleOf(::RecipeRepositoryImpl) { bind<RecipeRepository>() }
    singleOf(::UserDetailsRepositoryImpl) { bind<UserDetailsRepository>() }
    singleOf(::FoodLogRepositoryImpl) { bind<FoodLogRepository>() }
    singleOf(::LogDateRequest)
    singleOf(::WeightRepositoryImpl) { bind<WeightRepository>() }
    singleOf(::MeasurementRepositoryImpl) { bind<MeasurementRepository>() }
    singleOf(::DailyGoalRepositoryImpl) { bind<DailyGoalRepository>() }
    singleOf(::FavoriteRepositoryImpl) { bind<FavoriteRepository>() }
    singleOf(::AppFlagRepositoryImpl) { bind<AppFlagRepository>() }
    singleOf(::ProRepositoryImpl) { bind<ProRepository>() }
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
    factory { GetProStatusUseCase(repository = get(), isPaywallEnabled = isPaywallEnabled) }
    factoryOf(::StartProTrialUseCase)
    factoryOf(::SetProPurchasedUseCase)
    factoryOf(::IsTrialEndedNoticeDismissedUseCase)
    factoryOf(::DismissTrialEndedNoticeUseCase)
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
}

fun initKoin() {
    startKoin {
        printLogger()
        modules(platformModule, sharedModule, viewModelModule, useCaseModule, repositoryModule)
    }
}
