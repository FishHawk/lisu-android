package com.fishhawk.driftinglibraryandroid.ui.provider.latest

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.provider.base.MangaListViewModel

class LatestViewModel(
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
        load()
    }

    override suspend fun loadResult() =
        remoteProviderRepository.getLatestMangaList(providerId, 1, option)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.getLatestMangaList(providerId, page + 1, option)
}