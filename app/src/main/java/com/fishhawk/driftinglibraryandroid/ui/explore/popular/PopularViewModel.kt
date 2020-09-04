package com.fishhawk.driftinglibraryandroid.ui.explore.popular

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListFromProviderViewModel

class PopularViewModel(
    private val providerId: String,
    private val remoteProviderRepository: RemoteProviderRepository,
    remoteDownloadRepository: RemoteDownloadRepository,
    remoteSubscriptionRepository: RemoteSubscriptionRepository
) : MangaListFromProviderViewModel(
    providerId,
    remoteDownloadRepository,
    remoteSubscriptionRepository
) {
    override suspend fun loadResult() =
        remoteProviderRepository.getPopularMangaList(providerId, 1)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.getPopularMangaList(providerId, page + 1)
}
