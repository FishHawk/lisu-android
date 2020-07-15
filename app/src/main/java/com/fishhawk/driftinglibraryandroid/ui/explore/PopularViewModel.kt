package com.fishhawk.driftinglibraryandroid.ui.explore

import com.fishhawk.driftinglibraryandroid.ui.base.MangaListFromSourceViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository

class PopularViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : MangaListFromSourceViewModel(source, remoteLibraryRepository) {
    override suspend fun loadResult() =
        remoteLibraryRepository.getPopularMangaList(source, 1)

    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.getPopularMangaList(source, page + 1)
}
