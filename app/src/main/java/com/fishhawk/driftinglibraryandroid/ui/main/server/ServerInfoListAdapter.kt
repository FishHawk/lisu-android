package com.fishhawk.driftinglibraryandroid.ui.main.server

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ServerInfoCardBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class ServerInfoListAdapter(
    private val listener: Listener
) : BaseAdapter<ServerInfo>() {

    var selectedId: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: ServerInfoCardBinding) :
        BaseAdapter.ViewHolder<ServerInfo>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ServerInfoCardBinding::inflate, parent)
        )

        override fun bind(item: ServerInfo, position: Int) {
            binding.name.text = item.name
            binding.address.text = item.address

            val colorId =
                if (item.id == selectedId) R.color.loading_indicator_green
                else R.color.loading_indicator_red
            val color = ContextCompat.getColor(itemView.context, colorId)
            binding.coloredHead.setBackgroundColor(color)

            binding.root.setOnClickListener { listener.onItemClick(item) }
            binding.deleteButton.setOnClickListener { listener.onServerDelete(item) }
            binding.editButton.setOnClickListener { listener.onServerEdit(item) }
        }
    }

    interface Listener {
        fun onItemClick(info: ServerInfo)
        fun onServerDelete(info: ServerInfo)
        fun onServerEdit(info: ServerInfo)
    }
}