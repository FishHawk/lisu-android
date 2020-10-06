package com.fishhawk.driftinglibraryandroid.ui.main.globalsearch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchGroupBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

data class SearchGroup(
    val provider: ProviderInfo,
    var result: Result<List<MangaOutline>>
)

class GlobalSearchGroupListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<SearchGroup, GlobalSearchGroupListAdapter.ViewHolder>() {
    var onItemClicked: ((ProviderInfo, MangaOutline) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GlobalSearchGroupBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    fun setListWithEmptyItem(providers: List<ProviderInfo>) {
        this.setList(providers.map { SearchGroup(it, Result.Loading) })
    }

    fun setSearchGroupResult(provider: ProviderInfo, result: Result<List<MangaOutline>>) {
        list.withIndex()
            .find { it.value.provider.id == provider.id }
            ?.let {
                it.value.result = result
                notifyItemChanged(it.index)
            }
    }

    inner class ViewHolder(private val binding: GlobalSearchGroupBinding) :
        BaseRecyclerViewAdapter.ViewHolder<SearchGroup>(binding) {

        override fun bind(item: SearchGroup, position: Int) {
            binding.searchGroup = item

            val adapter = GlobalSearchGroupAdapter(context)
            adapter.onItemClicked = { onItemClicked?.invoke(item.provider, it) }
            binding.list.adapter = adapter

            when (val result = item.result) {
                is Result.Success -> {
                    adapter.setList(result.data)
                    if (result.data.isEmpty()) binding.multipleStatusView.showEmpty()
                    else binding.multipleStatusView.showContent()
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        }
    }
}