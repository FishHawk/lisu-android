package com.fishhawk.driftinglibraryandroid.ui.main.globalsearch

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class GlobalSearchGroupAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<MangaOutline, GlobalSearchGroupAdapter.ViewHolder>() {
    var onItemClicked: ((MangaOutline) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GlobalSearchThumbnailBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    inner class ViewHolder(private val binding: GlobalSearchThumbnailBinding) :
        BaseRecyclerViewAdapter.ViewHolder<MangaOutline>(binding) {

        override fun bind(item: MangaOutline, position: Int) {
            binding.outline = item

            Glide.with(context).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.thumb)

            binding.root.setOnClickListener { onItemClicked?.invoke(item) }
        }
    }
}