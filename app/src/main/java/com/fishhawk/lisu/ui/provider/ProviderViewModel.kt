package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.datastore.BoardFilter
import com.fishhawk.lisu.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.base.PagedList
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.util.flatten
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Board(
    val filters: List<BoardFilter>,
    val mangaResult: Result<PagedList<MangaDto>>?,
)

class ProviderViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val providerBrowseHistoryRepository: ProviderBrowseHistoryRepository,
) : ViewModel() {
    val providerId = args.getString("providerId")!!
    val boardId = args.getString("boardId")!!

    private val boardModel =
        lisuRepository.providers
            .filterNotNull()
            .mapNotNull {
                it.value?.map { providers ->
                    providers.find { provider -> provider.id == providerId }
                        ?.boardModels?.get(boardId)
                        ?: return@mapNotNull null
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _board = boardModel
        .filterNotNull()
        .flatMapLatest { result ->
            flatten(
                result.map { boardModel ->
                    val filters = providerBrowseHistoryRepository
                        .getFilters(providerId, boardId, boardModel)
                    val remoteList = filters.flatMapLatest {
                        val options = it.associate { it.name to it.selected }
                        lisuRepository.getBoard(providerId, boardId, options)
                    }
                    combine(filters, remoteList) { f, r -> f to r }
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val board = _board
        .filterNotNull()
        .map { result ->
            result.map { (filters, remoteList) ->
                Board(filters, remoteList.value)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun addToLibrary(manga: MangaDto) {
        viewModelScope.launch {
            lisuRepository.addMangaToLibrary(manga.providerId, manga.id).fold({}, {})
        }
    }

    fun removeFromLibrary(manga: MangaDto) {
        viewModelScope.launch {
            lisuRepository.removeMangaFromLibrary(manga.providerId, manga.id).fold({}, {})
        }
    }

    fun updateFilterHistory(name: String, selected: Int) {
        viewModelScope.launch {
            providerBrowseHistoryRepository.setFilter(providerId, boardId, name, selected)
        }
    }

    fun reloadProvider() {
        viewModelScope.launch {
            lisuRepository.providers.value?.reload()
        }
    }

    fun reload() {
        viewModelScope.launch {
            _board.value?.getOrNull()?.second?.reload()
        }
    }

    fun requestNextPage() {
        viewModelScope.launch {
            _board.value?.getOrNull()?.second?.requestNextPage()
        }
    }
}
