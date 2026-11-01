package org.thelazybattley.macrotrack.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import org.thelazybattley.macrotrack.core.billing.BillingService
import org.thelazybattley.macrotrack.core.billing.PlayBillingService
import org.thelazybattley.macrotrack.core.platform.AndroidAppShortcutPublisher
import org.thelazybattley.macrotrack.core.platform.AndroidLogReminderScheduler
import org.thelazybattley.macrotrack.core.platform.AppShortcutPublisher
import org.thelazybattley.macrotrack.core.platform.HomeWidgetRefresher
import org.thelazybattley.macrotrack.widget.AndroidHomeWidgetRefresher
import org.thelazybattley.macrotrack.core.platform.AndroidPhotoReminderScheduler
import org.thelazybattley.macrotrack.core.platform.LogReminderScheduler
import org.thelazybattley.macrotrack.core.platform.AndroidPhotoStorage
import org.thelazybattley.macrotrack.core.platform.PhotoReminderScheduler
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import org.thelazybattley.macrotrack.core.platform.AndroidWaterReminderScheduler
import org.thelazybattley.macrotrack.core.platform.WaterReminderScheduler
import org.thelazybattley.macrotrack.core.telemetry.FirebaseTelemetry
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.data.local.AppDatabase
import org.koin.core.qualifier.named

actual val platformModule: Module = module {
    single {
        getDatabaseBuilder(get())
    }
    factory(qualifier = named(name = SAMPLE_DATABASE)) {
        Room.inMemoryDatabaseBuilder<AppDatabase>(context = get<Context>().applicationContext)
    }
    // Created with Koin so it sees the first activity resume; it needs that activity to open the purchase sheet.
    single<BillingService>(createdAtStart = true) { PlayBillingService(context = get(), telemetry = get()) }
    single<Telemetry> { FirebaseTelemetry.create(context = get()) }
    single<PhotoStorage> { AndroidPhotoStorage(context = get()) }
    single<PhotoReminderScheduler> { AndroidPhotoReminderScheduler(context = get()) }
    single<LogReminderScheduler> { AndroidLogReminderScheduler(context = get()) }
    single<WaterReminderScheduler> { AndroidWaterReminderScheduler(context = get()) }
    single<AppShortcutPublisher> { AndroidAppShortcutPublisher(context = get()) }
    single<HomeWidgetRefresher> { AndroidHomeWidgetRefresher(context = get()) }
}

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17, AppDatabase.MIGRATION_17_18, AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20, AppDatabase.MIGRATION_20_21, AppDatabase.MIGRATION_21_22)
}
