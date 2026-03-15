package org.thelazybattley.macrotrack.core.di

import getRoomDatabase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.thelazybattley.macrotrack.data.local.AppDatabase
import org.thelazybattley.macrotrack.ui.TestViewModel

expect val platformModule: Module

val sharedModule = module {
    single<AppDatabase> { getRoomDatabase(get()) }
    single { get<AppDatabase>().foodDao() }
}

val viewModelModule = module {
    viewModel { TestViewModel(get()) }
}

fun initKoin() {
    startKoin {
        printLogger()
        modules(platformModule, sharedModule, viewModelModule)
    }
}
