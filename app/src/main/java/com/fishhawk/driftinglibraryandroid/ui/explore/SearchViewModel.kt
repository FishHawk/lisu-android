package com.fishhawk.driftinglibraryandroid.ui.explore

import com.fishhawk.driftinglibraryandroid.ui.base.MangaListFromSourceViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository

class SearchViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : MangaListFromSourceViewModel(source, remoteLibraryRepository) {
    private var keywords = ""

    override suspend fun loadResult() =
        remoteLibraryRepository.searchInSource(source, keywords, 1)

    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.searchInSource(source, keywords, page + 1)

    fun search(keywords: String) {
        this.keywords = keywords
        load()
    }
}