package com.fishhawk.driftinglibraryandroid.explore

import com.fishhawk.driftinglibraryandroid.base.MangaListFromSourceViewModel
import com.fishhawk.driftinglibraryandroid.base.RefreshableListViewModelWithFetchMore
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline

class PopularViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : MangaListFromSourceViewModel(source, remoteLibraryRepository) {
    override suspend fun loadResult() =
        remoteLibraryRepository.getPopularMangaList(source, 1)

    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.getPopularMangaList(source, page + 1)
}
