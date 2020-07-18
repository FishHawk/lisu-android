package com.fishhawk.driftinglibraryandroid.ui.library

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModelWithFetchMore
import kotlinx.coroutines.launch


class LibraryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : RefreshableListViewModelWithFetchMore<MangaOutline>() {
    private var address = remoteLibraryRepository.url
    var filter: String = ""

    override suspend fun loadResult() = remoteLibraryRepository.searchInLibrary("", filter)
    override suspend fun fetchMoreResult(): Result<List<MangaOutline>> {
        val lastId = when (val result = _list.value) {
            is Result.Success -> result.data.let { if (it.isEmpty()) "" else it.last().id }
            else -> ""
        }
        return remoteLibraryRepository.searchInLibrary(lastId, filter)
    }

    fun reload(filter: String) {
        this.filter = filter
        load()
    }

    fun reloadIfNeed(filter: String) {
        if (address != remoteLibraryRepository.url || _list.value !is Result.Success)
            reload(filter)
        address = remoteLibraryRepository.url
    }

    fun deleteManga(id: String) = viewModelScope.launch {
        val result = remoteLibraryRepository.deleteMangaFromLibrary(id)
        resultWarp(result) { load() }
    }
}
