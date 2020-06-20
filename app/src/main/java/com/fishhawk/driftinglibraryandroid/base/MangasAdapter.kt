package com.fishhawk.driftinglibraryandroid.base

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.MangaGridThumbnailBinding
import com.fishhawk.driftinglibraryandroid.databinding.MangaListThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.util.navToGalleryActivity


class MangasAdapter(
    private val activity: Activity,
    private var data: MutableList<MangaSummary>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ViewType(val value: Int) {
        GRID(0),
        LIST(1)
    }

    private var viewType = ViewType.GRID

    fun setViewTypeGrid() {
        viewType = ViewType.GRID
        notifyDataSetChanged()
    }

    fun setViewTypeList() {
        viewType = ViewType.LIST
        notifyDataSetChanged()
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
            ViewType.LIST ->
                ListViewHolder(
                    MangaListThumbnailBinding.inflate(
                        LayoutInflater.from(activity),
                        parent, false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GridViewHolder -> holder.bind(data[position])
            is ListViewHolder -> holder.bind(data[position])
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

    inner class ListViewHolder(private val binding: MangaListThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MangaSummary) {
            binding.title.text = item.title
            binding.thumb.transitionName = item.id

            item.source.let { binding.source.text = it }
            item.author.let { binding.author.text = it }
            item.update.let { binding.update.text = it }

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
}



