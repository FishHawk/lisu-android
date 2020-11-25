package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference

class ExploreViewModel(
    private val remoteProviderRepository: RemoteProviderRepository
) : ViewModel() {
    val providerList: LiveData<Result<List<ProviderInfo>>?> =
        GlobalPreference.selectedServer.asFlow().asLiveData().switchMap {
            liveData {
                emit(null)
                emit(remoteProviderRepository.getProvidersInfo())
            }
        }
}
