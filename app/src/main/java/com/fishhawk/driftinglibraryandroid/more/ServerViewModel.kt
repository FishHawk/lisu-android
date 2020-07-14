package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.repository.data.ServerInfo
import kotlinx.coroutines.launch


class ServerViewModel(
    private val serverInfoRepository: ServerInfoRepository
) : ViewModel() {
    val serverInfoList: LiveData<List<ServerInfo>> =
        serverInfoRepository.observeAllServerInfo()

    fun addServer(serverInfo: ServerInfo) {
        viewModelScope.launch { serverInfoRepository.insertServerInfo(serverInfo) }
    }

    fun deleteServer(serverInfo: ServerInfo) {
        viewModelScope.launch { serverInfoRepository.deleteServerInfo(serverInfo) }
    }

    fun updateServer(serverInfo: ServerInfo) {
        viewModelScope.launch { serverInfoRepository.updateServerInfo(serverInfo) }
    }
}

@Suppress("UNCHECKED_CAST")
class ServerViewModelFactory(
    private val serverInfoRepository: ServerInfoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(ServerViewModel::class.java) ->
                ServerViewModel(serverInfoRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
