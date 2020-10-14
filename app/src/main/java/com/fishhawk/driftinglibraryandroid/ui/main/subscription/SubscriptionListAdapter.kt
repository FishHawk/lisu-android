package com.fishhawk.driftinglibraryandroid.ui.main.subscription

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SubscriptionCardBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.Subscription
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class SubscriptionListAdapter(
    private val listener: Listener
) : BaseAdapter<Subscription>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: SubscriptionCardBinding) :
        BaseAdapter.ViewHolder<Subscription>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(SubscriptionCardBinding::inflate, parent)
        )

        private fun hideActions() {
            binding.actionPanel.visibility = View.INVISIBLE
            binding.actionProgressBar.visibility = View.VISIBLE
        }

        private fun showActions() {
            binding.actionProgressBar.visibility = View.INVISIBLE
            binding.actionPanel.visibility = View.VISIBLE
        }

        override fun bind(item: Subscription, position: Int) {
            showActions()

            binding.targetManga.text = item.id
            binding.provider.text = item.providerId
            binding.sourceManga.text = item.sourceManga

            val colorId =
                if (item.isEnabled) R.color.loading_indicator_green
                else R.color.loading_indicator_red
            val color = ContextCompat.getColor(itemView.context, colorId)
            binding.coloredHead.setBackgroundColor(color)

            binding.deleteButton.setOnClickListener {
                listener.onSubscriptionDelete(item.id)
                hideActions()
            }

            binding.enableButton.visibility = if (!item.isEnabled) View.VISIBLE else View.GONE
            binding.enableButton.setOnClickListener {
                listener.onSubscriptionEnable(item.id)
                hideActions()
            }

            binding.disableButton.visibility = if (item.isEnabled) View.VISIBLE else View.GONE
            binding.disableButton.setOnClickListener {
                listener.onSubscriptionDisable(item.id)
                hideActions()
            }
        }
    }

    interface Listener {
        fun onSubscriptionDelete(id: String)
        fun onSubscriptionEnable(id: String)
        fun onSubscriptionDisable(id: String)
    }
}