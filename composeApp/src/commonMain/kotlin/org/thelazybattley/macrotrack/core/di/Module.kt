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
import org.thelazybattley.macrotrack.data.repository.FoodRepositoryImpl
import org.thelazybattley.macrotrack.domain.repository.FoodRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.GetFoodByNameUseCase
import org.thelazybattley.macrotrack.domain.usecase.InsertFoodUseCase
import org.thelazybattley.macrotrack.ui.TestViewModel

expect val platformModule: Module

val sharedModule = module {
    single<AppDatabase> { getRoomDatabase(get()) }
    single { get<AppDatabase>().foodDao() }
    singleOf(::FoodRepositoryImpl) { bind<FoodRepository>() }
}

val useCaseModule = module {
    factoryOf(::CalculateTDEEUseCase)
    factoryOf(::InsertFoodUseCase)
    factoryOf(::GetAllFoodUseCase)
    factoryOf(::GetFoodByNameUseCase)
}

val viewModelModule = module {
    viewModelOf(::TestViewModel)
}

fun initKoin() {
    startKoin {
        printLogger()
        modules(platformModule, sharedModule, viewModelModule, useCaseModule)
    }
}
