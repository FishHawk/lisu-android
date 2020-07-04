package com.fishhawk.driftinglibraryandroid.explore

import com.fishhawk.driftinglibraryandroid.base.BasePartListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline

class SearchViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BasePartListViewModel<MangaOutline>() {
    private var page = 1
    private var keywords = ""

    override suspend fun loadResult() =
        remoteLibraryRepository.search(source, keywords, 1)

    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.search(source, keywords, page + 1)

    override fun onRefreshSuccess() {
        page = 1
    }

    override fun onFetchMoreSuccess(size: Int) {
        if (size > 0) page += 1
    }

    fun search(keywords: String) {
        this.keywords = keywords
        load()
    }
}