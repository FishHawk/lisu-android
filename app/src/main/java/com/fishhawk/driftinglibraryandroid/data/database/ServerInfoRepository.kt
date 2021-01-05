package com.fishhawk.driftinglibraryandroid.data.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.fishhawk.driftinglibraryandroid.data.database.dao.ServerInfoDao
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo

class ServerInfoRepository(
    private val dao: ServerInfoDao
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun observeServerInfo(id: Int): LiveData<ServerInfo> = dao.observe(id)

    suspend fun listAllServerInfo(): List<ServerInfo> =
        withContext(ioDispatcher) { dao.list() }

    suspend fun selectServerInfo(id: Int): ServerInfo? =
        withContext(ioDispatcher) { dao.select(id) }

    suspend fun insertServerInfo(serverInfo: ServerInfo) =
        withContext(ioDispatcher) { dao.insert(serverInfo) }

    suspend fun updateServerInfo(serverInfo: ServerInfo) =
        withContext(ioDispatcher) { dao.update(serverInfo) }

    suspend fun deleteServerInfo(serverInfo: ServerInfo) =
        withContext(ioDispatcher) { dao.delete(serverInfo) }
}