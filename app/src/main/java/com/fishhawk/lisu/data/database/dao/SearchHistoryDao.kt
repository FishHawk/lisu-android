package com.fishhawk.lisu.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fishhawk.lisu.data.database.model.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistory ORDER BY date DESC")
    fun list(): Flow<List<SearchHistory>>

    @Query("SELECT * FROM SearchHistory WHERE providerId = :providerId ORDER BY date DESC")
    fun listByProvider(providerId: String): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistory)

    @Query("DELETE FROM SearchHistory WHERE providerId = :providerId AND keywords = :keywords")
    suspend fun deleteByKeywords(providerId: String, keywords: String)

    @Query("DELETE FROM SearchHistory")
    suspend fun clear()
}