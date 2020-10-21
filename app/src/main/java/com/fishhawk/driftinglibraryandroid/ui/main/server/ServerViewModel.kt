package com.fishhawk.driftinglibraryandroid.ui.main.server

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import kotlinx.coroutines.launch

class ServerViewModel(
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val serverInfoRepository: ServerInfoRepository
) : ViewModel() {
    val serverInfoList: MutableLiveData<List<ServerInfo>> = MutableLiveData()

    init {
        loadServerList()
    }

    private fun loadServerList() = viewModelScope.launch {
        serverInfoList.value = serverInfoRepository.listAllServerInfo()
    }

    fun saveServerListOrder() = viewModelScope.launch {
        serverInfoList.value?.forEachIndexed { position, serverInfo ->
            serverInfo.position = position
            serverInfoRepository.updateServerInfo(serverInfo)
        }
        loadServerList()
    }

    fun addServer(serverInfo: ServerInfo) = viewModelScope.launch {
        val maxPosition = serverInfoList.value?.map { it.position }?.maxOrNull()
        serverInfo.position = maxPosition?.plus(1) ?: 0
        serverInfoRepository.insertServerInfo(serverInfo)
        loadServerList()
    }

    fun deleteServer(serverInfo: ServerInfo) = viewModelScope.launch {
        serverInfoRepository.deleteServerInfo(serverInfo)
        loadServerList()
        readingHistoryRepository.clearReadingHistoryOfServer(serverInfo.id)
    }

    fun updateServer(serverInfo: ServerInfo) = viewModelScope.launch {
        serverInfoRepository.updateServerInfo(serverInfo)
        loadServerList()
    }
}
