package com.fishhawk.lisu.ui.library

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Effect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

sealed interface LibraryEffect : Effect {
    data class Toast(val message: String) : LibraryEffect
    data class NavToGallery(val manga: MangaDto) : LibraryEffect
}

class LibraryViewModel(
    private val repository: RemoteLibraryRepository
) : BaseViewModel<LibraryEffect>() {
    private var source: LibraryMangaSource? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val mangaList = repository.serviceFlow.flatMapLatest {
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
            return repository.search(page).fold(
                { LoadResult.Page(it, null, if (it.isEmpty()) null else page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, MangaDto>): Int = 0
    }
}
