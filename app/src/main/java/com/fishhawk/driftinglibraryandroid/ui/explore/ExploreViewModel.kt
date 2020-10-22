package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class ExploreViewModel(
    private val remoteProviderRepository: RemoteProviderRepository
) : ViewModel() {
    val providerList: LiveData<Result<List<ProviderInfo>>> =
        SettingsHelper.selectedServer.switchMap {
            liveData {
                emit(Result.Loading)
                emit(remoteProviderRepository.getProvidersInfo())
            }
        }
}
