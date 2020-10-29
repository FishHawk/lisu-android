package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Event
import com.fishhawk.driftinglibraryandroid.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class RefreshableListViewModelWithFetchMore<T> : RefreshableListViewModel<T>() {
    private val _fetchMoreFinish: MutableLiveData<Event<Unit>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Unit>> = _fetchMoreFinish

    abstract suspend fun fetchMoreResult(): Result<List<T>>

    open fun onFetchMoreSuccess(data: List<T>) {}
    open fun onFetchMoreError(throwable: Throwable) {}

    fun fetchMore() {
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = fetchMoreResult()) {
                is Result.Success -> {
                    (_list.value as? Result.Success)?.data?.addAll(result.data)
                    _list.value = _list.value
                    if (result.data.isEmpty()) feed(R.string.error_hint_empty_fetch_more_result)
                    onFetchMoreSuccess(result.data)
                }
                is Result.Error -> {
                    feed(result.exception)
                    onFetchMoreError(result.exception)
                }
            }
            _fetchMoreFinish.value =
                Event(Unit)
        }
    }
}


abstract class RefreshableListViewModel<T> : ListViewModel<T>() {
    private val _refreshFinish: MutableLiveData<Event<Unit>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Unit>> = _refreshFinish

    open fun onRefreshSuccess(data: List<T>) {}
    open fun onRefreshError(throwable: Throwable) {}

    fun refresh() {
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = loadResult()) {
                is Result.Success -> {
                    _list.value = Result.Success(result.data.toMutableList())
                    if (result.data.isEmpty()) feed(R.string.error_hint_empty_refresh_result)
                    onRefreshSuccess(result.data)
                }
                is Result.Error -> {
                    feed(result.exception)
                    onRefreshError(result.exception)
                }
            }
            _refreshFinish.value =
                Event(Unit)
        }
    }
}

abstract class ListViewModel<T> : FeedbackViewModel() {
    protected val _list: MutableLiveData<Result<MutableList<T>>?> = MutableLiveData()
    val list: LiveData<Result<MutableList<T>>?> = _list

    abstract suspend fun loadResult(): Result<List<T>>

    open fun onLoadSuccess(data: List<T>) {}
    open fun onLoadError(throwable: Throwable) {}

    fun load() {
        _list.value = null
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = loadResult()) {
                is Result.Success -> {
                    _list.value = Result.Success(result.data.toMutableList())
                    onLoadSuccess(result.data)
                }
                is Result.Error -> {
                    _list.value = result
                    onLoadError(result.exception)
                }
            }
        }
    }
}
