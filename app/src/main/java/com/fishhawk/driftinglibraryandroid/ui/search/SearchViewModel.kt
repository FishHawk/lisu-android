package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderMangaListComponent
import kotlinx.coroutines.launch

class SearchViewModel(
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository
) : FeedbackViewModel() {
    private val providerId: MutableLiveData<String> = MutableLiveData()
    var keywords = ""

    val mangaList = ProviderMangaListComponent(viewModelScope) { page, _ ->
        remoteProviderRepository.search(providerId.value!!, keywords, page)
    }

    fun setProviderId(id: String) {
        providerId.value = id
        mangaList.reset()
    }

    fun search(keywords: String) {
        this.keywords = keywords
        mangaList.load()
    }

    fun download(id: String, title: String) = viewModelScope.launch {
        val result = remoteDownloadRepository.postDownloadTask(providerId.value!!, id, title)
        resultWarp(result) { feed(R.string.download_task_created) }
    }

    fun subscribe(id: String, title: String) = viewModelScope.launch {
        val result = remoteSubscriptionRepository.postSubscription(providerId.value!!, id, title)
        resultWarp(result) { feed(R.string.subscription_created) }
    }
}
