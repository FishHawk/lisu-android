package com.fishhawk.driftinglibraryandroid.data.database

import com.fishhawk.driftinglibraryandroid.data.database.dao.ReadingHistoryDao
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory

class ReadingHistoryRepository(private val dao: ReadingHistoryDao) {

    fun list(serverId: Int) = dao.list(serverId)

    fun select(serverId: Int, mangaId: String) = dao.select(serverId, mangaId)

    suspend fun update(readingHistory: ReadingHistory) = dao.insert(readingHistory)

    suspend fun clear(serverId: Int) = dao.clear(serverId)
}