package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.ui.base.NotificationViewModel

class ProviderViewModel(
    private val providerId: String,
    private val remoteProviderRepository: RemoteProviderRepository
) : NotificationViewModel() {
    val detail: LiveData<Result<ProviderDetail>> = liveData<Result<ProviderDetail>> {
        emit(Result.Loading)
        emit(remoteProviderRepository.getProvidersDetail(providerId))
    }
}
