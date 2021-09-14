package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: RemoteProviderRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    private val _providerList = MutableStateFlow(
        emptyList<Provider>()
    )
    val providerList = _providerList.asStateFlow()

    init {
        reload()
        repository.serviceFlow.onEach { reload() }.launchIn(viewModelScope)
    }

    fun reload() = viewModelScope.launch {
        _viewState.value = ViewState.Loading
        repository.listProvider()
            .onSuccess {
                _viewState.value = ViewState.Loaded
                _providerList.value = it
            }.onFailure {
                _viewState.value = ViewState.Failure(it)
            }
    }
}
