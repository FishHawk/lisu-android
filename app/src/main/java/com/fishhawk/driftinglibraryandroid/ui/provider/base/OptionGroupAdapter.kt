package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionItemBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class OptionGroupAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<String, OptionGroupAdapter.ViewHolder>() {
    var onOptionSelected: ((Int) -> Unit)? = null
    var selectedOptionIndex: Int = 0

    fun selectOption(index: Int) {
        onOptionSelected?.invoke(index)
        val oldSelectedOptionIndex = selectedOptionIndex
        selectedOptionIndex = index

        notifyItemChanged(oldSelectedOptionIndex)
        notifyItemChanged(selectedOptionIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProviderOptionItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    inner class ViewHolder(private val binding: ProviderOptionItemBinding) :
        BaseRecyclerViewAdapter.ViewHolder<String>(binding) {

        override fun bind(item: String, position: Int) {
            binding.optionName = item
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                if (position == selectedOptionIndex) R.attr.colorAccent
                else R.attr.colorOnPrimary
                , typedValue, true
            )
            binding.optionColor = typedValue.data
            binding.root.setOnClickListener { selectOption(position) }
        }
    }
}