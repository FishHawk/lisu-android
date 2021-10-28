package com.fishhawk.lisu.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RemoteLibraryRepository
) : FeedbackViewModel() {

    val keywords = MutableStateFlow(savedStateHandle.get<String>("keywords") ?: "")

    private var source: LibraryMangaSource? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val mangaList =
        combine(
            keywords,
            repository.serviceFlow
        ) { keywords, _ -> keywords }.flatMapLatest {
            Pager(PagingConfig(pageSize = 20)) {
                LibraryMangaSource().also { source = it }
            }.flow.cachedIn(viewModelScope)
        }

    suspend fun getRandomManga(): Result<MangaDto> {
        return repository.getRandomManga()
    }

    fun deleteManga(manga: MangaDto) = viewModelScope.launch {
        val result = repository.deleteManga(manga.providerId, manga.id)
        resultWarp(result) { source?.invalidate() }
    }

    inner class LibraryMangaSource : PagingSource<Int, MangaDto>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaDto> {
            val page = params.key ?: 0
            return repository.search(page, keywords.value).fold(
                { LoadResult.Page(it, null, if (it.isEmpty()) null else page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, MangaDto>): Int = 0
    }
}
