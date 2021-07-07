package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.filterKeys
import kotlin.collections.isNotEmpty
import kotlin.collections.mutableMapOf
import kotlin.collections.set

typealias Option = MutableMap<String, Int>

class ProviderMangaSource(
    private val option: Option,
    private val loadFunction: suspend (key: Int, option: Option) -> Result<List<MangaOutline>>
) : PagingSource<Int, MangaOutline>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaOutline> {
        val page = params.key ?: 1
        return when (val result = loadFunction(page, option)) {
            is Result.Success -> LoadResult.Page(
                data = result.data,
                prevKey = null,
                nextKey = page.plus(1)
            )
            is Result.Error -> LoadResult.Error(result.exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MangaOutline>): Int = 0
}

class ProviderMangaList(
    scope: CoroutineScope,
    private val loadFunction: suspend (key: Int, option: Option) -> Result<List<MangaOutline>>
) {
    private var option: Option = mutableMapOf()
    private var source: ProviderMangaSource? = null

    val list = Pager(PagingConfig(pageSize = 20)) {
        ProviderMangaSource(option, loadFunction).also { source = it }
    }.flow.cachedIn(scope)

    fun selectOption(name: String, index: Int) {
        if (option[name] != index) {
            option[name] = index
            source?.invalidate()
        }
    }

    fun selectOption(option: Option) {
        if (option.keys.size != this.option.keys.size ||
            option.filterKeys { option[it] != this.option[it] }.isNotEmpty()
        ) {
            this.option = option
            source?.invalidate()
        }
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

    val popularOptionModel = detail.map {
        if (it is Result.Success) it.data.optionModels.popular else mapOf()
    }
    val latestOptionModel = detail.map {
        if (it is Result.Success) it.data.optionModels.latest else mapOf()
    }
    val categoryOptionModel = detail.map {
        if (it is Result.Success) it.data.optionModels.category else mapOf()
    }

    val popularMangaList = ProviderMangaList(viewModelScope) { page, option ->
        remoteProviderRepository.listPopularManga(provider.id, page, option)
    }
    val latestMangaList = ProviderMangaList(viewModelScope) { page, option ->
        remoteProviderRepository.listLatestManga(provider.id, page, option)
    }
    val categoryMangaList = ProviderMangaList(viewModelScope) { page, option ->
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
