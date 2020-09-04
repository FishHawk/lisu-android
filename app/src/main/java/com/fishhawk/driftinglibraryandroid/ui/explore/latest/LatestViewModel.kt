package com.fishhawk.driftinglibraryandroid.ui.explore.latest

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListFromProviderViewModel

class LatestViewModel(
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
        remoteProviderRepository.getLatestMangaList(providerId, 1)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.getLatestMangaList(providerId, page + 1)
}