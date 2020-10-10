package com.fishhawk.driftinglibraryandroid.ui.main.globalsearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo

class GlobalSearchViewModel(
    private val remoteLibraryRepository: RemoteProviderRepository
) : ViewModel() {
    val keywords: MutableLiveData<String> = MutableLiveData("")

    val providerList: LiveData<Result<List<ProviderInfo>>> = liveData {
        emit(Result.Loading)
        emit(remoteLibraryRepository.getProvidersInfo())
    }

    suspend fun search(providerId: String, keywords: String): Result<List<MangaOutline>> =
        remoteLibraryRepository.search(providerId, keywords, 1)
}