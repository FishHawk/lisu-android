package com.fishhawk.driftinglibraryandroid.explore

import com.fishhawk.driftinglibraryandroid.base.BasePartListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline

class PopularViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BasePartListViewModel<MangaOutline>() {
    private var page = 1

    override suspend fun loadResult() =
        remoteLibraryRepository.getPopularMangaList(source, 1)

    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.getPopularMangaList(source, page + 1)

    override fun onRefreshSuccess() {
        page = 1
    }

    override fun onFetchMoreSuccess(size: Int) {
        if (size > 0) page += 1
    }
}
