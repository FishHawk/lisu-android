package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.DialogChapterImageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderChapterImageBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseRecyclerViewAdapter


class ImageListAdapter(
    private val context: Context
) : BaseRecyclerViewAdapter<String, ImageListAdapter.ViewHolder>(mutableListOf()) {
    var onImageSave: ((Int, String) -> Unit)? = null
    var onImageShare: ((Int, String) -> Unit)? = null

    var isContinuous = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReaderChapterImageBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    inner class ViewHolder(private val binding: ReaderChapterImageBinding) :
        BaseRecyclerViewAdapter.ViewHolder<String>(binding) {

        override fun bind(item: String, position: Int) {
            if (!isContinuous) {
                binding.root.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                binding.root.requestLayout()
            }

            binding.content.setZoomable(!isContinuous)
            binding.content.setImageResource(android.R.color.transparent)

            binding.background.visibility = View.VISIBLE
            binding.progress.visibility = View.VISIBLE
            binding.errorHint.visibility = View.GONE

            binding.number.text = (position + 1).toString()
            binding.background.setOnLongClickListener {
                createChapterImageActionDialog(position, item)
                true
            }
            binding.content.setOnLongClickListener {
                createChapterImageActionDialog(position, item)
                true
            }

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
                        if (isContinuous) {
                            binding.content.requestLayout()
                            binding.content.layoutParams.height =
                                resource.height * binding.content.width / resource.width
                        }
                        binding.content.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }

    fun createChapterImageActionDialog(page: Int, url: String) {
        val dialogBinding =
            DialogChapterImageBinding.inflate(LayoutInflater.from(context), null, false)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Page ${page + 1}")
            .setView(dialogBinding.root)
            .create()

        dialogBinding.refreshButton.setOnClickListener {
            dialog.dismiss()
            notifyItemChanged(page)
        }
        dialogBinding.shareButton.setOnClickListener {
            dialog.dismiss()
            onImageShare?.invoke(page, url)
        }
        dialogBinding.saveButton.setOnClickListener {
            dialog.dismiss()
            onImageSave?.invoke(page, url)
        }
        dialog.show()
    }
}