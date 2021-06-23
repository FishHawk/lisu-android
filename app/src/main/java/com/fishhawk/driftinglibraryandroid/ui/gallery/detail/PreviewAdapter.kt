package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.databinding.*
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

class PreviewAdapter(
    private val listener: Listener
) : BaseAdapter<String>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<String> {
        return PreviewPageViewHolder(parent)
    }

    inner class PreviewPageViewHolder(private val binding: GalleryPreviewPageBinding) :
        BaseAdapter.ViewHolder<String>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(GalleryPreviewPageBinding::inflate, parent)
        )

        override fun bind(url: String, position: Int) {
            Glide.with(itemView.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.previewImage)
            binding.root.setOnClickListener { listener.onPageClick(position) }
            binding.pageNumber.text = (position + 1).toString()
        }
    }

    interface Listener {
        fun onPageClick(page: Int)
    }
}
