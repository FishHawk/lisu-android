package com.fishhawk.lisu.ui.globalsearch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.ui.base.ViewState
import kotlinx.coroutines.flow.*

data class SearchResult(
    val provider: ProviderDto,
    val viewState: ViewState,
    val mangas: List<MangaDto> = emptyList()
)

class GlobalSearchViewModel(
    args: Bundle,
    private val remoteLibraryRepository: RemoteProviderRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {

    private val _keywords = MutableStateFlow(args.getString("keywords"))
    val keywords = _keywords.asStateFlow()

    init {
        keywords
            .filterNotNull()
            .onEach { searchHistoryRepository.update(SearchHistory("", it)) }
            .launchIn(viewModelScope)
    }

    val suggestions = searchHistoryRepository.list()
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val providerList =
        flow { emit(remoteLibraryRepository.listProvider().getOrNull()) }.filterNotNull()

    val searchResultList =
        combine(keywords.filterNotNull(), providerList) { keywords, providerList ->
            providerList.map { provider ->
                flow {
                    emit(
                        remoteLibraryRepository.search(provider.id, 0, keywords).fold(
                            { SearchResult(provider, ViewState.Loaded, it) },
                            { SearchResult(provider, ViewState.Failure(it)) }
                        )
                    )
                }.stateIn(
                    viewModelScope,
                    SharingStarted.Lazily,
                    SearchResult(provider, ViewState.Loading)
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    fun search(keywords: String) {
        _keywords.value = keywords
    }
}