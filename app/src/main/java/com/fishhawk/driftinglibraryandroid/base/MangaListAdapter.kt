package com.fishhawk.driftinglibraryandroid.base

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.MangaGridThumbnailBinding
import com.fishhawk.driftinglibraryandroid.databinding.MangaLinearThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.util.navToGalleryActivity
import kotlin.Unit as Unit1


class MangaListAdapter(
    private val activity: Activity,
    private var data: MutableList<MangaSummary>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ViewType(val value: Int) {
        GRID(0),
        LINEAR(1)
    }

    private var viewType = ViewType.GRID

    fun setDisplayModeGrid() {
        viewType = ViewType.GRID
    }

    fun setDisplayModeLinear() {
        viewType = ViewType.LINEAR
    }

    fun update(newData: MutableList<MangaSummary>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GridViewHolder -> holder.bind(data[position])
            is LinearViewHolder -> holder.bind(data[position])
        }
    }

    override fun getItemCount() = data.size

    inner class GridViewHolder(private val binding: MangaGridThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MangaSummary) {
            binding.title.text = item.title
            binding.thumb.transitionName = item.id

            Glide.with(activity).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)

            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToGalleryActivity(
                    item.id, item.title, item.thumb, item.source, binding.thumb
                )
            }
        }
    }

    inner class LinearViewHolder(private val binding: MangaLinearThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MangaSummary) {
            binding.title.text = item.title
            binding.author.text = item.author
            binding.update.text = item.update
            binding.source.text = item.source

            binding.update.visibility = if (item.update == null) View.GONE else View.VISIBLE

            Glide.with(activity).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.thumb)

            binding.root.setOnClickListener {
                (activity as AppCompatActivity).navToGalleryActivity(
                    item.id, item.title, item.thumb, item.source, binding.thumb
                )
            }
        }
    }
}
