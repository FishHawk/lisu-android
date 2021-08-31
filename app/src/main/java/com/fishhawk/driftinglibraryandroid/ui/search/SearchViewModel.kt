package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    savedStateHandle: SavedStateHandle,
) : FeedbackViewModel() {

    val provider: ProviderInfo = savedStateHandle.get("provider")!!

    private val _keywords = MutableStateFlow(savedStateHandle.get<String>("keywords"))
    val keywords = _keywords.asStateFlow()

    private var source: ProviderSearchMangaSource? = null

    val mangaList = keywords.filterNotNull().flatMapLatest { keywords ->
        Pager(PagingConfig(pageSize = 20)) {
            ProviderSearchMangaSource(keywords).also { source = it }
        }.flow
    }.cachedIn(viewModelScope)

    init {
        listOf(
            keywords,
            remoteProviderRepository.serviceFlow
        ).forEach { it.onEach { source?.invalidate() }.launchIn(viewModelScope) }
    }

    fun search(keywords: String) {
        _keywords.value = keywords
    }

    fun addToLibrary(sourceMangaId: String, targetMangaId: String) = viewModelScope.launch {
        val result = remoteLibraryRepository.createManga(
            targetMangaId,
            provider.id,
            sourceMangaId,
            false
        )
        resultWarp(result) { feed(R.string.successfully_add_to_library) }
    }

    inner class ProviderSearchMangaSource(
        private val keywords: String
    ) : PagingSource<Int, MangaOutline>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaOutline> {
            val page = params.key ?: 1
            return remoteProviderRepository.listManga(
                provider.id,
                keywords,
                page
            ).fold({
                LoadResult.Page(
                    data = it,
                    prevKey = null,
                    nextKey = page.plus(1)
                )
            }, { LoadResult.Error(it) })
        }

        override fun getRefreshKey(state: PagingState<Int, MangaOutline>): Int = 0
    }
}
