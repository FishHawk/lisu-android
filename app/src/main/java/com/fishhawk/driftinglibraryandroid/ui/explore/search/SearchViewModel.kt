package com.fishhawk.driftinglibraryandroid.ui.explore.search

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListFromProviderViewModel

class SearchViewModel(
    private val providerId: String,
    private val remoteProviderRepository: RemoteProviderRepository,
    remoteDownloadRepository: RemoteDownloadRepository,
    remoteSubscriptionRepository: RemoteSubscriptionRepository
) : MangaListFromProviderViewModel(
    providerId,
    remoteDownloadRepository,
    remoteSubscriptionRepository
) {
    private var keywords = ""

    override suspend fun loadResult() =
        remoteProviderRepository.search(providerId, keywords, 1)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.search(providerId, keywords, page + 1)

    fun search(keywords: String) {
        this.keywords = keywords
        load()
    }
}