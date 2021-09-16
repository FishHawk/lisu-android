package com.fishhawk.driftinglibraryandroid.data.database

import com.fishhawk.driftinglibraryandroid.data.database.dao.ReadingHistoryDao
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory

class ReadingHistoryRepository(private val dao: ReadingHistoryDao) {

    fun list() = dao.list()

    fun select(mangaId: String) = dao.select(mangaId)

    suspend fun update(readingHistory: ReadingHistory) = dao.insert(readingHistory)

    suspend fun delete(readingHistory: ReadingHistory) = dao.delete(readingHistory)

    suspend fun clear() = dao.clear()
}