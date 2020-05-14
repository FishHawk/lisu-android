package com.fishhawk.driftinglibraryandroid.repository

import androidx.lifecycle.LiveData
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadingHistoryRepository(
    private val dao: ReadingHistoryDao
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun observeReadingHistory(id: String): LiveData<ReadingHistory> = dao.observe(id)
    fun observeAllReadingHistory(): LiveData<List<ReadingHistory>> = dao.observeAll()

    suspend fun updateReadingHistory(readingHistory: ReadingHistory) = withContext(ioDispatcher) {
        dao.insert(readingHistory)
    }
}