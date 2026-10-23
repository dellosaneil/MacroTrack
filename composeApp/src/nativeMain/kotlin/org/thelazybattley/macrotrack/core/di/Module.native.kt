package org.thelazybattley.macrotrack.core.di

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import org.thelazybattley.macrotrack.core.billing.BillingService
import org.thelazybattley.macrotrack.core.billing.UnavailableBillingService
import org.thelazybattley.macrotrack.core.platform.IosLocationTracker
import org.thelazybattley.macrotrack.core.platform.IosStepSensor
import org.thelazybattley.macrotrack.core.platform.LocationTracker
import org.thelazybattley.macrotrack.core.platform.NoStepNotifier
import org.thelazybattley.macrotrack.core.platform.NoStepNudgeScheduler
import org.thelazybattley.macrotrack.core.platform.StepNotifier
import org.thelazybattley.macrotrack.core.platform.StepNudgeScheduler
import org.thelazybattley.macrotrack.core.platform.StepSensor
import org.thelazybattley.macrotrack.data.local.AppDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module = module {
    single {
        val dbFilePath = documentDirectory() + "/my_room.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
        ).addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17)
    }
    single<BillingService> { UnavailableBillingService() }
    single<StepSensor> { IosStepSensor() }
    single<LocationTracker> { IosLocationTracker() }
    single<StepNudgeScheduler> { NoStepNudgeScheduler }
    single<StepNotifier> { NoStepNotifier }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
