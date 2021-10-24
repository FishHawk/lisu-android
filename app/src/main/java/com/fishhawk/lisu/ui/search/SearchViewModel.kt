package com.fishhawk.lisu.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    savedStateHandle: SavedStateHandle,
) : FeedbackViewModel() {

    val providerId: String = savedStateHandle.get("providerId")!!

    private val _keywords = MutableStateFlow(savedStateHandle.get<String>("keywords"))
    val keywords = _keywords.asStateFlow()

    init {
        keywords
            .filterNotNull()
            .onEach { searchHistoryRepository.update(SearchHistory(providerId, it)) }
            .launchIn(viewModelScope)
    }

    val suggestions = searchHistoryRepository.listByProvider(providerId)
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var source: ProviderSearchMangaSource? = null

    val mangaList = keywords.filterNotNull().flatMapLatest { keywords ->
        Pager(PagingConfig(pageSize = 20)) {
            ProviderSearchMangaSource(keywords).also { source = it }
        }.flow
    }.cachedIn(viewModelScope)

    init {
        listOf(keywords, remoteProviderRepository.serviceFlow).forEach {
            it.onEach { source?.invalidate() }.launchIn(viewModelScope)
        }
    }

    fun search(keywords: String) {
        _keywords.value = keywords
    }

    fun deleteSuggestion(keywords: String) = viewModelScope.launch {
        searchHistoryRepository.deleteByKeywords(providerId, keywords)
    }

//    fun addToLibrary(sourceMangaId: String, targetMangaId: String) = viewModelScope.launch {
//        val result = remoteLibraryRepository.createManga(
//            targetMangaId,
//            provider.id,
//            sourceMangaId,
//            false
//        )
//        resultWarp(result) { feed(R.string.successfully_add_to_library) }
//    }

    inner class ProviderSearchMangaSource(
        private val keywords: String
    ) : PagingSource<Int, MangaDto>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaDto> {
            val page = params.key ?: 0
            return remoteProviderRepository.search(providerId, page, keywords).fold(
                { LoadResult.Page(it, null, page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, MangaDto>): Int = 0
    }
}
