package com.fishhawk.driftinglibraryandroid.ui.server

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.repository.data.ServerInfo
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
