package com.fishhawk.driftinglibraryandroid.ui.provider

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionItemBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter
import com.fishhawk.driftinglibraryandroid.ui.base.resolveAttrColor

class OptionGroupAdapter(
    private val listener: Listener,
    private var selectedOptionIndex: Int = -1
) : BaseAdapter<String>() {


    private fun selectOption(index: Int) {
        listener.onOptionSelect(index)
        val oldSelectedOptionIndex = selectedOptionIndex
        selectedOptionIndex = index

        notifyItemChanged(oldSelectedOptionIndex)
        notifyItemChanged(selectedOptionIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    inner class ViewHolder(private val binding: ProviderOptionItemBinding) :
        BaseAdapter.ViewHolder<String>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ProviderOptionItemBinding::inflate, parent)
        )

        override fun bind(item: String, position: Int) {
            binding.root.text = item
            binding.root.setTextColor(
                itemView.context.resolveAttrColor(
                    if (position == selectedOptionIndex) R.attr.colorAccent
                    else R.attr.colorOnPrimary
                )
            )
            binding.root.setOnClickListener { selectOption(position) }
        }
    }

    interface Listener {
        fun onOptionSelect(index: Int)
    }
}