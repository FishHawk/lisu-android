package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.RemotePagingList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias Option = MutableMap<String, Int>

class ProviderMangaListComponent(
    scope: CoroutineScope,
    private val loadFunction: suspend (key: Int, option: Option) -> Result<List<MangaOutline>>
) : RemotePagingList<Int, MangaOutline>(scope) {

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
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    argProviderId: String
) : FeedbackViewModel() {

    val providerId: MutableLiveData<String> = MutableLiveData(argProviderId)

    val detail: LiveData<Result<ProviderDetail>?> = providerId.switchMap {
        liveData {
            emit(null)
            emit(remoteProviderRepository.getProvider(it))
        }
    }

    val popularMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.listPopularManga(providerId.value!!, page, option)
    }

    val latestMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.listLatestManga(providerId.value!!, page, option)
    }

    val categoryMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.listCategoryManga(providerId.value!!, page, option)
    }

    fun download(sourceMangaId: String, targetMangaId: String) = viewModelScope.launch {
        val result = remoteLibraryRepository.createManga(
            targetMangaId,
            providerId.value!!,
            sourceMangaId,
            true
        )
        resultWarp(result) { feed(R.string.download_task_created) }
    }

    fun subscribe(sourceMangaId: String, targetMangaId: String) = viewModelScope.launch {
        val result = remoteLibraryRepository.createManga(
            targetMangaId,
            providerId.value!!,
            sourceMangaId,
            false
        )
        resultWarp(result) { feed(R.string.subscription_created) }
    }
}
