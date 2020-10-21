package com.fishhawk.driftinglibraryandroid.ui.main.provider.base

import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionGroupBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class OptionGroupListAdapter(
    private val listener: Listener
) : BaseAdapter<Pair<String, List<String>>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: ProviderOptionGroupBinding) :
        BaseAdapter.ViewHolder<Pair<String, List<String>>>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ProviderOptionGroupBinding::inflate, parent)
        )

        override fun bind(item: Pair<String, List<String>>, position: Int) {
            val adapter = OptionGroupAdapter(object : OptionGroupAdapter.Listener {
                override fun onOptionSelect(index: Int) {
                    listener.onOptionSelect(item.first, index)
                }
            })
            adapter.setList(item.second)
            binding.options.adapter = adapter
        }
    }

    interface Listener {
        fun onOptionSelect(name: String, index: Int)
    }
}