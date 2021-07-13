package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val remoteProviderRepository: RemoteProviderRepository
) : ViewModel() {
    val providerList = P.selectedServer.asFlow().flatMapLatest {
        flowOf(null, remoteProviderRepository.listProvider())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}
