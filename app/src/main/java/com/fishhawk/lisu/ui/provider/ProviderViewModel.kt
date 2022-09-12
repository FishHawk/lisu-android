package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.base.PagedList
import com.fishhawk.lisu.data.network.model.BoardFilterValue
import com.fishhawk.lisu.data.network.model.BoardId
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import com.fishhawk.lisu.util.flatCombine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ProviderEvent : Event {
    data class RemoveFromLibraryFailure(val exception: Throwable) : ProviderEvent
    data class AddToLibraryFailure(val exception: Throwable) : ProviderEvent
    data class RefreshFailure(val exception: Throwable) : ProviderEvent
}

data class Board(
    val filterValues: BoardFilterValue,
    val mangaResult: Result<PagedList<MangaDto>>?,
)

class ProviderViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val providerBrowseHistoryRepository: ProviderBrowseHistoryRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : BaseViewModel<ProviderEvent>() {
    val providerId = args.getString("providerId")!!
    val boardId = BoardId.valueOf(args.getString("boardId")!!)

    private val boardModel = lisuRepository
        .providers.value!!.value!!.getOrThrow()
        .find { provider -> provider.id == providerId }!!
        .boardModels[boardId]!!

    val hasAdvanceFilters = boardModel.advance.isNotEmpty()
    val hasSearchBar = boardModel.hasSearchBar

    private val _keywords = MutableStateFlow(args.getString("keywords") ?: "")
    val keywords = _keywords.asStateFlow()

    val suggestions = searchHistoryRepository.listByProvider(providerId)
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val filterValues =
        providerBrowseHistoryRepository
            .getBoardFilterValue(providerId, boardId, boardModel)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val remoteMangaList = flatCombine(
        keywords,
        filterValues.filterNotNull(),
    ) { keywords, filterValues ->
        lisuRepository.getBoard(providerId, boardId, filterValues, keywords)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val board = combine(
        filterValues.filterNotNull(),
        remoteMangaList,
    ) { filterValues, remoteMangaList ->
        Board(filterValues, remoteMangaList?.value)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun search(keywords: String) {
        _keywords.value = keywords
    }

    fun deleteSuggestion(keywords: String) = viewModelScope.launch {
        searchHistoryRepository.deleteByKeywords(providerId, keywords)
    }

    fun addToLibrary(manga: MangaDto) {
        viewModelScope.launch {
            lisuRepository.addMangaToLibrary(manga.providerId, manga.id)
                .onFailure { sendEvent(ProviderEvent.AddToLibraryFailure(it)) }
        }
    }

    fun removeFromLibrary(manga: MangaDto) {
        viewModelScope.launch {
            lisuRepository.removeMangaFromLibrary(manga.providerId, manga.id)
                .onFailure { sendEvent(ProviderEvent.RemoveFromLibraryFailure(it)) }
        }
    }

    fun updateFilterHistory(name: String, value: Any) {
        viewModelScope.launch {
            providerBrowseHistoryRepository.setFilterValue(providerId, boardId, name, value)
        }
    }

    fun updateFilterHistory(values: Map<String, Any>) {
        viewModelScope.launch {
            values.map { (name, value) ->
                providerBrowseHistoryRepository.setFilterValue(providerId, boardId, name, value)
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            remoteMangaList.value?.reload()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            remoteMangaList.value?.refresh()
                ?.onFailure { sendEvent(ProviderEvent.RefreshFailure(it)) }
            _isRefreshing.value = false
        }
    }

    fun requestNextPage() {
        viewModelScope.launch {
            remoteMangaList.value?.requestNextPage()
        }
    }
}
