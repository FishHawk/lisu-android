package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.app.Activity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderOptionItemBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class OptionGroupAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<String, OptionGroupAdapter.ViewHolder>(mutableListOf()) {
    var onOptionSelected: (Int) -> Unit = {}
    var selectedOptionIndex: Int = 0

    fun selectOption(index: Int) {
        onOptionSelected(index)
        val oldSelectedOptionIndex = selectedOptionIndex
        selectedOptionIndex = index

        notifyItemChanged(oldSelectedOptionIndex)
        notifyItemChanged(selectedOptionIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProviderOptionItemBinding.inflate(
                LayoutInflater.from(activity), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: ProviderOptionItemBinding) :
        BaseRecyclerViewAdapter.ViewHolder<String>(binding) {

        override fun bind(item: String, position: Int) {
            binding.optionName = item
            val typedValue = TypedValue()
            activity.theme.resolveAttribute(
                if (position == selectedOptionIndex) R.attr.colorAccent
                else R.attr.colorOnPrimary
                , typedValue, true
            )
            binding.optionColor = typedValue.data
            binding.root.setOnClickListener { selectOption(position) }
        }
    }
}