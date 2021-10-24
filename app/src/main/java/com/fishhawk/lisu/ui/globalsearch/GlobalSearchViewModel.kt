package com.fishhawk.lisu.ui.globalsearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.Provider
import com.fishhawk.lisu.ui.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchResult(
    val provider: Provider,
    val viewState: ViewState,
    val mangas: List<MangaDto> = emptyList()
)

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteProviderRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _keywords = MutableStateFlow(savedStateHandle.get<String>("keywords"))
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