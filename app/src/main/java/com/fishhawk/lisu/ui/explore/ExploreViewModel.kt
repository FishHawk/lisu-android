package com.fishhawk.lisu.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.ui.base.ViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExploreViewModel (
    private val repository: RemoteProviderRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    private val _providers = MutableStateFlow(emptyList<ProviderDto>())

    val providers = _providers
        .map { list -> list.groupBy { it.lang } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    val lastUsedProvider =
        combine(_providers, PR.lastUsedProvider.flow) { list, name ->
            list.find { it.id == name }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        reload()
        repository.serviceFlow.onEach { reload() }.launchIn(viewModelScope)
    }

    fun reload() = viewModelScope.launch {
        _viewState.value = ViewState.Loading
        repository.listProvider()
            .onSuccess {
                _viewState.value = ViewState.Loaded
                _providers.value = it
            }.onFailure {
                _viewState.value = ViewState.Failure(it)
            }
    }
}
