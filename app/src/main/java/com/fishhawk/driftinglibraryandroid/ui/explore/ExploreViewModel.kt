package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class RefreshHandle<T>(
    private val scope: CoroutineScope,
    flow: Flow<suspend () -> ResultX<T>>
) {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val channel = Channel<Unit>()

    init {
        scope.launch { channel.send(Unit) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val flow = combine(
        channel.receiveAsFlow(),
        flow.onEach { _isRefreshing.value = false })
    { _, loader -> loader }
        .flatMapLatest {
            if (_isRefreshing.value) flow {
                val result = it()
                _isRefreshing.value = false
                if (result.isSuccess) emit(result)
            } else flow {
                emit(null)
                emit(it())
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(), null)

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
    private val repository: RemoteProviderRepository
) : ViewModel() {
    val providerList = RefreshHandle(
        viewModelScope,
        repository.serviceFlow.map { suspend { repository.listProvider() } }
    )
}
