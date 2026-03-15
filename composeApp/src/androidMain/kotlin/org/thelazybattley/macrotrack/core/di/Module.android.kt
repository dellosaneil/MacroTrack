package org.thelazybattley.macrotrack.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import org.thelazybattley.macrotrack.data.local.AppDatabase

actual val platformModule: Module = module {
    single {
        getDatabaseBuilder(get())
    }
}

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
