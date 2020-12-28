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
import com.fishhawk.driftinglibraryandroid.ui.base.PagingList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias Option = MutableMap<String, Int>

class ProviderMangaListComponent(
    scope: CoroutineScope,
    private val loadFunction: suspend (key: Int, option: Option) -> Result<List<MangaOutline>>
) : PagingList<Int, MangaOutline>(scope) {

    private var option: Option = mutableMapOf()

    fun selectOption(name: String, index: Int) {
        if (option[name] != index) {
            option[name] = index
            reload()
        }
    }

    fun selectOption(option: Option) {
        if (option.keys.size != this.option.keys.size ||
            option.filterKeys { option[it] != this.option[it] }.isNotEmpty()
        ) {
            this.option = option
            reload()
        }
    }

    override suspend fun load(key: Int?): Result<Pair<Int?, List<MangaOutline>>> {
        val page = key ?: 1
        return loadFunction(key ?: 1, option).map { Pair(page + 1, it) }
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
