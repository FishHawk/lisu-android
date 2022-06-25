package com.fishhawk.lisu.data.database.dao

import androidx.room.*
import com.fishhawk.lisu.data.database.model.MangaSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaSettingDao {
    @Query("SELECT * FROM MangaSetting WHERE providerId = :providerId AND mangaId = :mangaId")
    fun select(providerId: String, mangaId: String): Flow<MangaSetting?>

    @Query("SELECT * FROM MangaSetting WHERE title = :title")
    fun selectMostSimilar(title: String): Flow<MangaSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: MangaSetting)
}

