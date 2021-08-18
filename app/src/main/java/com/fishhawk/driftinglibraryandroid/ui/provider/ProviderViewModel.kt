package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.datastore.OptionGroup
import com.fishhawk.driftinglibraryandroid.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProviderMangaSource(
    private val option: Map<String, Int>,
    private val loadFunction: suspend (key: Int, option: Map<String, Int>) -> ResultX<List<MangaOutline>>
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

@OptIn(ExperimentalCoroutinesApi::class)
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
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    val popularOptionModel = detail.flatMapLatest {
        it.getOrNull()?.optionModels?.popular?.let { model ->
            providerBrowseHistoryRepository.getOption(provider.id, 0, model)
        } ?: flow { emptyList<OptionGroup>() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val latestOptionModel = detail.flatMapLatest {
        it.getOrNull()?.optionModels?.latest?.let { model ->
            providerBrowseHistoryRepository.getOption(provider.id, 1, model)
        } ?: flow { emptyList<OptionGroup>() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val categoryOptionModel = detail.flatMapLatest {
        it.getOrNull()?.optionModels?.category?.let { model ->
            providerBrowseHistoryRepository.getOption(provider.id, 2, model)
        } ?: flow { emptyList<OptionGroup>() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val popularMangaList = createProviderMangaList(popularOptionModel) { page, option ->
        remoteProviderRepository.listPopularManga(provider.id, page, option)
    }
    val latestMangaList = createProviderMangaList(latestOptionModel) { page, option ->
        remoteProviderRepository.listLatestManga(provider.id, page, option)
    }
    val categoryMangaList = createProviderMangaList(categoryOptionModel) { page, option ->
        remoteProviderRepository.listCategoryManga(provider.id, page, option)
    }

    private fun createProviderMangaList(
        optionGroup: Flow<List<OptionGroup>?>,
        loadFunction: suspend (key: Int, option: Map<String, Int>) -> ResultX<List<MangaOutline>>
    ) = optionGroup.filterNotNull().flatMapLatest { groups ->
        Pager(PagingConfig(pageSize = 20)) {
            val option = groups.map { it.name to it.selected }.toMap()
            ProviderMangaSource(option, loadFunction)
        }.flow
    }.cachedIn(viewModelScope)

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

    fun updateOptionHistory(page: Int, name: String, selected: Int) =
        viewModelScope.launch {
            providerBrowseHistoryRepository.setOption(provider.id, page, name, selected)
        }
}
