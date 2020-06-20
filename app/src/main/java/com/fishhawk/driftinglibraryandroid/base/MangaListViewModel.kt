package com.fishhawk.driftinglibraryandroid.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.util.Event

abstract class MangaListViewModel : ViewModel() {
    private val _mangaList: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val mangaList: LiveData<Result<List<MangaSummary>>> = _mangaList

    private val _refreshFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Throwable?>> = _refreshFinish

    private val _fetchMoreFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Throwable?>> = _fetchMoreFinish

    abstract fun load()
    abstract fun refresh()
    abstract fun fetchMore()

    protected fun setLoading() {
        _mangaList.value = Result.Loading
    }

    protected fun processLoadResult(result: Result<List<MangaSummary>>) {
        _mangaList.value = result
    }

    protected fun processRefreshResult(result: Result<List<MangaSummary>>) {
        when (result) {
            is Result.Success -> {
                _mangaList.value = result
                if (result.data.isEmpty()) _refreshFinish.value = Event(EmptyListException())
                else _refreshFinish.value = Event(null)
            }
            is Result.Error -> _refreshFinish.value = Event(result.exception)
        }
    }

    protected fun processFetchMoreResult(result: Result<List<MangaSummary>>) {
        when (result) {
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