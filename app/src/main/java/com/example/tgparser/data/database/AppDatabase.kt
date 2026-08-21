package com.example.tgparser.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun channelDao(): ChannelDao

    abstract fun messageDao(): MessageDao

    abstract fun mediaDao(): MediaDao
}