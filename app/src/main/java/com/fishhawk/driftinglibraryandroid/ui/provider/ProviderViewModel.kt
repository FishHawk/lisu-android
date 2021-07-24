package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.set

typealias Option = MutableMap<String, Int>

class ProviderMangaSource(
    private val option: Option,
    private val loadFunction: suspend (key: Int, option: Option) -> ResultX<List<MangaOutline>>
) : PagingSource<Int, MangaOutline>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaOutline> {
        val page = params.key ?: 1
        return loadFunction(page, option).fold({
            LoadResult.Page(
                data = it,
                prevKey = null,
                nextKey = if (it.isEmpty()) null else page.plus(1)
            )
        }, { LoadResult.Error(it) })
    }

    override fun getRefreshKey(state: PagingState<Int, MangaOutline>): Int = 1
}

class ProviderMangaList(
    scope: CoroutineScope,
    private val loadFunction: suspend (key: Int, option: Option) -> ResultX<List<MangaOutline>>
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

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val providerBrowseHistoryRepository: ProviderBrowseHistoryRepository,
    savedStateHandle: SavedStateHandle
) : FeedbackViewModel() {

    val provider: ProviderInfo = savedStateHandle.get("provider")!!

    private val detail = flow {
        emit(remoteProviderRepository.getProvider(provider.id))
    }

    val popularOptionModel = detail.map {
        it.getOrNull()?.optionModels?.popular ?: mapOf()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mapOf())

    val latestOptionModel = detail.map {
        it.getOrNull()?.optionModels?.latest ?: mapOf()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mapOf())

    val categoryOptionModel = detail.map {
        it.getOrNull()?.optionModels?.category ?: mapOf()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mapOf())

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

    val pageHistory = providerBrowseHistoryRepository.getPageHistory(provider.id)

    fun getOptionHistory(page: Int, optionName: String) =
        providerBrowseHistoryRepository.getOptionHistory(provider.id, page, optionName)
}
