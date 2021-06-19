package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.remotePagingList
import kotlinx.coroutines.launch

class SearchViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val providerId: String,
    argKeywords: String
) : FeedbackViewModel() {

    val keywords = MutableLiveData(argKeywords)

    val outlines = remotePagingList<Int, MangaOutline> { key ->
        val page = key ?: 1
        remoteProviderRepository.listManga(
            providerId = providerId,
            keywords = keywords.value!!,
            page = page
        ).map { Pair(page + 1, it) }
    }.apply {
        data.addSource(keywords) { reload() }
    }

    fun addToLibrary(sourceMangaId: String, targetMangaId: String) = viewModelScope.launch {
        val result = remoteLibraryRepository.createManga(
            targetMangaId,
            providerId,
            sourceMangaId,
            false
        )
        resultWarp(result) { feed(R.string.successfully_add_to_library) }
    }
}
