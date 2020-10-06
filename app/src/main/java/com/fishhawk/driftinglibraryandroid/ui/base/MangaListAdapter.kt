package com.fishhawk.driftinglibraryandroid.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.SimpleDateFormat
import java.util.*
import com.fishhawk.driftinglibraryandroid.databinding.MangaGridThumbnailBinding
import com.fishhawk.driftinglibraryandroid.databinding.MangaLinearThumbnailBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline

class MangaListAdapter(
    private val activity: Activity,
    private val providerId: String?
) : BaseRecyclerViewAdapter<MangaOutline, BaseRecyclerViewAdapter.ViewHolder<MangaOutline>>(
    mutableListOf()
) {
    var onCardLongClicked: (MangaOutline) -> Unit = {}

    enum class ViewMode(val value: Int) {
        GRID(0),
        LINEAR(1)
    }

    var viewMode = ViewMode.GRID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<MangaOutline> {
        return when (this.viewMode) {
            ViewMode.GRID ->
                GridViewHolder(
                    MangaGridThumbnailBinding.inflate(
                        LayoutInflater.from(activity),
                        parent, false
                    )
                )
            ViewMode.LINEAR ->
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

        override fun bind(item: MangaOutline, position: Int) {
            binding.outline = item

            Glide.with(activity)
                .load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.thumb)

            binding.root.setOnClickListener {
                activity.navToGalleryActivity(item, providerId)
            }
            binding.root.setOnLongClickListener {
                onCardLongClicked(item)
                true
            }
        }
    }

    inner class LinearViewHolder(private val binding: MangaLinearThumbnailBinding) :
        BaseRecyclerViewAdapter.ViewHolder<MangaOutline>(binding) {

        @SuppressLint("SimpleDateFormat")
        override fun bind(item: MangaOutline, position: Int) {
            binding.outline = item

            binding.update.text = item.updateTime?.let {
                val date = Date(item.updateTime)
                val format = SimpleDateFormat("yyyy-MM-dd")
                format.format(date)
            }

            Glide.with(activity)
                .load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.thumb)

            binding.root.setOnClickListener {
                activity.navToGalleryActivity(item, providerId)
            }
            binding.root.setOnLongClickListener {
                onCardLongClicked(item)
                true
            }
        }
    }
}
