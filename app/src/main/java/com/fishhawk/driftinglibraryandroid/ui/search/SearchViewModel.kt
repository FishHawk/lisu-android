package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModelWithFetchMore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchViewModel(
    private val providerId: String,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository
) : RefreshableListViewModelWithFetchMore<MangaOutline>() {
    private var page = 1

    private var keywords = ""

    override suspend fun loadResult() =
        remoteProviderRepository.search(providerId, keywords, 1)

    override suspend fun fetchMoreResult() =
        remoteProviderRepository.search(providerId, keywords, page + 1)

    fun search(keywords: String) {
        this.keywords = keywords
        load()
    }

    override fun onRefreshSuccess(data: List<MangaOutline>) {
        page = 1
    }

    override fun onFetchMoreSuccess(data: List<MangaOutline>) {
        if (data.isNotEmpty()) page += 1
    }

    fun download(id: String, title: String) =
        viewModelScope.launch(Dispatchers.Main) {
            val result = remoteDownloadRepository.postDownloadTask(providerId, id, title)
            resultWarp(result) { feed(R.string.download_task_created) }
        }

    fun subscribe(id: String, title: String) =
        viewModelScope.launch(Dispatchers.Main) {
            val result = remoteSubscriptionRepository.postSubscription(providerId, id, title)
            resultWarp(result) { feed(R.string.subscription_created) }
        }
}
