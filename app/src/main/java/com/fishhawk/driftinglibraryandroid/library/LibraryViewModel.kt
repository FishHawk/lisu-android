package com.fishhawk.driftinglibraryandroid.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class EmptyListException : Exception()

class LibraryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    private var address = remoteLibraryRepository.url
    var filter: String = ""


    private val _mangaList: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val mangaList: LiveData<Result<List<MangaSummary>>> = _mangaList

    fun reload(filter: String) {
        this.filter = filter
        _mangaList.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            _mangaList.value = remoteLibraryRepository.getMangaList("", filter)
        }
    }

    fun reloadIfNeed(filter: String) {
        if (address != remoteLibraryRepository.url || mangaList.value !is Result.Success)
            reload(filter)
        address = remoteLibraryRepository.url
    }


    private val _refreshFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Throwable?>> = _refreshFinish

    fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = remoteLibraryRepository.getMangaList("", filter)) {
                is Result.Success -> {
                    _mangaList.value = result
                    if (result.data.isEmpty()) _refreshFinish.value = Event(EmptyListException())
                    else _refreshFinish.value = Event(null)
                }
                is Result.Error -> _refreshFinish.value = Event(result.exception)
            }
        }
    }


    private val _fetchMoreFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Throwable?>> = _fetchMoreFinish

    fun fetchMore() {
        val lastId = (_mangaList.value as Result.Success).data.let {
            if (it.isEmpty()) "" else it.last().id
        }

        GlobalScope.launch(Dispatchers.Main) {
            when (val result = remoteLibraryRepository.getMangaList(lastId, filter)) {
                is Result.Success -> {
                    (_mangaList.value as Result.Success).data.plus(result.data)
                    _mangaList.value = _mangaList.value
                    if (result.data.isEmpty()) _fetchMoreFinish.value = Event(EmptyListException())
                    else _fetchMoreFinish.value = Event(null)
                }
                is Result.Error -> _fetchMoreFinish.value = Event(result.exception)
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