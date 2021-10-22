package com.fishhawk.lisu.data.database.dao

import androidx.room.*
import com.fishhawk.lisu.data.database.model.ServerHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerHistoryDao {
    @Query("SELECT * FROM ServerHistory ORDER BY date DESC")
    fun list(): Flow<List<ServerHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ServerHistory)

    @Delete
    suspend fun delete(history: ServerHistory)

    @Query("DELETE FROM ServerHistory")
    suspend fun clear()
}