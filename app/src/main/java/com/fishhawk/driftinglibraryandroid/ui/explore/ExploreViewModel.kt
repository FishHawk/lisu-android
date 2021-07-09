package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val remoteProviderRepository: RemoteProviderRepository
) : ViewModel() {
    val providerList: MediatorLiveData<List<ProviderInfo>> = MediatorLiveData()

    init {
        viewModelScope.launch {
            remoteProviderRepository.listProvider().onSuccess {
                providerList.value = it
            }
        }
    }
}
