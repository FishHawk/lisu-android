package com.fishhawk.driftinglibraryandroid.more

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.BaseRecyclerViewAdapter
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.repository.data.SubscriptionMode

class SubscriptionListAdapter(
    private val activity: Activity,
    private val data: MutableList<Subscription>
) : BaseRecyclerViewAdapter<Subscription, SubscriptionListAdapter.ViewHolder>(data) {
    var onDelete: (Int) -> Unit = {}
    var onEnable: (Int) -> Unit = {}
    var onDisable: (Int) -> Unit = {}

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

    fun refreshSubscription(id: Int) {
        val position = data.indexOfFirst { it.id == id }
        if (position != -1) {
            notifyItemChanged(position)
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

    inner class ViewHolder(private val binding: SubscriptionCardBinding) :
        BaseRecyclerViewAdapter.ViewHolder<Subscription>(binding) {

        private fun hideActions() {
            binding.actionPanel.visibility = View.INVISIBLE
            binding.actionProgressBar.visibility = View.VISIBLE
        }

        private fun showActions() {
            binding.actionProgressBar.visibility = View.INVISIBLE
            binding.actionPanel.visibility = View.VISIBLE
        }

        override fun bind(item: Subscription) {
            binding.subscription = item
            showActions()

            when (item.mode) {
                SubscriptionMode.ENABLED -> {
                    val color = ContextCompat.getColor(activity, R.color.loading_indicator_green)
                    binding.coloredHead.setBackgroundColor(color)
                    binding.switchButton.text = "Disable"
                }
                SubscriptionMode.DISABLED -> {
                    val color = ContextCompat.getColor(activity, R.color.loading_indicator_red)
                    binding.coloredHead.setBackgroundColor(color)
                    binding.switchButton.text = "Enable"
                }
            }

            binding.switchButton.setOnClickListener {
                when (item.mode) {
                    SubscriptionMode.ENABLED -> onDisable(item.id)
                    SubscriptionMode.DISABLED -> onEnable(item.id)
                }
                hideActions()
            }
            binding.deleteButton.setOnClickListener {
                onDelete(item.id)
                hideActions()
            }
        }
    }
}