package com.fishhawk.driftinglibraryandroid.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.MangaGridThumbnailBinding
import com.fishhawk.driftinglibraryandroid.databinding.MangaLinearThumbnailBinding
import com.fishhawk.driftinglibraryandroid.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import java.text.SimpleDateFormat
import java.util.*


class MangaListAdapter(
    private val activity: Activity,
    private val source: String?
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

        @SuppressLint("SimpleDateFormat")
        override fun bind(item: MangaOutline, position: Int) {
            binding.outline = item

            binding.update.text = item.update?.let {
                val date = Date(item.update)
                val format = SimpleDateFormat("yyyy-MM-dd")
                format.format(date)
            }

            Glide.with(activity)
                .load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
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
