package com.fishhawk.driftinglibraryandroid.ui.main.subscription

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionCardBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.Subscription
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class SubscriptionListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<Subscription, SubscriptionListAdapter.ViewHolder>() {
    var onDeleted: ((String) -> Unit)? = null
    var onEnabled: ((String) -> Unit)? = null
    var onDisabled: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SubscriptionCardBinding.inflate(
                LayoutInflater.from(context), parent, false
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

        override fun bind(item: Subscription, position: Int) {
            binding.subscription = item
            showActions()

            if (item.isEnabled) {
                val color = ContextCompat.getColor(context, R.color.loading_indicator_green)
                binding.coloredHead.setBackgroundColor(color)
            } else {
                val color = ContextCompat.getColor(context, R.color.loading_indicator_red)
                binding.coloredHead.setBackgroundColor(color)
            }

            binding.enableButton.setOnClickListener {
                onEnabled?.invoke(item.id)
                hideActions()
            }
            binding.disableButton.setOnClickListener {
                onDisabled?.invoke(item.id)
                hideActions()
            }
            binding.deleteButton.setOnClickListener {
                onDeleted?.invoke(item.id)
                hideActions()
            }
        }
    }
}