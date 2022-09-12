package com.fishhawk.lisu.ui.globalsearch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.base.RemoteList
import com.fishhawk.lisu.data.network.model.BoardFilterValue
import com.fishhawk.lisu.data.network.model.BoardId
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.ProviderDto
import com.fishhawk.lisu.util.flatCombine
import com.fishhawk.lisu.util.flatten
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchRecord(
    val searchBoardId: BoardId,
    val provider: ProviderDto,
    val remoteList: RemoteList<MangaDto>,
)

class GlobalSearchViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {

    private val _keywords = MutableStateFlow(args.getString("keywords") ?: "")
    val keywords = _keywords.asStateFlow()

    private val providers =
        lisuRepository.providers
            .map { remoteProvider ->
                remoteProvider?.value?.map { providers ->
                    providers.mapNotNull { provider ->
                        provider.searchBoardId?.let { it to provider }
                    }
                }
            }

    val searchRecordsResult =
        flatCombine(
            keywords
                .filter { it.isNotBlank() }
                .onEach { searchHistoryRepository.update(SearchHistory("", it)) },
            providers,
        ) { keywords, providersResult ->
            flatten(
                providersResult?.map { providers ->
                    flatten(
                        providers.map { (boardId, provider) ->
                            lisuRepository.getBoard(
                                providerId = provider.id,
                                boardId = boardId,
                                filterValues = BoardFilterValue.Empty,
                                keywords = keywords,
                            ).map {
                                SearchRecord(
                                    searchBoardId = boardId,
                                    provider = provider,
                                    remoteList = it,
                                )
                            }
                        }
                    )
                }
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val suggestions = searchHistoryRepository.list()
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun search(keywords: String) {
        _keywords.value = keywords
    }

    fun reload(providerId: String) {
        viewModelScope.launch {
            searchRecordsResult.value?.getOrNull()
                ?.find { it.provider.id == providerId }
                ?.remoteList?.reload()
        }
    }

    fun reloadProviders() {
        viewModelScope.launch {
            lisuRepository.providers.value?.reload()
        }
    }
}