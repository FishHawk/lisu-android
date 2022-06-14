package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.lisu.data.remote.LisuRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProviderViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val providerBrowseHistoryRepository: ProviderBrowseHistoryRepository
) : ViewModel() {

    val provider = lisuRepository.getProvider(args.getString("providerId")!!)

    val boards = provider.boardModels.keys.toList()

    val boardFilters = provider.boardModels.mapValues { (boardId, model) ->
        providerBrowseHistoryRepository.getFilters(provider.id, boardId, model)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    }

    val boardMangaLists = boardFilters.mapValues { (boardId, filters) ->
        filters.filterNotNull().flatMapLatest { groups ->
            Pager(PagingConfig(pageSize = 20)) {
                val option = groups.associate { it.name to it.selected }
                ProviderMangaSource(boardId, option)
            }.flow
        }.cachedIn(viewModelScope)
    }

    fun addToLibrary(manga: MangaDto) = viewModelScope.launch {
        lisuRepository.addMangaToLibrary(manga.providerId, manga.id).fold({}, {})
    }

    fun removeFromLibrary(manga: MangaDto) = viewModelScope.launch {
        lisuRepository.removeMangaFromLibrary(manga.providerId, manga.id).fold({}, {})
    }

    val pageHistory = providerBrowseHistoryRepository.getBoardHistory(provider.id)

    fun updateFilterHistory(boardId: String, name: String, selected: Int) =
        viewModelScope.launch {
            providerBrowseHistoryRepository.setFilter(provider.id, boardId, name, selected)
        }

    inner class ProviderMangaSource(
        private val boardId: String,
        private val filters: Map<String, Int>
    ) : PagingSource<Int, MangaDto>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaDto> {
            val page = params.key ?: 0
            return lisuRepository.getBoard(provider.id, boardId, page, filters).fold(
                { LoadResult.Page(it, null, if (it.isEmpty()) null else page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, MangaDto>): Int = 0
    }
}
