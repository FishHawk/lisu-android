package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Event
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun <ITEM> ViewModel.remoteList(
    loadFunction: suspend () -> Result<List<ITEM>>
) = object : RemoteList<ITEM>(viewModelScope) {
    override suspend fun load(): Result<List<ITEM>> = loadFunction()
}

abstract class RemoteList<ITEM>(private val scope: CoroutineScope) {
    val state: MediatorLiveData<ViewState> = MediatorLiveData()
    val data: MediatorLiveData<List<ITEM>> = MediatorLiveData()

    private val _refreshFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Feedback>> = _refreshFinish

    protected abstract suspend fun load(): Result<List<ITEM>>

    fun reload() {
        state.value = ViewState.Loading
        data.value = emptyList()

        scope.launch {
            load().onSuccess {
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
            load().onSuccess {
                state.value =
                    if (it.isEmpty()) ViewState.Empty
                    else ViewState.Content
                data.value = it

                _refreshFinish.value = Event(
                    if (it.isEmpty()) Feedback.Hint(R.string.error_hint_empty_refresh_result)
                    else Feedback.Silent
                )
            }.onFailure {
                _refreshFinish.value = Event(Feedback.Failure(it))
            }
        }
    }
}

fun <Item> Fragment.bindToRemoteList(
    refreshLayout: RefreshLayout,
    component: RemoteList<Item>
) {
    component.refreshFinish.observe(viewLifecycleOwner, EventObserver {
        refreshLayout.isHeaderRefreshing = false
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
                override fun onFooterRefresh() {
                    isFooterRefreshing = false
                }
            }
        )
    }
}