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

    fun observeReadingHistory(serverId: Int, mangaId: String): LiveData<ReadingHistory> =
        dao.observe(serverId, mangaId)

    fun observeAllReadingHistoryOfServer(serverId: Int): LiveData<List<ReadingHistory>> =
        dao.observeAllOfServer(serverId)

    suspend fun updateReadingHistory(readingHistory: ReadingHistory) =
        withContext(ioDispatcher) {
            dao.insert(readingHistory)
        }

    suspend fun clearReadingHistoryOfServer(serverId: Int) =
        withContext(ioDispatcher) {
            dao.deleteAllOfServer(serverId)
        }
}