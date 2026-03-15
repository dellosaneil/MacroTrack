package org.thelazybattley.macrotrack

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.thelazybattley.macrotrack.core.di.platformModule
import org.thelazybattley.macrotrack.core.di.sharedModule
import org.thelazybattley.macrotrack.core.di.viewModelModule

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(platformModule, sharedModule, viewModelModule)
        }
    }
}
