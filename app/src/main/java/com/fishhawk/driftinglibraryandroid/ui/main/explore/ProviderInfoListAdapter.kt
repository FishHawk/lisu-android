package com.fishhawk.driftinglibraryandroid.ui.main.explore

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.ExploreProviderCardBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class ProviderInfoListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<ProviderInfo, ProviderInfoListAdapter.ViewHolder>() {
    var onItemClicked: ((ProviderInfo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ExploreProviderCardBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: ExploreProviderCardBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ProviderInfo>(binding) {

        override fun bind(item: ProviderInfo, position: Int) {
            binding.providerInfo = item
            binding.root.setOnClickListener { onItemClicked?.invoke(item) }
        }
    }
}
