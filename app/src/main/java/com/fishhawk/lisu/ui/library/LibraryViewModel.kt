package com.fishhawk.lisu.ui.library

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaKeyDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface LibraryEffect : Event {
    data class Toast(val message: String) : LibraryEffect
    data class NavToGallery(val manga: MangaDto) : LibraryEffect
}

class LibraryViewModel(
    private val libraryRepo: RemoteLibraryRepository,
    searchHistoryRepository: SearchHistoryRepository,
) : BaseViewModel<LibraryEffect>() {

    private val _keywords = MutableStateFlow("")
    val keywords = _keywords.asStateFlow()

    val suggestions = searchHistoryRepository.list()
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var source: LibraryMangaSource? = null

    val mangaList =
        combine(
            _keywords,
            libraryRepo.serviceFlow
        ) { keywords, _ -> keywords }.flatMapLatest { keywords ->
            Pager(PagingConfig(pageSize = 20)) {
                LibraryMangaSource(keywords).also { source = it }
            }.flow.cachedIn(viewModelScope)
        }

    fun getRandomManga() = viewModelScope.launch {
        libraryRepo.getRandomManga()
            .onSuccess { sendEvent(LibraryEffect.NavToGallery(it)) }
            .onFailure { sendEvent(LibraryEffect.Toast(it.localizedMessage ?: "")) }
    }

    fun deleteMultipleManga(mangas: List<MangaKeyDto>) = viewModelScope.launch {
        libraryRepo.deleteMultipleMangas(mangas)
            .onSuccess { source?.invalidate() }
            .onFailure { sendEvent(LibraryEffect.Toast(it.localizedMessage ?: "")) }
    }

    fun search(keywords: String) {
        _keywords.value = keywords
    }

    inner class LibraryMangaSource(private val keywords: String) : PagingSource<Int, MangaDto>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaDto> {
            val page = params.key ?: 0
            return libraryRepo.search(page, keywords).fold(
                { LoadResult.Page(it, null, if (it.isEmpty()) null else page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, MangaDto>): Int = 0
    }
}
