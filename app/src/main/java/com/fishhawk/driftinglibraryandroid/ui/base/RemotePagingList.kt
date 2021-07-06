package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun <KEY, ITEM> ViewModel.remotePagingList(
    loadFunction: suspend (key: KEY?) -> Result<Pair<KEY?, List<ITEM>>>
) = object : RemotePagingList<KEY, ITEM>(viewModelScope) {
    override suspend fun load(key: KEY?): Result<Pair<KEY?, List<ITEM>>> = loadFunction(key)
}

abstract class RemotePagingList<KEY, ITEM>(private val scope: CoroutineScope) {
    val state: MediatorLiveData<ViewState> = MediatorLiveData()
    val data: MediatorLiveData<List<ITEM>> = MediatorLiveData()

    private val _isRefreshing: MutableLiveData<Boolean> = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _isLoadMore: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoadMore: LiveData<Boolean> = _isLoadMore

    private val _refreshFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Feedback>> = _refreshFinish

    private val _fetchMoreFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Feedback>> = _fetchMoreFinish

    private var nextKey: KEY? = null
    private var isFinished: Boolean = false

    protected abstract suspend fun load(key: KEY?): Result<Pair<KEY?, List<ITEM>>>

    fun reload() {
        nextKey = null
        isFinished = false

        state.value = ViewState.Loading
        data.value = emptyList()

        scope.launch {
            load(nextKey).onSuccess { (key, it) ->
                nextKey = key
                isFinished = it.isEmpty()

                state.value =
                    if (it.isEmpty()) ViewState.Empty
                    else ViewState.Content
                data.value = it
            }.onFailure {
                state.value = ViewState.Error(it)
                data.value = emptyList()
            }
        }
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            load(null).onSuccess { (key, it) ->
                nextKey = key
                isFinished = it.isEmpty()

                state.value =
                    if (it.isEmpty()) ViewState.Empty
                    else ViewState.Content
                data.value = it

                _isRefreshing.value = false
                _refreshFinish.value = Event(
                    if (it.isEmpty()) Feedback.Hint(R.string.error_hint_empty_refresh_result)
                    else Feedback.Silent
                )
            }.onFailure {
                _isRefreshing.value = false
                _refreshFinish.value = Event(Feedback.Failure(it))
            }
        }
    }

    fun fetchMore() {
        if (state.value != ViewState.Content)
            _fetchMoreFinish.value = Event(Feedback.Silent)

        if (isFinished)
            _fetchMoreFinish.value =
                Event(Feedback.Hint(R.string.error_hint_empty_fetch_more_result))

        _isLoadMore.value = true
        scope.launch {
            load(nextKey).onSuccess { (key, it) ->
                nextKey = key
                isFinished = it.isEmpty()

                data.value =
                    (data.value?.toMutableList() ?: mutableListOf())
                        .apply { addAll(it) }

                _isLoadMore.value = false
                _fetchMoreFinish.value = Event(
                    if (it.isEmpty()) Feedback.Hint(R.string.error_hint_empty_fetch_more_result)
                    else Feedback.Silent
                )
            }.onFailure {
                _isLoadMore.value = false
                _fetchMoreFinish.value = Event(Feedback.Failure(it))
            }
        }
    }
}

fun <Item> Fragment.bindToRemotePagingList(
    refreshLayout: RefreshLayout,
    component: RemotePagingList<*, Item>,
) {
    component.refreshFinish.observe(viewLifecycleOwner, EventObserver {
        refreshLayout.isHeaderRefreshing = false
        processFeedback(it)
    })

    component.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver {
        refreshLayout.isFooterRefreshing = false
        processFeedback(it)
    })

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

        setOnRefreshListener(
            object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = component.refresh()
                override fun onFooterRefresh() = component.fetchMore()
            }
        )
    }
}