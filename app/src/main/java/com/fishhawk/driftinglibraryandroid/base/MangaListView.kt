package com.fishhawk.driftinglibraryandroid.base

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.MangaListViewBinding
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.SpacingItemDecoration
import com.fishhawk.driftinglibraryandroid.util.makeSnackBar
import com.hippo.refreshlayout.RefreshLayout

class MangaListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MultipleStatusView(context, attrs, defStyleAttr) {
    private val binding = MangaListViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun setup(viewModel: MangaListViewModel, activity: Activity, source: String?) {
        binding.refreshLayout.apply {
            setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = viewModel.refresh()
                override fun onFooterRefresh() = viewModel.fetchMore()
            })

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

        binding.list.adapter = MangaListAdapter(activity, mutableListOf(), source)
    }

    fun updateMangaListDisplayMode() {
        binding.list.apply {
            while (itemDecorationCount > 0) {
                removeItemDecorationAt(0);
            }

            when (SettingsHelper.displayMode.getValueDirectly()) {
                SettingsHelper.DISPLAY_MODE_GRID -> {
                    addItemDecoration(SpacingItemDecoration(3, 16, true))
                    (adapter as MangaListAdapter).setDisplayModeGrid()
                    layoutManager = GridLayoutManager(context, 3)
                }
                SettingsHelper.DISPLAY_MODE_LINEAR -> {
                    addItemDecoration(SpacingItemDecoration(1, 16, true))
                    (adapter as MangaListAdapter).setDisplayModeLinear()
                    layoutManager = LinearLayoutManager(context)
                }
            }
            adapter = adapter
        }
    }

    fun onMangaListChanged(result: Result<List<MangaOutline>>) {
        when (result) {
            is Result.Success -> {
                (binding.list.adapter!! as MangaListAdapter).update(result.data.toMutableList())
                if (binding.list.adapter!!.itemCount == 0) binding.multipleStatusView.showEmpty()
                else binding.multipleStatusView.showContent()
            }
            is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
            is Result.Loading -> binding.multipleStatusView.showLoading()
        }
    }

    fun onRefreshFinishEvent(exception: Throwable?) {
        binding.refreshLayout.isHeaderRefreshing = false
        exception?.apply {
            when (this) {
                is EmptyListException -> binding.root.makeSnackBar(getString(R.string.library_empty_hint))
                else -> binding.root.makeSnackBar(
                    message ?: getString(R.string.library_unknown_error_hint)
                )
            }
        }
    }

    fun onFetchMoreFinishEvent(exception: Throwable?) {
        binding.refreshLayout.isFooterRefreshing = false
        exception?.apply {
            when (this) {
                is EmptyListException -> binding.root.makeSnackBar(getString(R.string.library_reach_end_hint))
                else -> binding.root.makeSnackBar(
                    message ?: getString(R.string.library_unknown_error_hint)
                )
            }
        }
    }

    private fun getString(id: Int): String {
        return context.resources.getString(id)
    }
}
