package com.fishhawk.driftinglibraryandroid.explore

import com.fishhawk.driftinglibraryandroid.base.BasePartListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline

class LatestViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BasePartListViewModel<MangaOutline>() {
    private var page = 1

    override suspend fun loadResult() = remoteLibraryRepository.getLatestMangaList(source, 1)
    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.getLatestMangaList(source, page + 1)

    override fun onRefreshSuccess() {
        page = 1
    }

    override fun onFetchMoreSuccess(size: Int) {
        if (size > 0) page += 1
    }
}