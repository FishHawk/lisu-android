package com.fishhawk.driftinglibraryandroid.ui.server

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ServerInfoCardBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class ServerInfoListAdapter(
    private val listener: Listener
) : BaseAdapter<ServerInfo>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun onSelectedChanged(
                viewHolder: RecyclerView.ViewHolder?,
                actionState: Int
            ) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    listener.onDragFinish()
                }
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val oldPosition = viewHolder.bindingAdapterPosition
                val newPosition = target.bindingAdapterPosition
                val item = list[oldPosition]
                if (oldPosition < newPosition) {
                    for (i in oldPosition until newPosition) {
                        list[i] = list[i + 1]
                    }
                } else {
                    for (i in oldPosition downTo newPosition + 1) {
                        list[i] = list[i - 1]
                    }
                }
                list[newPosition] = item
                notifyItemMoved(oldPosition, newPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

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
        fun onDragFinish()
    }
}