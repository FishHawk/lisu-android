package com.fishhawk.driftinglibraryandroid.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BasePartListViewModel<T> : BaseListViewModel<T>() {
    protected val _fetchMoreFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Throwable?>> = _fetchMoreFinish

    open fun onFetchMoreSuccess() {}
    open fun onFetchMoreError() {}

    abstract suspend fun fetchMoreResult(): Result<List<T>>

    fun fetchMore() {
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = fetchMoreResult()) {
                is Result.Success -> {
                    (_list.value as? Result.Success)?.data?.addAll(result.data)
                    _list.value = _list.value
                    _fetchMoreFinish.value =
                        if (result.data.isEmpty()) Event(EmptyListException())
                        else Event(null)
                    onFetchMoreSuccess()
                }
                is Result.Error -> {
                    _fetchMoreFinish.value = Event(result.exception)
                    onFetchMoreError()
                }
            }
        }
    }
}

abstract class BaseListViewModel<T> : ViewModel() {
    protected val _list: MutableLiveData<Result<MutableList<T>>> = MutableLiveData()
    val list: LiveData<Result<MutableList<T>>> = _list

    protected val _refreshFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Throwable?>> = _refreshFinish


    open fun onLoadSuccess() {}
    open fun onLoadError() {}

    open fun onRefreshSuccess() {}
    open fun onRefreshError() {}


    abstract suspend fun loadResult(): Result<List<T>>

    fun load() {
        _list.value = Result.Loading
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = loadResult()) {
                is Result.Success -> {
                    _list.value = Result.Success(result.data.toMutableList())
                    onLoadSuccess()
                }
                is Result.Error -> {
                    _list.value = result
                    onLoadError()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = loadResult()) {
                is Result.Success -> {
                    _list.value = Result.Success(result.data.toMutableList())
                    _refreshFinish.value =
                        if (result.data.isEmpty()) Event(EmptyListException())
                        else Event(null)
                    onRefreshSuccess()
                }
                is Result.Error -> {
                    _refreshFinish.value = Event(result.exception)
                    onRefreshError()
                }

            }
        }
    }
}