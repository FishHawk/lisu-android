package com.fishhawk.driftinglibraryandroid.data.database.dao

import androidx.room.*
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM ReadingHistory WHERE serverId = :serverId ORDER BY date DESC")
    fun list(serverId: Int): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM ReadingHistory WHERE serverId = :serverId AND mangaId = :mangaId")
    fun select(serverId: Int, mangaId: String): Flow<ReadingHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ReadingHistory)

    @Update
    suspend fun update(history: ReadingHistory)

    @Query("DELETE FROM ReadingHistory WHERE serverId = :id")
    suspend fun clear(id: Int)
}

