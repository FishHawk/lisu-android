package com.fishhawk.driftinglibraryandroid.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.base.MangaListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class EmptyListException : Exception()

class LibraryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : MangaListViewModel() {
    private var address = remoteLibraryRepository.url
    var filter: String = ""

    fun reload(filter: String) {
        this.filter = filter
        load()
    }

    fun reloadIfNeed(filter: String) {
        if (address != remoteLibraryRepository.url || mangaList.value !is Result.Success)
            reload(filter)
        address = remoteLibraryRepository.url
    }

    override fun load() {
        setLoading()
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getMangaList("", filter)
            processLoadResult(result)
        }
    }

    override fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getMangaList("", filter)
            processRefreshResult(result)
        }
    }

    override fun fetchMore() {
        val lastId = (mangaList.value as Result.Success).data.let {
            if (it.isEmpty()) "" else it.last().id
        }

        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getMangaList(lastId, filter)
            processFetchMoreResult(result)
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