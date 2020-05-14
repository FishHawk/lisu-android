package com.fishhawk.driftinglibraryandroid.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    var filter: String = ""
    var address = remoteLibraryRepository.url


    private val _mangaList: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val mangaList: LiveData<Result<List<MangaSummary>>> = _mangaList

    fun reload(filter: String) {
        this.filter = filter
        _mangaList.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            _mangaList.value = remoteLibraryRepository.getMangaList("", filter)
        }
    }

    fun reloadIfNeed() {
        if (address != remoteLibraryRepository.url || mangaList.value !is Result.Success) reload("")
        address = remoteLibraryRepository.url
    }

    // library content
    private val _refreshResult: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val refreshResult: LiveData<Result<List<MangaSummary>>> = _refreshResult

    fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getMangaList("", filter)
            if (result is Result.Success) _mangaList.value = result
            _refreshResult.value = result
        }
    }

    private val _fetchMoreResult: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val fetchMoreResult: LiveData<Result<List<MangaSummary>>> = _fetchMoreResult

    fun fetchMore() {
        val lastId = (_mangaList.value as Result.Success).data.let {
            if (it.isEmpty()) "" else it.last().id
        }

        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getMangaList(lastId, filter)
            if (result is Result.Success)
                _mangaList.value = (_mangaList.value as Result.Success).also { old ->
                    old.data.plus(result.data)
                }
            _fetchMoreResult.value = result
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