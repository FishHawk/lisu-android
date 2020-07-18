package com.fishhawk.driftinglibraryandroid.ui.explore.globalsearch

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchGroupBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

data class SearchResultGroup(
    val source: String,
    var result: Result<List<MangaOutline>>
)


class GlobalSearchGroupListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<SearchResultGroup, GlobalSearchGroupListAdapter.ViewHolder>(
    mutableListOf()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            GlobalSearchGroupBinding.inflate(
                LayoutInflater.from(activity),
                parent,
                false
            )
        )
    }

    fun addResultGroup(source: String) {
        list.add(
            SearchResultGroup(
                source,
                Result.Loading
            )
        )
    }

    fun updateResultFromSource(source: String, result: Result<List<MangaOutline>>) {
        val index = list.indexOfFirst { it.source == source }
        if (index != -1) {
            list[index].result = result
            notifyItemChanged(index)
        }
    }

    inner class ViewHolder(private val binding: GlobalSearchGroupBinding) :
        BaseRecyclerViewAdapter.ViewHolder<SearchResultGroup>(binding) {

        override fun bind(item: SearchResultGroup) {
            binding.searchResultGroup = item
            val adapter =
                GlobalSearchGroupAdapter(
                    activity,
                    item.source
                )
            binding.list.adapter = adapter
            when (val result = item.result) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        binding.progressBar.visibility = View.GONE
                        binding.list.visibility = View.GONE
                    } else {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.list.visibility = View.VISIBLE
                        adapter.changeList(result.data.toMutableList())
                    }
                }
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.list.visibility = View.INVISIBLE
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.list.visibility = View.GONE
                }
            }
        }
    }
}