package io.github.denis201git.tgspyglass.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.denis201git.tgspyglass.data.database.dao.ChannelDao
import io.github.denis201git.tgspyglass.data.database.dao.MediaDao
import io.github.denis201git.tgspyglass.data.database.dao.MessageDao
import io.github.denis201git.tgspyglass.data.database.entity.ChannelEntity
import io.github.denis201git.tgspyglass.data.database.entity.MediaEntity
import io.github.denis201git.tgspyglass.data.database.entity.MessageEntity

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