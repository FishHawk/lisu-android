package com.fishhawk.driftinglibraryandroid.data.database.dao

import androidx.room.*
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM ReadingHistory ORDER BY date DESC")
    fun list(): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM ReadingHistory WHERE mangaId = :mangaId")
    fun select(mangaId: String): Flow<ReadingHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ReadingHistory)

    @Update
    suspend fun update(history: ReadingHistory)

    @Query("DELETE FROM ReadingHistory")
    suspend fun clear()
}

