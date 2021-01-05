package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchGroupBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter
import com.fishhawk.driftinglibraryandroid.widget.ViewState

data class SearchGroup(
    val provider: ProviderInfo,
    var result: Result<List<MangaOutline>>?
)

class GlobalSearchGroupListAdapter(
    private val listener: Listener
) : BaseAdapter<SearchGroup>() {

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
            binding.recyclerView.adapter = adapter

            val result = item.result
            if (result is Result.Success) adapter.setList(result.data)
            binding.multiStateView.viewState = when (result) {
                is Result.Success -> {
                    if (result.data.isEmpty()) ViewState.Empty
                    else ViewState.Content
                }
                is Result.Error -> ViewState.Error(result.exception)
                null -> ViewState.Loading
            }
        }
    }

    interface Listener {
        fun onItemClicked(info: ProviderInfo, outline: MangaOutline)
    }
}