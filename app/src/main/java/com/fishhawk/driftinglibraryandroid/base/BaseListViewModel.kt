package com.fishhawk.driftinglibraryandroid.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class EmptyRefreshResultError : Exception()
class EmptyFetchMoreResultError : Exception()

abstract class BasePartListViewModel<T> : BaseListViewModel<T>() {
    private val _fetchMoreFinish: MutableLiveData<Event<Unit>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Unit>> = _fetchMoreFinish

    open fun onFetchMoreSuccess(size: Int) {}
    open fun onFetchMoreError() {}

    abstract suspend fun fetchMoreResult(): Result<List<T>>

    fun fetchMore() {
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = fetchMoreResult()) {
                is Result.Success -> {
                    (_list.value as? Result.Success)?.data?.addAll(result.data)
                    _list.value = _list.value
                    if (result.data.isEmpty()) _operationError.value =
                        Event(EmptyFetchMoreResultError())
                    onFetchMoreSuccess(result.data.size)
                }
                is Result.Error -> {
                    _operationError.value = Event(result.exception)
                    onFetchMoreError()
                }
            }
            _fetchMoreFinish.value = Event(Unit)
        }
    }
}

abstract class BaseListViewModel<T> : ViewModel() {
    protected val _list: MutableLiveData<Result<MutableList<T>>> = MutableLiveData()
    val list: LiveData<Result<MutableList<T>>> = _list

    private val _refreshFinish: MutableLiveData<Event<Unit>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Unit>> = _refreshFinish

    protected val _operationError: MutableLiveData<Event<Throwable>> = MutableLiveData()
    val operationError: LiveData<Event<Throwable>> = _operationError


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
                    if (result.data.isEmpty()) _operationError.value =
                        Event(EmptyRefreshResultError())
                    onRefreshSuccess()
                }
                is Result.Error -> {
                    _operationError.value = Event(result.exception)
                    onRefreshError()
                }
            }
            _refreshFinish.value = Event(Unit)
        }
    }
}