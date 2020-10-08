package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionGroupBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class OptionGroupListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<Pair<String, List<String>>, OptionGroupListAdapter.ViewHolder>() {
    var onOptionSelected: ((String, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProviderOptionGroupBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: ProviderOptionGroupBinding) :
        BaseRecyclerViewAdapter.ViewHolder<Pair<String, List<String>>>(binding) {

        override fun bind(item: Pair<String, List<String>>, position: Int) {
            val adapter = OptionGroupAdapter(context)
            adapter.setList(item.second)
            adapter.onOptionSelected = { onOptionSelected?.invoke(item.first, it) }
            binding.options.adapter = adapter
        }
    }
}