package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class ExploreViewModel(
    private val remoteLibraryRepository: RemoteProviderRepository
) : ViewModel() {
    val providerList: LiveData<Result<List<ProviderInfo>>> =
        SettingsHelper.selectedServer.switchMap {
            liveData {
                emit(Result.Loading)
                emit(remoteLibraryRepository.getProvidersInfo())
            }
        }
}
