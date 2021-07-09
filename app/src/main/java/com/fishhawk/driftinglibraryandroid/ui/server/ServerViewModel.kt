package com.fishhawk.driftinglibraryandroid.ui.server

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
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
