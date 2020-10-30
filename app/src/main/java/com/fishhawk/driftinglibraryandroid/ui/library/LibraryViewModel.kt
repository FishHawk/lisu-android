package com.fishhawk.driftinglibraryandroid.ui.library

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.Page
import com.fishhawk.driftinglibraryandroid.ui.base.PagingList
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: RemoteLibraryRepository,
    argKeywords: String?
) : FeedbackViewModel() {

    val keywords = MutableLiveData(argKeywords ?: "")

    val mangaList = object : PagingList<Long, MangaOutline>(viewModelScope) {
        override suspend fun loadPage(key: Long?): Result<Page<Long, MangaOutline>> {
            return repository.search(
                lastTime = key ?: Long.MAX_VALUE,
                keywords = keywords.value ?: "",
                limit = 20
            ).map { Page(data = it, nextPage = it.lastOrNull()?.updateTime) }
        }
    }

    init {
        mangaList.list.addSource(keywords) { mangaList.load() }
        mangaList.list.addSource(GlobalPreference.selectedServer) { mangaList.load() }
    }

    fun deleteManga(id: String) = viewModelScope.launch {
        val result = repository.deleteManga(id)
        resultWarp(result) { mangaList.load() }
    }
}
