package com.fishhawk.driftinglibraryandroid.ui.main.provider.category

import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.main.provider.base.ProviderBaseViewModel

class CategoryViewModel(
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
        remoteProviderRepository.getCategoryMangaList(providerId, 1, option)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.getCategoryMangaList(providerId, page + 1, option)
}