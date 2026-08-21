package com.example.tgparser.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tgparser.data.database.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @Query("""
        SELECT *
        FROM channels
        ORDER BY name
    """)
    fun observeAll(): Flow<List<ChannelEntity>>

    @Query("""
        SELECT *
        FROM channels
        WHERE telegramId = :telegramId
        LIMIT 1
    """)
    suspend fun getByTelegramId(
        telegramId: String
    ): ChannelEntity?

    @Insert(
        onConflict = OnConflictStrategy.ABORT
    )
    suspend fun insert(
        channel: ChannelEntity
    )

    @Update
    suspend fun update(
        channel: ChannelEntity
    )

    @Delete
    suspend fun delete(
        channel: ChannelEntity
    )
}