package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.Page
import com.fishhawk.driftinglibraryandroid.ui.base.PagingList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias Option = MutableMap<String, Int>

class ProviderMangaListComponent(
    scope: CoroutineScope,
    private val loadPage: suspend (page: Int, option: Option) -> Result<List<MangaOutline>>
) : PagingList<Int, MangaOutline>(scope) {
    private var option: Option = mutableMapOf()

    fun selectOption(name: String, index: Int) {
        if (option[name] != index) {
            option[name] = index
            load()
        }
    }

    fun selectOption(option: Option) {
        if (option.keys.size != this.option.keys.size ||
            option.filterKeys { option[it] != this.option[it] }.isNotEmpty()
        ) {
            this.option = option
            load()
        }
    }

    override suspend fun loadPage(key: Int?): Result<Page<Int, MangaOutline>> {
        val page = key ?: 1
        return loadPage(key ?: 1, option)
            .map { Page(data = it, nextPage = page + 1) }
    }
}

class ProviderViewModel(
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository,
    argProviderId: String
) : FeedbackViewModel() {

    val providerId: MutableLiveData<String> = MutableLiveData(argProviderId)

    val detail: LiveData<Result<ProviderDetail>?> = providerId.switchMap {
        liveData {
            emit(null)
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
