package org.thelazybattley.macrotrack.core.di

import getRoomDatabase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.thelazybattley.macrotrack.data.local.AppDatabase
import org.thelazybattley.macrotrack.data.repository.FoodLogRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.FoodRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.RecipeRepositoryImpl
import org.thelazybattley.macrotrack.data.repository.UserDetailsRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository
import org.thelazybattley.macrotrack.domain.repository.FoodRepository
import org.thelazybattley.macrotrack.domain.repository.RecipeRepository
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateCaloriesFromMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacroPercentageUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetFoodByNameUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.InsertFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.DeleteFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetFoodLogByDateUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetRecipeByNameUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.InsertRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.UpdateRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewModel
import org.thelazybattley.macrotrack.features.createfood.CreateFoodViewModel
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewModel
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewModel
import org.thelazybattley.macrotrack.features.home.HomeTabViewModel
import org.thelazybattley.macrotrack.features.onboarding.OnboardingViewModel
import org.thelazybattley.macrotrack.features.splash.SplashViewModel

expect val platformModule: Module

val sharedModule = module {
    single<AppDatabase> { getRoomDatabase(get()) }
    single { get<AppDatabase>().foodDao() }
    single { get<AppDatabase>().recipeDao() }
    single { get<AppDatabase>().userDetailsDao() }
    single { get<AppDatabase>().foodLogDao() }
}

val repositoryModule = module {
    singleOf(::FoodRepositoryImpl) { bind<FoodRepository>() }
    singleOf(::RecipeRepositoryImpl) { bind<RecipeRepository>() }
    singleOf(::UserDetailsRepositoryImpl) { bind<UserDetailsRepository>() }
    singleOf(::FoodLogRepositoryImpl) { bind<FoodLogRepository>() }
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
    factoryOf(::InsertFoodLogUseCase)
    factoryOf(::CalculateCaloriesFromMacrosUseCase)
    factoryOf(::CalculateAdjustMacrosUseCase)
    factoryOf(::CalculateMacroPercentageUseCase)
    factoryOf(::GetFoodLogByDateUseCase)
    factoryOf(::UpdateRecipeUseCase)
}

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::HomeTabViewModel)
    viewModelOf(::AddMealViewModel)
    viewModelOf(::FoodLogViewModel)
    viewModelOf(::CreateFoodViewModel)
    viewModelOf(::CreateRecipeViewModel)
}

fun initKoin() {
    startKoin {
        printLogger()
        modules(platformModule, sharedModule, viewModelModule, useCaseModule, repositoryModule)
    }
}
