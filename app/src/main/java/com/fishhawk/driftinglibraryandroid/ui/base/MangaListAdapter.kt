package com.fishhawk.driftinglibraryandroid.ui.base

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.MangaGridThumbnailBinding
import com.fishhawk.driftinglibraryandroid.databinding.MangaLinearThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.extension.navToGalleryActivity


class MangaListAdapter(
    private val activity: Activity,
    private val source: String?
) : BaseRecyclerViewAdapter<MangaOutline, BaseRecyclerViewAdapter.ViewHolder<MangaOutline>>(
    mutableListOf()
) {
    var onCardLongClicked: (MangaOutline) -> Unit = {}

    enum class ViewType(val value: Int) {
        GRID(0),
        LINEAR(1)
    }

    var viewType = ViewType.GRID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<MangaOutline> {
        return when (this.viewType) {
            ViewType.GRID ->
                GridViewHolder(
                    MangaGridThumbnailBinding.inflate(
                        LayoutInflater.from(activity),
                        parent, false
                    )
                )
            ViewType.LINEAR ->
                LinearViewHolder(
                    MangaLinearThumbnailBinding.inflate(
                        LayoutInflater.from(activity),
                        parent, false
                    )
                )
        }
    }

    inner class GridViewHolder(private val binding: MangaGridThumbnailBinding) :
        BaseRecyclerViewAdapter.ViewHolder<MangaOutline>(binding) {

        override fun bind(item: MangaOutline) {
            binding.outline = item

            Glide.with(activity).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)

            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToGalleryActivity(
                    item.id, item.title, item.thumb, source
                )
            }
            binding.root.setOnLongClickListener {
                onCardLongClicked(item)
                true
            }
        }
    }

    inner class LinearViewHolder(private val binding: MangaLinearThumbnailBinding) :
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
            binding.root.setOnLongClickListener {
                onCardLongClicked(item)
                true
            }
        }
    }
}
