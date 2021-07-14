package com.fishhawk.driftinglibraryandroid.ui.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val serverInfoRepository: ServerInfoRepository
) : ViewModel() {
    val serverList = serverInfoRepository.list()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    fun addServer(serverInfo: ServerInfo) = viewModelScope.launch {
        val maxPosition = serverList.value.map { it.position }.maxOrNull()
        serverInfo.position = maxPosition?.plus(1) ?: 0
        serverInfoRepository.insert(serverInfo)
    }

    fun deleteServer(serverInfo: ServerInfo) = viewModelScope.launch {
        serverInfoRepository.delete(serverInfo)
        readingHistoryRepository.clear(serverInfo.id)
    }

    fun updateServer(serverInfo: ServerInfo) = viewModelScope.launch {
        serverInfoRepository.update(serverInfo)
    }
}
