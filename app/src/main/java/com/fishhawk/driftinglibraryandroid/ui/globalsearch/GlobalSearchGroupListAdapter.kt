package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchGroupBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

data class SearchGroup(
    val provider: ProviderInfo,
    var result: Result<List<MangaOutline>>
)

class GlobalSearchGroupListAdapter(
    private val listener: Listener
) : BaseAdapter<SearchGroup>() {

    fun setListWithEmptyItem(providers: List<ProviderInfo>) {
        setList(providers.map { SearchGroup(it, Result.Loading) })
    }

    fun setSearchGroupResult(provider: ProviderInfo, result: Result<List<MangaOutline>>) {
        list.withIndex()
            .find { it.value.provider.id == provider.id }
            ?.let {
                it.value.result = result
                notifyItemChanged(it.index)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: GlobalSearchGroupBinding) :
        BaseAdapter.ViewHolder<SearchGroup>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GlobalSearchGroupBinding::inflate, parent)
        )

        @SuppressLint("SetTextI18n")
        override fun bind(item: SearchGroup, position: Int) {
            binding.provider.text = "${item.provider.lang} - ${item.provider.name}"

            val adapter = GlobalSearchGroupAdapter(object : GlobalSearchGroupAdapter.Listener {
                override fun onItemClicked(outline: MangaOutline) {
                    listener.onItemClicked(item.provider, outline)
                }
            })
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

    interface Listener {
        fun onItemClicked(info: ProviderInfo, outline: MangaOutline)
    }
}