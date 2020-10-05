package com.fishhawk.driftinglibraryandroid.ui.main.server

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import kotlinx.coroutines.launch

class ServerViewModel(
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val serverInfoRepository: ServerInfoRepository
) : ViewModel() {
    val serverInfoList: LiveData<List<ServerInfo>> =
        serverInfoRepository.observeAllServerInfo()

    fun addServer(serverInfo: ServerInfo) = viewModelScope.launch {
        serverInfoRepository.insertServerInfo(serverInfo)
    }

    fun deleteServer(serverInfo: ServerInfo) = viewModelScope.launch {
        serverInfoRepository.deleteServerInfo(serverInfo)
        readingHistoryRepository.clearReadingHistoryOfServer(serverInfo.id)
    }

    fun updateServer(serverInfo: ServerInfo) = viewModelScope.launch {
        serverInfoRepository.updateServerInfo(serverInfo)
    }
}
