package com.fishhawk.driftinglibraryandroid.ui.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<ITEM>(
    protected val list: MutableList<ITEM> = mutableListOf()
) : RecyclerView.Adapter<BaseAdapter.ViewHolder<ITEM>>() {

    fun setList(newList: List<ITEM>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder<ITEM>, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount() = list.size

    protected fun <VB : ViewBinding> viewBinding(
        factory: (LayoutInflater, ViewGroup, Boolean) -> VB,
        parent: ViewGroup
    ): VB {
        return factory(LayoutInflater.from(parent.context), parent, false)
    }

    abstract class ViewHolder<ITEM>(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        open fun bind(item: ITEM, position: Int) {}
    }
}
