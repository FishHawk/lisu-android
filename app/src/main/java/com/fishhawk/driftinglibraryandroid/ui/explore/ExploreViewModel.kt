package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
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
