package com.fishhawk.driftinglibraryandroid.reader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.fishhawk.driftinglibraryandroid.R
import com.github.chrisbanes.photoview.PhotoView

class ImageListAdapter(
    private val context: Context,
    private var data: List<String>
) : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_chapter_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val view: View = itemView
        private val photoView: PhotoView = view.findViewById(R.id.content)

        fun bind(item: String) {
            photoView.setZoomable(true)
            Glide.with(context)
                .asBitmap()
                .load(item)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(photoView)
        }
    }
}
