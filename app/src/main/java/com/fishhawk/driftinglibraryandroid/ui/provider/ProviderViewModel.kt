package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListComponentWithFetchMore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias Option = MutableMap<String, Int>

class ProviderMangaListComponent(
    scope: CoroutineScope,
    private val loadPage: suspend (page: Int, option: Option) -> Result<List<MangaOutline>>
) : RefreshableListComponentWithFetchMore<MangaOutline>(scope) {
    private var page = 1
    private var option: Option = mutableMapOf()

    fun selectOption(name: String, index: Int) {
        option[name] = index
        load()
    }

    override fun reset() {
        super.reset()
        page = 1
        option.clear()
    }

    override suspend fun loadInternal(): Result<List<MangaOutline>> =
        loadPage(1, option)

    override suspend fun fetchMoreInternal(): Result<List<MangaOutline>> =
        loadPage(page + 1, option)

    override fun onRefreshFinish(result: Result<List<MangaOutline>>) {
        result.onSuccess { page = 1 }
    }

    override fun onFetchMoreFinish(result: Result<List<MangaOutline>>) {
        result.onSuccess { if (it.isNotEmpty()) page += 1 }
    }
}

class ProviderViewModel(
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository
) : FeedbackViewModel() {
    private val providerId: MutableLiveData<String> = MutableLiveData()

    fun getProviderId(): String = providerId.value.orEmpty()

    fun setProviderId(id: String) {
        providerId.value = id
        popularMangaList.reset()
        latestMangaList.reset()
        categoryMangaList.reset()
    }

    val detail: LiveData<Result<ProviderDetail>> = switchMap(providerId) {
        liveData {
            emit(Result.Loading)
            emit(remoteProviderRepository.getProvidersDetail(it))
        }
    }

    val popularMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.getPopularMangaList(providerId.value!!, page, option)
    }

    val latestMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.getLatestMangaList(providerId.value!!, page, option)
    }

    val categoryMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.getCategoryMangaList(providerId.value!!, page, option)
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
