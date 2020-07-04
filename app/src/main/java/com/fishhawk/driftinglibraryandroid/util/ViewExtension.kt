package com.fishhawk.driftinglibraryandroid.util

import android.view.View
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.BaseListViewModel
import com.fishhawk.driftinglibraryandroid.base.BasePartListViewModel
import com.google.android.material.snackbar.Snackbar
import com.hippo.refreshlayout.RefreshLayout

fun RefreshLayout.bindingToViewModel(viewModel: BaseListViewModel<*>) {
    setupDefaultColorSchemeResources()

    val listener = when (viewModel) {
        is BasePartListViewModel<*> -> {
            object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = viewModel.refresh()
                override fun onFooterRefresh() = viewModel.fetchMore()
            }
        }
        else -> {
            object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = viewModel.refresh()
                override fun onFooterRefresh() {
                    isFooterRefreshing = false
                }
            }
        }
    }
    setOnRefreshListener(listener)
}

fun RefreshLayout.setupDefaultColorSchemeResources() {
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
}

fun View.makeSnackBar(content: String) {
    Snackbar.make(this, content, Snackbar.LENGTH_SHORT).show()
}
