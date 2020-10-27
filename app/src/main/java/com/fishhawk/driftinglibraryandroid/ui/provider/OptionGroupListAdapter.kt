package com.fishhawk.driftinglibraryandroid.ui.provider

import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionGroupBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

data class OptionGroup(
    val name: String,
    val options: List<String>,
    val startIndex: Int
)

class OptionGroupListAdapter(
    private val listener: Listener
) : BaseAdapter<OptionGroup>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: ProviderOptionGroupBinding) :
        BaseAdapter.ViewHolder<OptionGroup>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ProviderOptionGroupBinding::inflate, parent)
        )

        override fun bind(item: OptionGroup, position: Int) {
            val adapter = OptionGroupAdapter(object : OptionGroupAdapter.Listener {
                override fun onOptionSelect(index: Int) {
                    listener.onOptionSelect(item.name, index)
                }
            }, item.startIndex)
            adapter.setList(item.options)
            binding.options.adapter = adapter
        }
    }

    interface Listener {
        fun onOptionSelect(name: String, index: Int)
    }
}