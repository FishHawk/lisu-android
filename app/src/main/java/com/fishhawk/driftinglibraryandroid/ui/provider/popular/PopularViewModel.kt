package com.fishhawk.driftinglibraryandroid.ui.provider.popular

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.provider.base.MangaListViewModel

class PopularViewModel(
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
        remoteProviderRepository.getPopularMangaList(providerId, 1, option)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.getPopularMangaList(providerId, page + 1, option)
}
