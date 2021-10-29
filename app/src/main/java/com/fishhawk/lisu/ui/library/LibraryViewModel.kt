package com.fishhawk.lisu.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Effect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LibraryEffect : Effect {
    data class Toast(val message: String) : LibraryEffect
    data class NavToGallery(val manga: MangaDto) : LibraryEffect
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RemoteLibraryRepository
) : BaseViewModel<LibraryEffect>() {

    private val _keywords = MutableStateFlow(savedStateHandle.get<String>("keywords") ?: "")
    val keywords = _keywords.asStateFlow()

    private var source: LibraryMangaSource? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val mangaList =
        combine(
            _keywords,
            repository.serviceFlow
        ) { keywords, _ -> keywords }.flatMapLatest {
            Pager(PagingConfig(pageSize = 20)) {
                LibraryMangaSource().also { source = it }
            }.flow.cachedIn(viewModelScope)
        }

    fun getRandomManga() = viewModelScope.launch {
        repository.getRandomManga()
            .onSuccess { sendEffect(LibraryEffect.NavToGallery(it)) }
            .onFailure { sendEffect(LibraryEffect.Toast(it.localizedMessage ?: "")) }
    }

    fun deleteManga(manga: MangaDto) = viewModelScope.launch {
        repository.deleteManga(manga.providerId, manga.id)
            .onSuccess { source?.invalidate() }
            .onFailure { sendEffect(LibraryEffect.Toast(it.localizedMessage ?: "")) }
    }

    inner class LibraryMangaSource : PagingSource<Int, MangaDto>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaDto> {
            val page = params.key ?: 0
            return repository.search(page, _keywords.value).fold(
                { LoadResult.Page(it, null, if (it.isEmpty()) null else page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, MangaDto>): Int = 0
    }
}
