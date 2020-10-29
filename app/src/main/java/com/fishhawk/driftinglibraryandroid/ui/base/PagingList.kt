package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Event
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class Page<KEY, ITEM>(
    val data: List<ITEM>,
    val nextPage: KEY?
)

// TODO : fix consistency
abstract class PagingList<KEY, ITEM>(private val scope: CoroutineScope) {
    val list: MediatorLiveData<Result<MutableList<ITEM>>?> = MediatorLiveData()

    private val _refreshFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Feedback>> = _refreshFinish

    private val _fetchMoreFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Feedback>> = _fetchMoreFinish

    private var nextPage: KEY? = null
    private var canFetchMore: Boolean = true

    protected abstract suspend fun loadPage(key: KEY?): Result<Page<KEY, ITEM>>

    private fun onNewPage(page: Page<KEY, ITEM>) {
        nextPage = page.nextPage
        if (page.data.isEmpty()) canFetchMore = false
    }

    fun load() {
        nextPage = null
        list.value = null
        scope.launch {
            list.value = loadPage(null)
                .onSuccess { onNewPage(it) }
                .map { it.data.toMutableList() }
        }
    }

    fun refresh() {
        scope.launch {
            loadPage(null).onSuccess {
                onNewPage(it)

                list.value = Result.Success(it.data.toMutableList())

                _refreshFinish.value = Event(
                    if (it.data.isEmpty()) Feedback.Hint(R.string.error_hint_empty_refresh_result)
                    else Feedback.Silent
                )
            }.onFailure {
                _refreshFinish.value = Event(Feedback.Failure(it))
            }
        }
    }

    fun fetchMore() {
        if (!canFetchMore)
            _fetchMoreFinish.value = Event(
                Feedback.Hint(R.string.error_hint_empty_fetch_more_result)
            )
        else scope.launch {
            loadPage(nextPage).onSuccess {
                onNewPage(it)

                val items = (list.value as? Result.Success)?.data ?: mutableListOf()
                items.addAll(it.data)
                list.value = Result.Success(items)

                _fetchMoreFinish.value = Event(
                    if (it.data.isEmpty()) Feedback.Hint(R.string.error_hint_empty_fetch_more_result)
                    else Feedback.Silent
                )
            }.onFailure {
                _fetchMoreFinish.value = Event(Feedback.Failure(it))
            }
        }
    }
}

fun <Item> Fragment.bindToPagingList(
    multipleStatusView: MultipleStatusView,
    refreshLayout: RefreshLayout,
    component: PagingList<*, Item>,
    adapter: BaseAdapter<Item>
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
