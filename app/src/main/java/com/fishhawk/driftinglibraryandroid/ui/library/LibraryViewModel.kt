package com.fishhawk.driftinglibraryandroid.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RemoteLibraryRepository
) : FeedbackViewModel() {

    val keywords = MutableStateFlow(savedStateHandle.get<String>("keywords") ?: "")

    private var source: LibraryMangaSource? = null

    val mangas = Pager(PagingConfig(pageSize = 20)) {
        LibraryMangaSource().also { source = it }
    }.flow.cachedIn(viewModelScope)

    init {
        listOf(
            GlobalPreference.selectedServer.asFlow(),
            keywords
        ).forEach { it.onEach { source?.invalidate() }.launchIn(viewModelScope) }
    }

    fun deleteManga(id: String) = viewModelScope.launch {
        val result = repository.deleteManga(id)
        resultWarp(result) { source?.invalidate() }
    }

    inner class LibraryMangaSource : PagingSource<Long, MangaOutline>() {
        override suspend fun load(params: LoadParams<Long>): LoadResult<Long, MangaOutline> {
            return repository.listManga(
                params.key ?: Long.MAX_VALUE,
                keywords.value
            ).fold({
                LoadResult.Page(
                    data = it,
                    prevKey = null,
                    nextKey = it.lastOrNull()?.updateTime
                )
            }, { LoadResult.Error(it) })
        }

        override fun getRefreshKey(state: PagingState<Long, MangaOutline>): Long? = null
    }
}
