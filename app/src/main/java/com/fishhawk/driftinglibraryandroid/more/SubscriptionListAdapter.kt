package com.fishhawk.driftinglibraryandroid.more

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.repository.data.SubscriptionMode

class SubscriptionListAdapter(
    private val activity: Activity,
    private var data: List<Subscription>
) : RecyclerView.Adapter<SubscriptionListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionListAdapter.ViewHolder {
        return ViewHolder(
            SubscriptionCardBinding.inflate(
                LayoutInflater.from(activity),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SubscriptionListAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: SubscriptionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Subscription) {
            binding.source.text = item.source
            binding.sourceManga.text = item.sourceManga
            binding.targetManga.text = item.targetManga

            val color = when (item.mode) {
                SubscriptionMode.ENABLED ->
                    ContextCompat.getColor(activity, R.color.loading_indicator_green)
                SubscriptionMode.DISABLED ->
                    ContextCompat.getColor(activity, R.color.loading_indicator_red)
            }
            binding.coloredHead.setBackgroundColor(color)

            binding.root.setOnClickListener {
                println("asdfasdf")
            }
        }
    }
}