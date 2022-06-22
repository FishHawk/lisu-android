package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.datastore.BoardFilter
import com.fishhawk.lisu.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.lisu.data.remote.LisuRepository
import com.fishhawk.lisu.data.remote.util.PagedList
import com.fishhawk.lisu.data.remote.model.MangaDto
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
    private val providerBrowseHistoryRepository: ProviderBrowseHistoryRepository
) : ViewModel() {
    val providerId = args.getString("providerId")!!

    val provider =
        lisuRepository.providers
            .mapNotNull {
                it?.value
                    ?.getOrNull()
                    ?.find { provider -> provider.id == providerId }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _boards = provider
        .filterNotNull()
        .flatMapLatest { provider ->
            flatten(
                provider.boardModels.mapValues { (boardId, model) ->
                    val filters =
                        providerBrowseHistoryRepository.getFilters(provider.id, boardId, model)

                    val remoteList = filters.flatMapLatest {
                        val options = it.associate { it.name to it.selected }
                        lisuRepository.getBoard(providerId, boardId, options)
                    }

                    combine(filters, remoteList) { f, r -> f to r }
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    val boards = _boards
        .map {
            it.mapValues { (_, pair) ->
                val (filters, remoteList) = pair
                Board(filters, remoteList.value)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    val pageHistory = providerBrowseHistoryRepository.getBoardHistory(providerId)

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

    fun updateFilterHistory(boardId: String, name: String, selected: Int) {
        viewModelScope.launch {
            providerBrowseHistoryRepository.setFilter(providerId, boardId, name, selected)
        }
    }

    fun reload(boardId: String) {
        viewModelScope.launch {
            _boards.value[boardId]?.second?.reload()
        }
    }

    fun requestNextPage(boardId: String) {
        viewModelScope.launch {
            _boards.value[boardId]?.second?.requestNextPage()
        }
    }
}
