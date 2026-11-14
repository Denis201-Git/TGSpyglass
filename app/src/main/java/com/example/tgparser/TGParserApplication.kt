package com.example.tgparser

import android.app.Application
import androidx.room.Room
import com.example.tgparser.data.database.AppDatabase
import com.example.tgparser.data.database.AppDatabaseProvider

class TGParserApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tgparser.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        AppDatabaseProvider.initialize(database)
    }
}