package com.fishhawk.lisu.data.database

import com.fishhawk.lisu.data.database.dao.MangaSettingDao
import com.fishhawk.lisu.data.database.model.MangaSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest

class MangaSettingRepository(private val dao: MangaSettingDao) {
    fun select(providerId: String, mangaId: String, title: String?): Flow<MangaSetting?> =
        dao.select(providerId, mangaId)
            .transformLatest {
                if (it != null || title.isNullOrBlank()) emit(it)
                else emitAll(dao.selectMostSimilar(title))
            }

    suspend fun update(history: MangaSetting) = dao.insertOrUpdate(history)
}