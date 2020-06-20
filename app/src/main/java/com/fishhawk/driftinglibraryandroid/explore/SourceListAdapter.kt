package com.fishhawk.driftinglibraryandroid.explore

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ExploreSourceCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.Source

class SourceListAdapter(
    private val activity: Activity,
    private var data: List<Source>
) : RecyclerView.Adapter<SourceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SourceListAdapter.ViewHolder {
        return ViewHolder(
            ExploreSourceCardBinding.inflate(
                LayoutInflater.from(activity),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SourceListAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: ExploreSourceCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Source) {
            binding.name.text = item.name

            if (!item.isLatestSupport) binding.latest.visibility = View.INVISIBLE
            binding.latest.setOnClickListener {
                val bundle = bundleOf("source" to item.name)
                binding.root.findNavController().navigate(R.id.action_explore_to_latest, bundle)
            }

            binding.browser.setOnClickListener {
                val bundle = bundleOf("source" to item.name)
                binding.root.findNavController().navigate(R.id.action_explore_to_popular, bundle)
            }
        }
    }
}
