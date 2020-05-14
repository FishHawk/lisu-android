package com.fishhawk.driftinglibraryandroid.repository.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM ReadingHistory")
    fun getAll(): List<ReadingHistory>

    @Query("SELECT * FROM ReadingHistory WHERE id = :id")
    fun findById(id: String): ReadingHistory

    @Query("SELECT * FROM ReadingHistory WHERE id = :id")
    fun observe(id: String): LiveData<ReadingHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: ReadingHistory)

    @Update
    fun update(history: ReadingHistory)
}

