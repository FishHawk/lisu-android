package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.fishhawk.comicimageview.OnTapListener
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderChapterImageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderEmptyPageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderErrorPageBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter

sealed class Page {
    data class ContentPage(val url: String) : Page()
    data class ErrorPage(val message: String) : Page()
    object EmptyPage : Page()
}

class ImageListAdapter(private val context: Context) : BaseAdapter<Page>() {

    var onItemLongPress: ((position: Int, url: String) -> Unit) = { _, _ -> }
    var onItemSingleTapConfirmed: ((event: MotionEvent) -> Unit) = {}

    var isContinuous = false


    enum class ViewType(val value: Int) {
        CONTENT(0),
        ERROR(1),
        EMPTY(2),
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Page> {
        return when (viewType) {
            ViewType.CONTENT.value -> ContentPageViewHolder(parent)
            ViewType.ERROR.value -> ErrorPageViewHolder(parent)
            ViewType.EMPTY.value -> EmptyPageViewHolder(parent)
            else -> throw IllegalAccessError()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is Page.ContentPage -> ViewType.CONTENT.value
            is Page.ErrorPage -> ViewType.ERROR.value
            is Page.EmptyPage -> ViewType.EMPTY.value
        }
    }

    fun setContentPage(newList: List<String>) {
        if (newList.isEmpty()) setList(listOf(Page.EmptyPage))
        else setList(newList.map { Page.ContentPage(it) })
    }

    fun setErrorPage(message: String) {
        setList(listOf(Page.ErrorPage(message)))
    }


    inner class EmptyPageViewHolder(binding: ReaderEmptyPageBinding) :
        BaseAdapter.ViewHolder<Page>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ReaderEmptyPageBinding::inflate, parent)
        )
    }

    inner class ErrorPageViewHolder(private val binding: ReaderErrorPageBinding) :
        BaseAdapter.ViewHolder<Page>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ReaderErrorPageBinding::inflate, parent)
        )

        override fun bind(item: Page, position: Int) {
            val message = (item as Page.ErrorPage).message
            binding.message.text = message
        }
    }

    inner class ContentPageViewHolder(private val binding: ReaderChapterImageBinding) :
        BaseAdapter.ViewHolder<Page>(binding) {


        constructor(parent: ViewGroup) : this(
            viewBinding(ReaderChapterImageBinding::inflate, parent)
        )

        private fun setLoading() {
            binding.content.isVisible = false
            binding.progress.isVisible = true
            binding.retryButton.isVisible = false
            binding.errorHint.isVisible = false
        }

        private fun setError() {
            binding.content.isVisible = false
            binding.progress.isVisible = false
            binding.retryButton.isVisible = true
            binding.errorHint.isVisible = true
        }

        private fun setContent() {
            binding.content.isVisible = true
            binding.progress.isVisible = false
            binding.retryButton.isVisible = false
            binding.errorHint.isVisible = false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bind(item: Page, position: Int) {
            val url = (item as Page.ContentPage).url
            setLoading()

            if (!isContinuous) {
                binding.root.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                binding.root.requestLayout()
            }

            val detector = GestureDetector(
                context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent?): Boolean = true

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean =
                        onItemSingleTapConfirmed.invoke(e).let { true }

                    override fun onLongPress(e: MotionEvent) =
                        onItemLongPress.invoke(position, url)
                })

            binding.root.setOnTouchListener { _, event ->
                detector.onTouchEvent(event)
            }

            binding.number.text = (position + 1).toString()

            binding.retryButton.setOnClickListener { notifyItemChanged(position) }

            binding.content.zoomable = !isContinuous
            binding.content.setImageResource(android.R.color.transparent)

            binding.content.setOnLongClickListener {
                onItemLongPress.invoke(position, url).let { true }
            }

            binding.content.onTapListener = object : OnTapListener {
                override fun onTap(view: View?, ev: MotionEvent) {
                    onItemSingleTapConfirmed.invoke(ev)
                }
            }

            Glide.with(context)
                .asBitmap()
                .timeout(20000)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setContent()
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setError()
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
}