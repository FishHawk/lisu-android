package com.fishhawk.driftinglibraryandroid.data.database.dao

import androidx.room.*
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerInfoDao {
    @Query("SELECT * FROM ServerInfo ORDER BY position ASC")
    fun list(): Flow<List<ServerInfo>>

    @Query("SELECT * FROM ServerInfo WHERE id = :id")
    fun select(id: Int): Flow<ServerInfo?>

    @Insert
    suspend fun insert(serverInfo: ServerInfo)

    @Update
    suspend fun update(serverInfo: ServerInfo)

    @Delete
    suspend fun delete(serverInfo: ServerInfo)
}

