package com.fishhawk.lisu.data.database

import com.fishhawk.lisu.data.database.dao.SearchHistoryDao
import com.fishhawk.lisu.data.database.model.SearchHistory

class SearchHistoryRepository(private val dao: SearchHistoryDao) {

    fun list() = dao.list()

    fun listByProvider(providerId: String) = dao.listByProvider(providerId)

    suspend fun update(history: SearchHistory) = dao.insert(history)

    suspend fun deleteByKeywords(providerId: String, keywords: String) =
        dao.deleteByKeywords(providerId, keywords)

    suspend fun clear() = dao.clear()
}