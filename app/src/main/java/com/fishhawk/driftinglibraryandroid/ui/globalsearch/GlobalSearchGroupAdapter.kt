package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class GlobalSearchGroupAdapter(
    private val listener: Listener
) : BaseAdapter<MangaOutline>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    inner class ViewHolder(private val binding: GlobalSearchThumbnailBinding) :
        BaseAdapter.ViewHolder<MangaOutline>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GlobalSearchThumbnailBinding::inflate, parent)
        )

        override fun bind(item: MangaOutline, position: Int) {
            binding.title.text = item.title

            Glide.with(itemView.context).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.thumb)

            binding.root.setOnClickListener { listener.onItemClicked(item) }
        }
    }

    interface Listener {
        fun onItemClicked(outline: MangaOutline)
    }
}