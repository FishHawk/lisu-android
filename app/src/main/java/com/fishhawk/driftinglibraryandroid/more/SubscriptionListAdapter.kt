package com.fishhawk.driftinglibraryandroid.more

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.BaseRecyclerViewAdapter
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription

class SubscriptionListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<Subscription, SubscriptionListAdapter.ViewHolder>(mutableListOf()) {
    var onDelete: (Int) -> Unit = {}
    var onEnable: (Int) -> Unit = {}
    var onDisable: (Int) -> Unit = {}

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

            if (item.isEnabled) {
                val color = ContextCompat.getColor(activity, R.color.loading_indicator_green)
                binding.coloredHead.setBackgroundColor(color)
                binding.switchButton.text = "Disable"
            } else {
                val color = ContextCompat.getColor(activity, R.color.loading_indicator_red)
                binding.coloredHead.setBackgroundColor(color)
                binding.switchButton.text = "Enable"
            }

            binding.switchButton.setOnClickListener {
                if (item.isEnabled) onDisable(item.id)
                else onEnable(item.id)
                hideActions()
            }
            binding.deleteButton.setOnClickListener {
                onDelete(item.id)
                hideActions()
            }
        }
    }
}