package com.fishhawk.driftinglibraryandroid.repository.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM ReadingHistory WHERE serverId = :serverId AND mangaId = :mangaId")
    fun observe(serverId: Int, mangaId: String): LiveData<ReadingHistory>

    @Query("SELECT * FROM ReadingHistory WHERE serverId = :serverId ORDER BY date DESC")
    fun observeAllOfServer(serverId: Int): LiveData<List<ReadingHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: ReadingHistory)

    @Update
    fun update(history: ReadingHistory)

    @Query("DELETE FROM ReadingHistory WHERE serverId = :id")
    fun deleteAllOfServer(id: Int)
}

