package io.github.denis201git.tgspyglass.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.denis201git.tgspyglass.data.database.entity.MediaEntity

@Dao
interface MediaDao {

    @Insert(
        onConflict = OnConflictStrategy.ABORT
    )
    suspend fun insertAll(
        media: List<MediaEntity>
    )

    @Query("""
        DELETE FROM media
        WHERE messageId = :messageId
    """)
    suspend fun deleteForMessage(
        messageId: Long
    )
}