package com.fishhawk.driftinglibraryandroid.data.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.fishhawk.driftinglibraryandroid.data.database.dao.ReadingHistoryDao
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory

class ReadingHistoryRepository(
    private val dao: ReadingHistoryDao
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun observe(serverId: Int, mangaId: String): LiveData<ReadingHistory> =
        dao.observe(serverId, mangaId)

    fun getAll(serverId: Int) = dao.getAll(serverId)

    suspend fun update(readingHistory: ReadingHistory) =
        withContext(ioDispatcher) {
            dao.insert(readingHistory)
        }

    suspend fun clear(serverId: Int) =
        withContext(ioDispatcher) {
            dao.deleteAll(serverId)
        }
}