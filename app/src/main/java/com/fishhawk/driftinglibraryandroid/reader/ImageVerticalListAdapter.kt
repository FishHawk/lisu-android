package com.fishhawk.driftinglibraryandroid.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ItemChapterImageVerticalBinding


class ImageVerticalListAdapter(
    private val context: Context,
    private var data: List<String>
) : RecyclerView.Adapter<ImageVerticalListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChapterImageVerticalBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: ItemChapterImageVerticalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, position: Int) {
            binding.content.setImageResource(android.R.color.transparent)
            binding.background.visibility = View.VISIBLE
            binding.progress.visibility = View.VISIBLE
            binding.errorHint.visibility = View.GONE

            binding.number.text = position.toString()
            Glide.with(context)
                .asBitmap()
                .load(item)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.background.visibility = View.GONE
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progress.visibility = View.GONE
                        binding.errorHint.visibility = View.VISIBLE
                        if (e != null) binding.errorHint.text = e.message
                        else binding.errorHint.setText(R.string.image_unknown_error_hint)
                        return false
                    }
                })
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        binding.content.requestLayout()
                        binding.content.layoutParams.height =
                            resource.height * binding.content.width / resource.width
                        binding.content.setImageBitmap(resource);
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }
}
