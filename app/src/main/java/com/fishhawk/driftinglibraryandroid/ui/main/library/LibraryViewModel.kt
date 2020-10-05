package com.fishhawk.driftinglibraryandroid.ui.main.library

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModelWithFetchMore
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: RemoteLibraryRepository
) : RefreshableListViewModelWithFetchMore<MangaOutline>() {
    private var address = repository.url
    var filter: String = ""

    override suspend fun loadResult() = repository.search(Long.MAX_VALUE, filter)
    override suspend fun fetchMoreResult(): Result<List<MangaOutline>> {
        val lastTime = when (val result = _list.value) {
            is Result.Success -> result.data.lastOrNull()?.updateTime
            else -> null
        }
        return repository.search(lastTime ?: Long.MAX_VALUE, filter)
    }

    fun reload(filter: String) {
        this.filter = filter
        load()
    }

    fun reloadIfNeed(filter: String) {
        if (address != repository.url || _list.value !is Result.Success)
            reload(filter)
        address = repository.url
    }

    fun deleteManga(id: String) = viewModelScope.launch {
        val result = repository.deleteManga(id)
        resultWarp(result) { load() }
    }
}
