package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.Page
import com.fishhawk.driftinglibraryandroid.ui.base.PagingList
import kotlinx.coroutines.launch

class SearchViewModel(
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository,
    private val providerId: String,
    argKeywords: String
) : FeedbackViewModel() {
    val keywords = MutableLiveData(argKeywords)

    val mangaList = object : PagingList<Int, MangaOutline>(viewModelScope) {
        override suspend fun loadPage(key: Int?): Result<Page<Int, MangaOutline>> {
            val page = key ?: 1
            return remoteProviderRepository.search(
                providerId = providerId,
                keywords = keywords.value!!,
                page = page
            ).map { Page(data = it, nextPage = page + 1) }
        }
    }

    init {
        mangaList.list.addSource(keywords) { mangaList.load() }
    }

    fun download(id: String, title: String) = viewModelScope.launch {
        val result = remoteDownloadRepository.postDownloadTask(providerId, id, title)
        resultWarp(result) { feed(R.string.download_task_created) }
    }

    fun subscribe(id: String, title: String) = viewModelScope.launch {
        val result = remoteSubscriptionRepository.postSubscription(providerId, id, title)
        resultWarp(result) { feed(R.string.subscription_created) }
    }
}
