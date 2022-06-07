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

sealed interface LibraryEvent : Event {
    data class GetRandomSuccess(val manga: MangaDto) : LibraryEvent
    data class GetRandomFailure(val exception: Throwable) : LibraryEvent
    data class DeleteMultipleFailure(val exception: Throwable) : LibraryEvent
}

class LibraryViewModel(
    private val libraryRepo: RemoteLibraryRepository,
    searchHistoryRepo: SearchHistoryRepository,
) : BaseViewModel<LibraryEvent>() {

    private val _keywords = MutableStateFlow("")
    val keywords = _keywords.asStateFlow()

    val suggestions = searchHistoryRepo.list()
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var source: LibraryMangaSource? = null
    val mangaList = combine(
        _keywords,
        libraryRepo.serviceFlow
    ) { keywords, _ -> keywords }
        .flatMapLatest { keywords ->
            Pager(PagingConfig(pageSize = 20)) {
                LibraryMangaSource(keywords).also { source = it }
            }.flow.cachedIn(viewModelScope)
        }

    fun getRandomManga() {
        viewModelScope.launch {
            libraryRepo.getRandomManga()
                .onSuccess { sendEvent(LibraryEvent.GetRandomSuccess(it)) }
                .onFailure { sendEvent(LibraryEvent.GetRandomFailure(it)) }
        }
    }

    fun deleteMultipleManga(mangas: List<MangaKeyDto>) {
        viewModelScope.launch {
            libraryRepo.deleteMultipleMangas(mangas)
                .onSuccess { source?.invalidate() }
                .onFailure { sendEvent(LibraryEvent.DeleteMultipleFailure(it)) }
        }
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
