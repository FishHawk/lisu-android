package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
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
    val provider: ProviderInfo
) : FeedbackViewModel() {

    val detail: LiveData<Result<ProviderDetail>?> = liveData {
        emit(null)
        emit(remoteProviderRepository.getProvider(provider.id))
    }

    val popularMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.listPopularManga(provider.id, page, option)
    }

    val latestMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.listLatestManga(provider.id, page, option)
    }

    val categoryMangaList = ProviderMangaListComponent(viewModelScope) { page, option ->
        remoteProviderRepository.listCategoryManga(provider.id, page, option)
    }

    fun addToLibrary(sourceMangaId: String, targetMangaId: String) = viewModelScope.launch {
        val result = remoteLibraryRepository.createManga(
            targetMangaId,
            provider.id,
            sourceMangaId,
            true
        )
        resultWarp(result) { feed(R.string.successfully_add_to_library) }
    }
}
