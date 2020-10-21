package com.fishhawk.driftinglibraryandroid.ui.main.provider.popular

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.main.provider.base.ProviderBaseViewModel

class PopularViewModel(
    private val providerId: String,
    private val remoteProviderRepository: RemoteProviderRepository,
    remoteDownloadRepository: RemoteDownloadRepository,
    remoteSubscriptionRepository: RemoteSubscriptionRepository
) : ProviderBaseViewModel(
    providerId,
    remoteDownloadRepository,
    remoteSubscriptionRepository
) {
    override suspend fun loadResult() =
        remoteProviderRepository.getPopularMangaList(providerId, 1, option)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.getPopularMangaList(providerId, page + 1, option)
}
