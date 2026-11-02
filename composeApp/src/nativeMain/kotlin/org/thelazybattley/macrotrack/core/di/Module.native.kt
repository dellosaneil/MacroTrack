package org.thelazybattley.macrotrack.core.di

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import org.thelazybattley.macrotrack.core.billing.BillingService
import org.thelazybattley.macrotrack.core.billing.UnavailableBillingService
import org.thelazybattley.macrotrack.core.platform.IosLogReminderScheduler
import org.thelazybattley.macrotrack.core.platform.IosPhotoReminderScheduler
import org.thelazybattley.macrotrack.core.platform.LogReminderScheduler
import org.thelazybattley.macrotrack.core.platform.IosPhotoStorage
import org.thelazybattley.macrotrack.core.platform.PhotoReminderScheduler
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import org.thelazybattley.macrotrack.core.platform.IosWaterReminderScheduler
import org.thelazybattley.macrotrack.core.platform.AppShortcutPublisher
import org.thelazybattley.macrotrack.core.platform.IosAppShortcutPublisher
import org.thelazybattley.macrotrack.core.platform.WaterReminderScheduler
import org.thelazybattley.macrotrack.core.platform.ChecklistReminderScheduler
import org.thelazybattley.macrotrack.core.platform.IosChecklistReminderScheduler
import org.thelazybattley.macrotrack.data.local.AppDatabase
import org.koin.core.qualifier.named
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module = module {
    single {
        val dbFilePath = documentDirectory() + "/my_room.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
        ).addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17, AppDatabase.MIGRATION_17_18, AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20, AppDatabase.MIGRATION_20_21, AppDatabase.MIGRATION_21_22, AppDatabase.MIGRATION_22_23, AppDatabase.MIGRATION_23_24)
    }
    factory(qualifier = named(name = SAMPLE_DATABASE)) { Room.inMemoryDatabaseBuilder<AppDatabase>() }
    single<BillingService> { UnavailableBillingService() }
    single<PhotoStorage> { IosPhotoStorage() }
    single<PhotoReminderScheduler> { IosPhotoReminderScheduler() }
    single<LogReminderScheduler> { IosLogReminderScheduler() }
    single<WaterReminderScheduler> { IosWaterReminderScheduler() }
    single<ChecklistReminderScheduler> { IosChecklistReminderScheduler() }
    single<AppShortcutPublisher> { IosAppShortcutPublisher() }
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
