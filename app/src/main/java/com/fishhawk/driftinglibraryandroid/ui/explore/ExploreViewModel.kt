package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class RefreshHandle(private val scope: CoroutineScope) {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val channel = Channel<Unit>()

    init {
        scope.launch { channel.send(Unit) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> attach(flow: Flow<suspend () -> Result<T>>): Flow<Result<T>?> {
        return combine(
            channel.receiveAsFlow(),
            flow.onEach { _isRefreshing.value = false }
        ) { type, loader -> Pair(type, loader) }
            .flatMapLatest {
                if (_isRefreshing.value) flow {
                    val result = it.second()
                    _isRefreshing.value = false
                    if (result.isSuccess) emit(result)
                } else flow {
                    emit(null)
                    emit(it.second())
                }
            }
    }

    fun refresh() = scope.launch {
        if (_isRefreshing.value) return@launch
        _isRefreshing.value = true
        channel.send(Unit)
    }

    fun reload() = scope.launch {
        _isRefreshing.value = false
        channel.send(Unit)
    }
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val remoteProviderRepository: RemoteProviderRepository
) : ViewModel() {
    val handler = RefreshHandle(viewModelScope)

    val providerList = handler.attach(
        P.selectedServer.asFlow().map { suspend { remoteProviderRepository.listProvider() } }
    ).stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
