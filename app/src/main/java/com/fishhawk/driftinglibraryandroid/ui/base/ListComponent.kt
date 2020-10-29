package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Event
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class RefreshableListComponentWithFetchMore<T>(
    scope: CoroutineScope
) : RefreshableListComponent<T>(scope) {
    private val _fetchMoreFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Feedback>> = _fetchMoreFinish

    protected open fun onFetchMoreFinish(result: Result<List<T>>) {}
    protected abstract suspend fun fetchMoreInternal(): Result<List<T>>

    fun fetchMore() {
        scope.launch {
            val result = fetchMoreInternal()

            result.onSuccess {
                (_list.value as? Result.Success)?.data?.addAll(it)
                _list.value = _list.value

                _fetchMoreFinish.value = Event(
                    if (it.isEmpty()) Feedback.Hint(R.string.error_hint_empty_fetch_more_result)
                    else Feedback.Silent
                )
            }.onFailure { _fetchMoreFinish.value = Event(Feedback.Failure(it)) }

            onFetchMoreFinish(result)
        }
    }
}


abstract class RefreshableListComponent<T>(
    scope: CoroutineScope
) : ListComponent<T>(scope) {
    private val _refreshFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Feedback>> = _refreshFinish

    protected open fun onRefreshFinish(result: Result<List<T>>) {}

    fun refresh() {
        scope.launch {
            val result = loadInternal()

            result.onSuccess {
                _list.value = Result.Success(it.toMutableList())
                _refreshFinish.value = Event(
                    if (it.isEmpty()) Feedback.Hint(R.string.error_hint_empty_refresh_result)
                    else Feedback.Silent
                )
            }.onFailure { _refreshFinish.value = Event(Feedback.Failure(it)) }

            onRefreshFinish(result)
        }
    }
}

abstract class ListComponent<T>(
    protected val scope: CoroutineScope
) {
    protected val _list: MutableLiveData<Result<MutableList<T>>?> = MutableLiveData()
    val list: LiveData<Result<MutableList<T>>?> = _list

    protected abstract suspend fun loadInternal(): Result<List<T>>
    fun load() {
        scope.launch {
            _list.value = null
            _list.value = loadInternal().map { it.toMutableList() }
        }
    }

    open fun reset() {
        _list.value = null
    }
}


fun <T> Fragment.bindToListComponent(
    multipleStatusView: MultipleStatusView,
    refreshLayout: RefreshLayout,
    component: ListComponent<T>,
    adapter: BaseAdapter<T>
) {
    component.list.observe(viewLifecycleOwner) { result ->
        when (result) {
            is Result.Success -> {
                adapter.setList(result.data)
                if (result.data.isEmpty()) multipleStatusView.showEmpty()
                else multipleStatusView.showContent()
            }
            is Result.Error -> multipleStatusView.showError(result.exception.message)
            null -> multipleStatusView.showLoading()
        }
    }

    if (component is RefreshableListComponent) {
        component.refreshFinish.observe(viewLifecycleOwner, EventObserver {
            refreshLayout.isHeaderRefreshing = false
            processFeedback(it)
        })
    }

    if (component is RefreshableListComponentWithFetchMore) {
        component.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver {
            refreshLayout.isFooterRefreshing = false
            processFeedback(it)
        })
    }

    with(refreshLayout) {
        setHeaderColorSchemeResources(
            R.color.loading_indicator_red,
            R.color.loading_indicator_purple,
            R.color.loading_indicator_blue,
            R.color.loading_indicator_cyan,
            R.color.loading_indicator_green,
            R.color.loading_indicator_yellow
        )
        setFooterColorSchemeResources(
            R.color.loading_indicator_red,
            R.color.loading_indicator_blue,
            R.color.loading_indicator_green,
            R.color.loading_indicator_orange
        )

        val listener = when (component) {
            is RefreshableListComponentWithFetchMore -> object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = component.refresh()
                override fun onFooterRefresh() = component.fetchMore()
            }
            is RefreshableListComponent -> object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = component.refresh()
                override fun onFooterRefresh() {
                    isFooterRefreshing = false
                }
            }
            else -> null
        }
        setOnRefreshListener(listener)
    }
}
