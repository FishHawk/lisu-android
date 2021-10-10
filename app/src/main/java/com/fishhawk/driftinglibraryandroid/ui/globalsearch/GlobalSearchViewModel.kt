package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDto
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.base.ViewState
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _keywords = MutableStateFlow(savedStateHandle.get<String>("keywords"))
    val keywords = _keywords.asStateFlow()

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