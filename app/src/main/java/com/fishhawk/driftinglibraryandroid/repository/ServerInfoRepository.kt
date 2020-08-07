package com.fishhawk.driftinglibraryandroid.repository

import androidx.lifecycle.LiveData
import com.fishhawk.driftinglibraryandroid.repository.data.ServerInfo
import com.fishhawk.driftinglibraryandroid.repository.local.ServerInfoDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerInfoRepository(
    private val dao: ServerInfoDao
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun observeAllServerInfo(): LiveData<List<ServerInfo>> = dao.observeAll()
    fun observeServerInfo(id: Int): LiveData<ServerInfo> = dao.observe(id)

    suspend fun selectServerInfo(id: Int): ServerInfo? =
        withContext(ioDispatcher) { dao.select(id) }

    suspend fun insertServerInfo(serverInfo: ServerInfo) =
        withContext(ioDispatcher) { dao.insert(serverInfo) }

    suspend fun updateServerInfo(serverInfo: ServerInfo) =
        withContext(ioDispatcher) { dao.update(serverInfo) }

    suspend fun deleteServerInfo(serverInfo: ServerInfo) =
        withContext(ioDispatcher) { dao.delete(serverInfo) }
}