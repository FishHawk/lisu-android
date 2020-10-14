package com.fishhawk.driftinglibraryandroid.ui.main.explore

import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.databinding.ExploreProviderCardHeaderBinding
import com.fishhawk.driftinglibraryandroid.databinding.ExploreProviderCardItemBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

sealed class ListItem {
    data class Header(val header: String) : ListItem()
    data class Item(val info: ProviderInfo) : ListItem()
}

class ProviderInfoListAdapter(
    private val listener: Listener
) : BaseAdapter<ListItem>() {

    var lastUsedProviderId: String? = null
        set(value) {
            field = value
            updateList()
        }

    var infoList: List<ProviderInfo>? = null
        set(value) {
            field = value
            updateList()
        }

    private fun updateList() {
        if (infoList == null) return

        val infoMap = infoList!!.groupBy { it.lang }
        val itemList = infoMap.flatMap { entry ->
            listOf(ListItem.Header(entry.key)) + entry.value.map { ListItem.Item(it) }
        }.toMutableList()

        infoList!!.find { it.id == lastUsedProviderId }?.let {
            itemList.add(0, ListItem.Item(it))
            itemList.add(0, ListItem.Header("Last used"))
        }

        setList(itemList)
    }


    enum class ViewType(val value: Int) {
        HEADER(0),
        ITEM(1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ListItem> {
        return when (viewType) {
            ViewType.HEADER.value -> HeaderViewHolder(parent)
            ViewType.ITEM.value -> ItemViewHolder(parent)
            else -> throw IllegalAccessError()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is ListItem.Header -> ViewType.HEADER.value
            is ListItem.Item -> ViewType.ITEM.value
        }
    }

    inner class ItemViewHolder(private val binding: ExploreProviderCardItemBinding) :
        ViewHolder<ListItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ExploreProviderCardItemBinding::inflate, parent)
        )

        override fun bind(item: ListItem, position: Int) {
            val info = (item as ListItem.Item).info
            binding.name.text = info.name
            Glide.with(itemView.context).load(info.icon).into(binding.icon)
            binding.root.setOnClickListener { listener.onItemClick(info) }
            binding.browse.setOnClickListener { listener.onBrowseClick(info) }
        }
    }

    inner class HeaderViewHolder(private val binding: ExploreProviderCardHeaderBinding) :
        ViewHolder<ListItem>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ExploreProviderCardHeaderBinding::inflate, parent)
        )

        override fun bind(item: ListItem, position: Int) {
            val header = (item as ListItem.Header).header
            binding.title.text = header
        }
    }

    interface Listener {
        fun onItemClick(providerInfo: ProviderInfo)
        fun onBrowseClick(providerInfo: ProviderInfo)
    }
}
