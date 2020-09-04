package com.fishhawk.driftinglibraryandroid.ui.server

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ServerInfoCardBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

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
    ): ViewHolder {
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

        override fun bind(item: ServerInfo, position: Int) {
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