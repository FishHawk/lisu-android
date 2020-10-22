package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.util.TypedValue
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionItemBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class OptionGroupAdapter(
    private val listener: Listener
) : BaseAdapter<String>() {

    var selectedOptionIndex: Int = 0

    fun selectOption(index: Int) {
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

            val typedValue = TypedValue()
            itemView.context.theme.resolveAttribute(
                if (position == selectedOptionIndex) R.attr.colorAccent
                else R.attr.colorOnPrimary
                , typedValue, true
            )
            binding.root.setTextColor(typedValue.data)

            binding.root.setOnClickListener { selectOption(position) }
        }
    }

    interface Listener {
        fun onOptionSelect(index: Int)
    }
}