package com.fishhawk.driftinglibraryandroid.ui.explore

import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListFromSourceViewModel

class LatestViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : MangaListFromSourceViewModel(source, remoteLibraryRepository) {
    override suspend fun loadResult() =
        remoteLibraryRepository.getLatestMangaList(source, 1)

    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.getLatestMangaList(source, page + 1)
}