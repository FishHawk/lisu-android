package com.fishhawk.driftinglibraryandroid.ui.library

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.remotePagingList
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: RemoteLibraryRepository,
    argKeywords: String?
) : FeedbackViewModel() {

    val keywords = MutableLiveData(argKeywords ?: "")

    val outlines = remotePagingList<Long, MangaOutline> { key ->
        repository.search(
            lastTime = key ?: Long.MAX_VALUE,
            keywords = keywords.value ?: "",
            limit = 20
        ).map { Pair(it.lastOrNull()?.updateTime, it) }
    }.apply {
        data.addSource(keywords) { reload() }
        data.addSource(GlobalPreference.selectedServer.asFlow().asLiveData()) { reload() }
    }

    fun deleteManga(id: String) = viewModelScope.launch {
        val result = repository.deleteManga(id)
        resultWarp(result) { outlines.reload() }
    }
}
