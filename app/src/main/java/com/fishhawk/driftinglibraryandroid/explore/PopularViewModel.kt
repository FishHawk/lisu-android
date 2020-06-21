package com.fishhawk.driftinglibraryandroid.explore

import com.fishhawk.driftinglibraryandroid.base.MangaListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PopularViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : MangaListViewModel() {
    private var page = 1

    override fun load() {
        page = 1
        setLoading()
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getPopularMangaList(source, page)
            processLoadResult(result)
        }
    }

    override fun refresh() {
        page = 1
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getPopularMangaList(source, page)
            processRefreshResult(result)
        }
    }

    override fun fetchMore() {
        page += 1
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getPopularMangaList(source, page)
            processFetchMoreResult(result)
        }
    }
}