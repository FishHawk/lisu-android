package com.fishhawk.driftinglibraryandroid.library

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fishhawk.driftinglibraryandroid.databinding.LibraryThumbnailBinding
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary

class LibraryListAdapter(
    private val context: Context,
    private var data: MutableList<MangaSummary>,
    private val listener: (MangaSummary, ImageView) -> Unit
) : RecyclerView.Adapter<LibraryListAdapter.ViewHolder>() {

    fun update(newData: MutableList<MangaSummary>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LibraryThumbnailBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: LibraryThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MangaSummary) {
            binding.title.text = item.title
            binding.thumb.transitionName = item.id
            Glide.with(context).load(item.thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .into(binding.thumb)
            binding.root.setOnClickListener { listener(item, binding.thumb) }
        }
    }
}
