package com.fishhawk.driftinglibraryandroid.ui.subscription

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter


class SubscriptionListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<Subscription, SubscriptionListAdapter.ViewHolder>(mutableListOf()) {
    var onDelete: (Int) -> Unit = {}
    var onEnable: (Int) -> Unit = {}
    var onDisable: (Int) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
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
            } else {
                val color = ContextCompat.getColor(activity, R.color.loading_indicator_red)
                binding.coloredHead.setBackgroundColor(color)
            }

            binding.enableButton.setOnClickListener {
                onEnable(item.id)
                hideActions()
            }
            binding.disableButton.setOnClickListener {
                onDisable(item.id)
                hideActions()
            }
            binding.deleteButton.setOnClickListener {
                onDelete(item.id)
                hideActions()
            }
        }
    }
}