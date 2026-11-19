package io.github.denis201git.tgspyglass

import android.app.Application
import androidx.room.Room
import io.github.denis201git.tgspyglass.data.database.AppDatabase
import io.github.denis201git.tgspyglass.data.database.AppDatabaseProvider

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