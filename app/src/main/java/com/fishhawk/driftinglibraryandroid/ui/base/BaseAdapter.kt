package com.fishhawk.driftinglibraryandroid.ui.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<ITEM>(
    val list: MutableList<ITEM> = mutableListOf()
) : RecyclerView.Adapter<BaseAdapter.ViewHolder<ITEM>>() {

    open val enableDiffUtil = false

    protected open fun areItemsTheSame(a: ITEM, b: ITEM): Boolean {
        return a === b
    }

    protected open fun areContentsTheSame(a: ITEM, b: ITEM): Boolean {
        return true
    }

    fun setList(newList: List<ITEM>) {
        if (enableDiffUtil) setListWithDiffUtil(newList)
        else setListWithoutDiffUtil(newList)
    }

    private fun setListWithDiffUtil(newList: List<ITEM>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areItemsTheSame(list[oldItemPosition], newList[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areContentsTheSame(list[oldItemPosition], newList[newItemPosition])

            override fun getOldListSize() = list.size
            override fun getNewListSize() = newList.size
        })
        diff.dispatchUpdatesTo(this)

        list.clear()
        list.addAll(newList)
    }

    private fun setListWithoutDiffUtil(newList: List<ITEM>) {
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
