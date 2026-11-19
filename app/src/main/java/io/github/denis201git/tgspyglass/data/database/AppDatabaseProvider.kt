package io.github.denis201git.tgspyglass.data.database

class AppDatabaseProvider {

    companion object {

        private var instance: AppDatabase? = null

        fun initialize(database: AppDatabase) {
            instance = database
        }

        fun get(): AppDatabase {
            return instance
                ?: error("AppDatabaseProvider is not initialized")
        }
    }
}