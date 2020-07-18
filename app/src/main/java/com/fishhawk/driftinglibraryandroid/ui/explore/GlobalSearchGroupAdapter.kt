package com.fishhawk.driftinglibraryandroid.ui.explore

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchThumbnailBinding
import com.fishhawk.driftinglibraryandroid.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter

class GlobalSearchGroupAdapter(
    private val activity: Activity,
    private val source: String
) : BaseRecyclerViewAdapter<MangaOutline, GlobalSearchGroupAdapter.ViewHolder>(mutableListOf()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GlobalSearchGroupAdapter.ViewHolder {
        return ViewHolder(
            GlobalSearchThumbnailBinding.inflate(
                LayoutInflater.from(activity),
                parent,
                false
            )
        )
    }

    inner class ViewHolder(private val binding: GlobalSearchThumbnailBinding) :
        BaseRecyclerViewAdapter.ViewHolder<MangaOutline>(binding) {

        override fun bind(item: MangaOutline) {
            binding.outline = item

            Glide.with(activity).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.thumb)

            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToGalleryActivity(
                    item.id, item.title, item.thumb, source
                )
            }
        }
    }
}