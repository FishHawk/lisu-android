package com.fishhawk.lisu.ui.library

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import kotlinx.coroutines.flow.*

class LibrarySearchViewModel(
    args: Bundle,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    searchHistoryRepository: SearchHistoryRepository,
) : BaseViewModel<LibraryEffect>() {

    private val _keywords = MutableStateFlow(args.getString("keywords"))
    val keywords = _keywords.asStateFlow()

    val suggestions = searchHistoryRepository.list()
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var source: LibraryMangaSource? = null

    val mangaList =
        combine(
            _keywords.filterNotNull(),
            remoteLibraryRepository.serviceFlow
        ) { keywords, _ -> keywords }.flatMapLatest {
            Pager(PagingConfig(pageSize = 20)) {
                LibraryMangaSource(it).also { source = it }
            }.flow.cachedIn(viewModelScope)
        }

    inner class LibraryMangaSource(private val keywords: String) : PagingSource<Int, MangaDto>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaDto> {
            val page = params.key ?: 0
            return remoteLibraryRepository.search(page, keywords).fold(
                { LoadResult.Page(it, null, if (it.isEmpty()) null else page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, MangaDto>): Int = 0
    }

    fun search(keywords: String) {
        _keywords.value = keywords
    }
}
