package com.fishhawk.driftinglibraryandroid.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.*
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.hippo.refreshlayout.RefreshLayout


fun Fragment.showErrorMessage(throwable: Throwable) {
    val message = when (throwable) {
        is EmptyRefreshResultError -> getString(R.string.error_hint_empty_refresh_result)
        is EmptyFetchMoreResultError -> getString(R.string.error_hint_empty_fetch_more_result)
        else -> throwable.message ?: getString(R.string.library_unknown_error_hint)
    }
    view?.makeSnackBar(message)
}

fun <T> Fragment.bindToListViewModel(
    multipleStatusView: MultipleStatusView,
    refreshLayout: RefreshLayout,
    viewModel: BaseListViewModel<T>,
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

    viewModel.refreshFinish.observe(viewLifecycleOwner, EventObserver {
        refreshLayout.isHeaderRefreshing = false
    })

    if (viewModel is BasePartListViewModel) {
        viewModel.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver {
            refreshLayout.isFooterRefreshing = false
        })
    }

    viewModel.operationError.observe(viewLifecycleOwner, EventObserver { exception ->
        showErrorMessage(exception)
    })

    refreshLayout.bindingToViewModel(viewModel)
}
