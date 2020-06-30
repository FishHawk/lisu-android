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
    private val data: MutableList<Subscription>
) : RecyclerView.Adapter<SubscriptionListAdapter.ViewHolder>() {
    private var onDelete: (Int) -> Unit = {}
    private var onEnable: (Int) -> Unit = {}
    private var onDisable: (Int) -> Unit = {}

    fun enableSubscription(id: Int) {
        val position = data.indexOfFirst { it.id == id }
        val subscription = data.getOrNull(position)
        subscription?.let {
            it.mode = SubscriptionMode.ENABLED
            notifyItemChanged(position)
        }
    }

    fun disableSubscription(id: Int) {
        val position = data.indexOfFirst { it.id == id }
        val subscription = data.getOrNull(position)
        subscription?.let {
            it.mode = SubscriptionMode.DISABLED
            notifyItemChanged(position)
        }
    }

    fun deleteSubscription(id: Int) {
        val position = data.indexOfFirst { it.id == id }
        if (position != -1) {
            data.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionListAdapter.ViewHolder {
        return ViewHolder(
            SubscriptionCardBinding.inflate(
                LayoutInflater.from(activity),
                parent,
                false
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
            binding.subscription = item

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

            binding.switchButton.setOnClickListener {
                when (item.mode) {
                    SubscriptionMode.ENABLED -> onEnable(item.id)
                    SubscriptionMode.DISABLED -> onDisable(item.id)
                }
            }
            binding.switchButton.setOnClickListener {
                onDelete(item.id)
            }
        }
    }
}