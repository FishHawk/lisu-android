package com.fishhawk.driftinglibraryandroid.ui.main.server

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ServerInfoCardBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class ServerInfoListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<ServerInfo, ServerInfoListAdapter.ViewHolder>() {
    var onItemClicked: ((ServerInfo) -> Unit)? = null
    var onDeleted: ((ServerInfo) -> Unit)? = null
    var onEdited: ((ServerInfo) -> Unit)? = null

    var selectedId: Int = 0
        set(value) {
            val oldValue = field
            field = value
            notifyItemChanged(field)
            notifyItemChanged(oldValue)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ServerInfoCardBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: ServerInfoCardBinding) :
        BaseRecyclerViewAdapter.ViewHolder<ServerInfo>(binding) {

        override fun bind(item: ServerInfo, position: Int) {
            binding.serverInfo = item
            if (item.id == selectedId) {
                val color = ContextCompat.getColor(context, R.color.loading_indicator_green)
                binding.coloredHead.setBackgroundColor(color)
            } else {
                val color = ContextCompat.getColor(context, R.color.loading_indicator_red)
                binding.coloredHead.setBackgroundColor(color)
            }
            binding.root.setOnClickListener { onItemClicked?.invoke(item) }
            binding.deleteButton.setOnClickListener { onDeleted?.invoke(item) }
            binding.editButton.setOnClickListener { onEdited?.invoke(item) }
        }
    }
}