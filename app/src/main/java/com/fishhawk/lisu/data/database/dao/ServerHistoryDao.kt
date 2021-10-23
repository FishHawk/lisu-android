package com.fishhawk.lisu.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fishhawk.lisu.data.database.model.ServerHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerHistoryDao {
    @Query("SELECT * FROM ServerHistory ORDER BY date DESC")
    fun list(): Flow<List<ServerHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ServerHistory)

    @Query("DELETE FROM ServerHistory WHERE address = :address")
    suspend fun deleteByAddress(address: String)

    @Query("DELETE FROM ServerHistory")
    suspend fun clear()
}