package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionGroupBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class OptionGroupListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<Pair<String, List<String>>, OptionGroupListAdapter.ViewHolder>(
    mutableListOf()
) {
    var onOptionSelected: (String, Int) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProviderOptionGroupBinding.inflate(
                LayoutInflater.from(activity), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: ProviderOptionGroupBinding) :
        BaseRecyclerViewAdapter.ViewHolder<Pair<String, List<String>>>(binding) {

        override fun bind(item: Pair<String, List<String>>, position: Int) {
            val adapter = OptionGroupAdapter(activity)
            adapter.setList(item.second.toMutableList())
            adapter.onOptionSelected = { onOptionSelected(item.first, it) }
            adapter.selectOption(0)
            binding.options.adapter = adapter
        }
    }
}