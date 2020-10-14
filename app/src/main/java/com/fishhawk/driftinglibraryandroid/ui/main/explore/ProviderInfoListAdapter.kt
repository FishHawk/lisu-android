package com.fishhawk.driftinglibraryandroid.ui.main.explore

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.databinding.ExploreProviderCardItemBinding
import com.fishhawk.driftinglibraryandroid.databinding.ExploreProviderCardHeaderBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

sealed class ListItem {
    data class Header(val header: String) : ListItem()
    data class Item(val info: ProviderInfo) : ListItem()
}

class ProviderInfoListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<ListItem, BaseRecyclerViewAdapter.ViewHolder<ListItem>>() {
    var onItemClicked: ((ProviderInfo) -> Unit)? = null
    var onBrowseClicked: ((ProviderInfo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ListItem> {
        return when (viewType) {
            ViewType.HEADER.value -> HeaderViewHolder(
                ExploreProviderCardHeaderBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
            )
            ViewType.ITEM.value -> ItemViewHolder(
                ExploreProviderCardItemBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
            )
            else -> throw IllegalAccessError()
        }
    }

    enum class ViewType(val value: Int) {
        HEADER(0),
        ITEM(1)
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is ListItem.Item -> ViewType.ITEM.value
            is ListItem.Header -> ViewType.HEADER.value
        }
    }

    fun setProviderInfoList(infoList: List<ProviderInfo>) {
        val infoMap = infoList.groupBy { it.lang }
        val itemList = infoMap.flatMap { entry ->
            listOf(ListItem.Header(entry.key)) + entry.value.map { ListItem.Item(it) }
        }
        setList(itemList)
    }

    inner class ItemViewHolder(private val binding: ExploreProviderCardItemBinding) :
        ViewHolder<ListItem>(binding) {

        override fun bind(item: ListItem, position: Int) {
            val info = (item as ListItem.Item).info
            binding.providerInfo = info
            Glide.with(context).load(info.icon).into(binding.icon)
            binding.root.setOnClickListener { onItemClicked?.invoke(info) }
            binding.browse.setOnClickListener { onBrowseClicked?.invoke(info) }
        }
    }

    inner class HeaderViewHolder(private val binding: ExploreProviderCardHeaderBinding) :
        ViewHolder<ListItem>(binding) {

        override fun bind(item: ListItem, position: Int) {
            val header = (item as ListItem.Header).header
            binding.title.text = header
        }
    }
}
