package org.thelazybattley.macrotrack.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import org.thelazybattley.macrotrack.core.billing.BillingService
import org.thelazybattley.macrotrack.core.billing.PlayBillingService
import org.thelazybattley.macrotrack.core.platform.AndroidLocationTracker
import org.thelazybattley.macrotrack.core.platform.AndroidStepNudgeScheduler
import org.thelazybattley.macrotrack.core.platform.AndroidStepSensor
import org.thelazybattley.macrotrack.core.platform.LocationTracker
import org.thelazybattley.macrotrack.core.platform.StepNudgeScheduler
import org.thelazybattley.macrotrack.core.platform.StepSensor
import org.thelazybattley.macrotrack.core.telemetry.FirebaseTelemetry
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.data.local.AppDatabase

actual val platformModule: Module = module {
    single {
        getDatabaseBuilder(get())
    }
    // Created with Koin so it sees the first activity resume; it needs that activity to open the purchase sheet.
    single<BillingService>(createdAtStart = true) { PlayBillingService(context = get(), telemetry = get()) }
    single<Telemetry> { FirebaseTelemetry.create(context = get()) }
    single<StepSensor> { AndroidStepSensor(context = get()) }
    single<LocationTracker> { AndroidLocationTracker(context = get()) }
    single<StepNudgeScheduler> { AndroidStepNudgeScheduler(context = get()) }
}

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17)
}
