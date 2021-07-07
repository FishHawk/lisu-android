package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    val provider: ProviderInfo,
    argKeywords: String
) : FeedbackViewModel() {

    val keywords = MutableStateFlow(argKeywords)

    private var source: ProviderSearchMangaSource? = null

    val mangaList = Pager(PagingConfig(pageSize = 20)) {
        ProviderSearchMangaSource().also { source = it }
    }.flow.cachedIn(viewModelScope)

    init {
        listOf(
            GlobalPreference.selectedServer.asFlow(),
            keywords
        ).forEach { it.onEach { source?.invalidate() }.launchIn(viewModelScope) }
    }

    fun addToLibrary(sourceMangaId: String, targetMangaId: String) = viewModelScope.launch {
        val result = remoteLibraryRepository.createManga(
            targetMangaId,
            provider.id,
            sourceMangaId,
            false
        )
        resultWarp(result) { feed(R.string.successfully_add_to_library) }
    }

    inner class ProviderSearchMangaSource : PagingSource<Int, MangaOutline>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaOutline> {
            val page = params.key ?: 1
            val result = remoteProviderRepository.listManga(
                provider.id,
                keywords.value,
                page
            )
            return when (result) {
                is Result.Success -> LoadResult.Page(
                    data = result.data,
                    prevKey = null,
                    nextKey = page.plus(1)
                )
                is Result.Error -> LoadResult.Error(result.exception)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, MangaOutline>): Int = 0
    }
}
