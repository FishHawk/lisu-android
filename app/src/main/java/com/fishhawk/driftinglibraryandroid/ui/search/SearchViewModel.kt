package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.remotePagingList
import kotlinx.coroutines.launch

class SearchViewModel(
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository,
    private val providerId: String,
    argKeywords: String
) : FeedbackViewModel() {

    val keywords = MutableLiveData(argKeywords)

    val outlines = remotePagingList<Int, MangaOutline> { key ->
        val page = key ?: 1
        remoteProviderRepository.search(
            providerId = providerId,
            keywords = keywords.value!!,
            page = page
        ).map { Pair(page + 1, it) }
    }.apply {
        data.addSource(keywords) { reload() }
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
