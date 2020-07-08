package com.fishhawk.driftinglibraryandroid.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.base.BasePartListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.launch
import java.lang.Exception

class EmptyListException : Exception()

class LibraryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BasePartListViewModel<MangaOutline>() {
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

    fun deleteManga(id: String) {
        viewModelScope.launch {
            when (val result = remoteLibraryRepository.deleteManga(id)) {
                is Result.Success -> load()
                is Result.Error -> _operationError.value = Event(result.exception)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class LibraryViewModelFactory(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}