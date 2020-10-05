package com.fishhawk.driftinglibraryandroid.ui.main.explore

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fishhawk.driftinglibraryandroid.databinding.ExploreSourceCardBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class ProviderListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<ProviderInfo, ProviderListAdapter.ViewHolder>(mutableListOf()) {
    var onCardClicked: (String) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProviderListAdapter.ViewHolder {
        return ViewHolder(
            ExploreSourceCardBinding.inflate(LayoutInflater.from(activity), parent, false)
        )
    }

    inner class ViewHolder(private val binding: ExploreSourceCardBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ProviderInfo>(binding) {

        override fun bind(item: ProviderInfo, position: Int) {
            binding.providerInfo = item
            binding.root.setOnClickListener { onCardClicked(item.id) }
        }
    }
}
