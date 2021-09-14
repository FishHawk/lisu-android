package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: RemoteProviderRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    private val providerList = MutableStateFlow(emptyList<Provider>())

    val providerMap = providerList
        .map { list -> list.groupBy { it.lang } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    val lastUsedProvider =
        combine(providerList, PR.lastUsedProvider.flow) { list, name ->
            list.find { it.name == name }
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
                providerList.value = it
            }.onFailure {
                _viewState.value = ViewState.Failure(it)
            }
    }
}
