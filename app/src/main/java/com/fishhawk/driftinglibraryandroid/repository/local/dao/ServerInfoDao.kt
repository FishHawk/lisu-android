package com.fishhawk.driftinglibraryandroid.repository.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo

@Dao
interface ServerInfoDao {
    @Query("SELECT * FROM ServerInfo")
    fun observeAll(): LiveData<List<ServerInfo>>

    @Query("SELECT * FROM ServerInfo WHERE id = :id")
    fun observe(id: Int): LiveData<ServerInfo>

    @Query("SELECT * FROM ServerInfo ORDER BY position ASC")
    fun list(): List<ServerInfo>

    @Query("SELECT * FROM ServerInfo WHERE id = :id")
    fun select(id: Int): ServerInfo?

    @Insert
    fun insert(serverInfo: ServerInfo)

    @Update
    fun update(serverInfo: ServerInfo)

    @Delete
    fun delete(serverInfo: ServerInfo)
}

