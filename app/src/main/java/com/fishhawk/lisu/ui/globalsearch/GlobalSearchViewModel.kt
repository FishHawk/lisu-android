package com.fishhawk.lisu.ui.globalsearch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.base.PagedList
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.ProviderDto
import com.fishhawk.lisu.util.flatten
import kotlinx.coroutines.flow.*

data class SearchResult(
    val provider: ProviderDto,
    val mangas: Result<PagedList<MangaDto>>? = null
)

class GlobalSearchViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {

    private val _keywords = MutableStateFlow(args.getString("keywords") ?: "")
    val keywords = _keywords.asStateFlow()

    private val _searchResultList =
        combine(
            keywords
                .filter { it.isNotBlank() }
                .onEach { searchHistoryRepository.update(SearchHistory("", it)) },
            lisuRepository.providers
                .mapNotNull { it?.value?.getOrNull() }
                .filterNotNull(),
        ) { keywords, providers ->
            flatten(
                providers.map { provider ->
                    lisuRepository.search(provider.id, keywords)
                        .map { provider to it }
                }
            )
        }
            .flattenConcat()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchResultList = _searchResultList
        .map { it.map { SearchResult(it.first, it.second.value) } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val suggestions = searchHistoryRepository.list()
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun search(keywords: String) {
        _keywords.value = keywords
    }
}