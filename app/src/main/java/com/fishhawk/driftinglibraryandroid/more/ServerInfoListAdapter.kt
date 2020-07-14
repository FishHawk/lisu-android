package com.fishhawk.driftinglibraryandroid.more

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.BaseRecyclerViewAdapter
import com.fishhawk.driftinglibraryandroid.databinding.ServerInfoCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.ServerInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class ServerInfoListAdapter(
    private val activity: Activity
) : BaseRecyclerViewAdapter<ServerInfo, ServerInfoListAdapter.ViewHolder>(mutableListOf()) {
    var onCardClicked: (ServerInfo) -> Unit = {}
    var onDelete: (ServerInfo) -> Unit = {}
    var onEdit: (ServerInfo) -> Unit = {}

    var selectedId: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServerInfoListAdapter.ViewHolder {
        return ViewHolder(
            ServerInfoCardBinding.inflate(
                LayoutInflater.from(activity),
                parent,
                false
            )
        )
    }

    inner class ViewHolder(private val binding: ServerInfoCardBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ServerInfo>(binding) {

        override fun bind(item: ServerInfo) {
            binding.serverInfo = item
            if (item.id == selectedId) {
                val color = ContextCompat.getColor(activity, R.color.loading_indicator_green)
                binding.coloredHead.setBackgroundColor(color)
            } else {
                val color = ContextCompat.getColor(activity, R.color.loading_indicator_red)
                binding.coloredHead.setBackgroundColor(color)
            }
            binding.editButton.setOnClickListener { onEdit(item) }
            binding.deleteButton.setOnClickListener { onDelete(item) }
            binding.root.setOnClickListener { onCardClicked(item) }
        }
    }
}