package com.fishhawk.driftinglibraryandroid.ui.main.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.ui.base.OperationViewModel

class ProviderViewModel(
    private val providerId: String,
    private val remoteProviderRepository: RemoteProviderRepository
) : OperationViewModel() {
    val detail: LiveData<Result<ProviderDetail>> = liveData {
        emit(Result.Loading)
        emit(remoteProviderRepository.getProvidersDetail(providerId))
    }
}
