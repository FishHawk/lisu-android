package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseRecyclerViewAdapter<ITEM, VH : BaseRecyclerViewAdapter.ViewHolder<ITEM>>(
    protected val list: MutableList<ITEM> = mutableListOf()
) : RecyclerView.Adapter<VH>() {

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount() = list.size

    fun setList(newList: List<ITEM>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    abstract class ViewHolder<ITEM>(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        open fun bind(item: ITEM, position: Int) {}
    }
}
