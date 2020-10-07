package com.fishhawk.driftinglibraryandroid.ui.provider.category

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.provider.base.MangaListViewModel

class CategoryViewModel(
    private val providerId: String,
    private val remoteProviderRepository: RemoteProviderRepository,
    remoteDownloadRepository: RemoteDownloadRepository,
    remoteSubscriptionRepository: RemoteSubscriptionRepository
) : MangaListViewModel(
    providerId,
    remoteDownloadRepository,
    remoteSubscriptionRepository
) {
    private val option: MutableMap<String, Int> = mutableMapOf()

    fun selectOption(optionType: String, optionIndex: Int) {
        option[optionType] = optionIndex
    }

    override suspend fun loadResult() =
        remoteProviderRepository.getCategoryMangaList(providerId, 1, option)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.getCategoryMangaList(providerId, page + 1, option)
}