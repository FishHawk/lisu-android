package com.fishhawk.lisu.data.database

import com.fishhawk.lisu.data.database.dao.ReadingHistoryDao
import com.fishhawk.lisu.data.database.model.ReadingHistory

class ReadingHistoryRepository(private val dao: ReadingHistoryDao) {

    fun list() = dao.list()

    fun select(mangaId: String) = dao.select(mangaId)

    suspend fun update(history: ReadingHistory) = dao.updateOrInsert(history)

    suspend fun delete(history: ReadingHistory) = dao.delete(history)

    suspend fun clear() = dao.clear()
}