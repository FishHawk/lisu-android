package com.fishhawk.driftinglibraryandroid.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fishhawk.driftinglibraryandroid.R


class ImageVerticalListAdapter(
    private val context: Context,
    private var data: List<String>
) : RecyclerView.Adapter<ImageVerticalListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_chapter_image_vertical, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val view: View = itemView
        private val imageView: ImageView = view.findViewById(R.id.content)

        fun bind(item: String) {
            imageView.layout(0, 0, 0, 0);
            imageView.setImageResource(R.drawable.placeholder)
            Glide.with(context)
                .asBitmap()
                .load(item)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        imageView.requestLayout()
                        imageView.layoutParams.height =
                            resource.height * imageView.width / resource.width
                        imageView.setImageBitmap(resource);
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }
}
