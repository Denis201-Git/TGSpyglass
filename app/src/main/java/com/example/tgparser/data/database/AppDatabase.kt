package com.example.tgparser.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tgparser.data.database.dao.ChannelDao
import com.example.tgparser.data.database.dao.MediaDao
import com.example.tgparser.data.database.dao.MessageDao
import com.example.tgparser.data.database.entity.ChannelEntity
import com.example.tgparser.data.database.entity.MediaEntity
import com.example.tgparser.data.database.entity.MessageEntity

@Database(
    entities = [
        ChannelEntity::class,
        MessageEntity::class,
        MediaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun channelDao(): ChannelDao

    abstract fun messageDao(): MessageDao

    abstract fun mediaDao(): MediaDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE media ADD COLUMN notSupported INTEGER NOT NULL DEFAULT 0"
                )

            }

        }

        val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE messages ADD COLUMN links TEXT NOT NULL DEFAULT ''"
                )

            }

        }

    }

}