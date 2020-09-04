package com.fishhawk.driftinglibraryandroid.ui.explore

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ExploreSourceCardBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo

class ProviderListAdapter(
    private val activity: Activity,
    private var data: List<ProviderInfo>
) : RecyclerView.Adapter<ProviderListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProviderListAdapter.ViewHolder {
        return ViewHolder(
            ExploreSourceCardBinding.inflate(
                LayoutInflater.from(activity),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ProviderListAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: ExploreSourceCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProviderInfo) {
            binding.name.text = item.name

            binding.root.setOnClickListener {
                val bundle = bundleOf("providerId" to item.id, "keywords" to "")
                binding.root.findNavController().navigate(R.id.action_explore_to_search, bundle)
            }

            if (!item.isLatestSupport) binding.latest.visibility = View.INVISIBLE
            binding.latest.setOnClickListener {
                val bundle = bundleOf("providerId" to item.id)
                binding.root.findNavController().navigate(R.id.action_explore_to_latest, bundle)
            }

            binding.popular.setOnClickListener {
                val bundle = bundleOf("providerId" to item.id)
                binding.root.findNavController().navigate(R.id.action_explore_to_popular, bundle)
            }
        }
    }
}
