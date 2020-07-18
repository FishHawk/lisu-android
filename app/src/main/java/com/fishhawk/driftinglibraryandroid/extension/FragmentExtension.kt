package com.fishhawk.driftinglibraryandroid.extension

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.hippo.refreshlayout.RefreshLayout
import com.fishhawk.driftinglibraryandroid.ui.base.Notification
import com.fishhawk.driftinglibraryandroid.repository.EventObserver


fun Fragment.showErrorMessage(notification: Notification) {
    val message = when (notification) {
        is ListEmptyNotification -> getString(R.string.error_hint_empty_refresh_result)
        is ListReachEndNotification -> getString(R.string.error_hint_empty_fetch_more_result)
        is NetworkErrorNotification ->
            notification.throwable.message ?: getString(R.string.library_unknown_error_hint)
        else -> getString(R.string.library_unknown_error_hint)
    }
    view?.makeToast(message)
}

fun <T> Fragment.bindToListViewModel(
    multipleStatusView: MultipleStatusView,
    refreshLayout: RefreshLayout,
    viewModel: RefreshableListViewModel<T>,
    adapter: BaseRecyclerViewAdapter<T, *>
) {
    viewModel.list.observe(viewLifecycleOwner, Observer { result ->
        when (result) {
            is Result.Success -> {
                adapter.changeList(result.data)
                if (result.data.isEmpty()) multipleStatusView.showEmpty()
                else multipleStatusView.showContent()
            }
            is Result.Error -> multipleStatusView.showError(result.exception.message)
            is Result.Loading -> multipleStatusView.showLoading()
        }
    })

    viewModel.refreshFinish.observe(viewLifecycleOwner,
        EventObserver {
            refreshLayout.isHeaderRefreshing = false
        })

    if (viewModel is RefreshableListViewModelWithFetchMore) {
        viewModel.fetchMoreFinish.observe(viewLifecycleOwner,
            EventObserver {
                refreshLayout.isFooterRefreshing = false
            })
    }

    viewModel.notification.observe(viewLifecycleOwner,
        EventObserver { notification ->
            showErrorMessage(notification)
        })

    refreshLayout.bindingToViewModel(viewModel)
}

fun RecyclerView.changeMangaListDisplayMode(adapter: MangaListAdapter) {
    val displayMode = SettingsHelper.displayMode.getValueDirectly()
    if (displayMode == SettingsHelper.DISPLAY_MODE_GRID &&
        (adapter.viewType != MangaListAdapter.ViewType.GRID || layoutManager == null)
    ) {
        adapter.viewType = MangaListAdapter.ViewType.GRID
        layoutManager = GridLayoutManager(context, 3)
        this.adapter = adapter
    } else if (displayMode == SettingsHelper.DISPLAY_MODE_LINEAR &&
        (adapter.viewType != MangaListAdapter.ViewType.LINEAR || layoutManager == null)
    ) {
        adapter.viewType = MangaListAdapter.ViewType.LINEAR
        layoutManager = LinearLayoutManager(context)
        this.adapter = adapter
    }
}