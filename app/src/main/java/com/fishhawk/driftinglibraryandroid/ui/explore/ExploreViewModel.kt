package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.base.remoteList

class ExploreViewModel(
    private val remoteProviderRepository: RemoteProviderRepository
) : ViewModel() {
    val providers = remoteList {
        remoteProviderRepository.getProvidersInfo()
    }.apply {
        data.addSource(GlobalPreference.selectedServer.asFlow().asLiveData()) { reload() }
    }
}
