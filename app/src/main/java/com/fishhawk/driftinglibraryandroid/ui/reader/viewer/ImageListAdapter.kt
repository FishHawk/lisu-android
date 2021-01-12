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
import com.fishhawk.driftinglibraryandroid.widget.comicimageview.OnTapListener
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderChapterImageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderEmptyPageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageTransitionNextBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageTransitionPrevBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderChapter
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderChapterPointer
import com.fishhawk.driftinglibraryandroid.util.glide.OnProgressChangeListener
import com.fishhawk.driftinglibraryandroid.util.glide.ProgressInterceptor
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

sealed class Page {
    data class ContentPage(val index: Int, val url: String) : Page()
    object EmptyPage : Page()

    data class PrevTransitionPage(val curr: ReaderChapter, val prev: ReaderChapter) : Page()
    data class NextTransitionPage(val curr: ReaderChapter, val next: ReaderChapter) : Page()
}

class ImageListAdapter(private val context: Context) : BaseAdapter<Page>() {

    var onItemLongPress: ((position: Int, url: String) -> Unit) = { _, _ -> }
    var onItemSingleTapConfirmed: ((event: MotionEvent) -> Unit) = {}

    var isContinuous = false


    enum class ViewType(val value: Int) {
        CONTENT(0),
        EMPTY(1),
        PREV_TRANSITION(2),
        NEXT_TRANSITION(3),
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Page> {
        return when (viewType) {
            ViewType.CONTENT.value -> ContentPageViewHolder(parent)
            ViewType.EMPTY.value -> EmptyPageViewHolder(parent)
            ViewType.PREV_TRANSITION.value -> PrevTransitionPageViewHolder(parent)
            ViewType.NEXT_TRANSITION.value -> NextTransitionPageViewHolder(parent)
            else -> throw IllegalAccessError()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is Page.ContentPage -> ViewType.CONTENT.value
            is Page.EmptyPage -> ViewType.EMPTY.value
            is Page.PrevTransitionPage -> ViewType.PREV_TRANSITION.value
            is Page.NextTransitionPage -> ViewType.NEXT_TRANSITION.value
        }
    }

    override val enableDiffUtil: Boolean = true
    override fun areItemsTheSame(a: Page, b: Page): Boolean {
        if (a is Page.ContentPage
            && b is Page.ContentPage
            && a.index == b.index
            && a.url == b.url
        ) return true
        return super.areItemsTheSame(a, b)
    }

    fun setChapterPointer(pointer: ReaderChapterPointer) {
        val newList = mutableListOf<Page>()

        val currChapter = pointer.currChapter
        val prevChapter = pointer.prevChapter
        val nextChapter = pointer.nextChapter

        if (prevChapter != null && prevChapter.state != ViewState.Content)
            newList.add(Page.PrevTransitionPage(currChapter, prevChapter))

        if (currChapter.images.isEmpty())
            newList.add(Page.EmptyPage)
        else
            newList.addAll(currChapter.images.mapIndexed { index, url ->
                Page.ContentPage(index, url)
            })

        if (nextChapter != null && nextChapter.state != ViewState.Content)
            newList.add(Page.NextTransitionPage(currChapter, nextChapter))

        setList(newList)
    }


    inner class EmptyPageViewHolder(private val binding: ReaderEmptyPageBinding) :
        BaseAdapter.ViewHolder<Page>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ReaderEmptyPageBinding::inflate, parent)
        )

        @SuppressLint("ClickableViewAccessibility")
        override fun bind(item: Page, position: Int) {
            val detector = GestureDetector(
                context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent?): Boolean = true

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean =
                        onItemSingleTapConfirmed.invoke(e).let { true }
                })

            binding.root.setOnTouchListener { _, event -> detector.onTouchEvent(event) }
        }
    }

    inner class PrevTransitionPageViewHolder(private val binding: ReaderPageTransitionPrevBinding) :
        BaseAdapter.ViewHolder<Page>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ReaderPageTransitionPrevBinding::inflate, parent)
        )

        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        override fun bind(item: Page, position: Int) {
            val page = (item as Page.PrevTransitionPage)
            binding.currChapterName.text = "${page.curr.name} ${page.curr.title}"
            binding.prevChapterName.text = "${page.prev.name} ${page.prev.title}"

            when (val state = page.prev.state) {
                is ViewState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.errorHint.isVisible = false
                }
                is ViewState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.errorHint.isVisible = true
                    binding.errorHint.text = state.exception.message
                }
            }

            val detector = GestureDetector(
                context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent?): Boolean = true

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean =
                        onItemSingleTapConfirmed.invoke(e).let { true }
                })

            binding.root.setOnTouchListener { _, event -> detector.onTouchEvent(event) }
        }
    }

    inner class NextTransitionPageViewHolder(private val binding: ReaderPageTransitionNextBinding) :
        BaseAdapter.ViewHolder<Page>(binding) {

        constructor(parent: ViewGroup) : this(
            viewBinding(ReaderPageTransitionNextBinding::inflate, parent)
        )

        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        override fun bind(item: Page, position: Int) {
            val page = (item as Page.NextTransitionPage)
            binding.currChapterName.text = "${page.curr.name} ${page.curr.title}"
            binding.nextChapterName.text = "${page.next.name} ${page.next.title}"

            when (val state = page.next.state) {
                is ViewState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.errorHint.isVisible = false
                }
                is ViewState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.errorHint.isVisible = true
                    binding.errorHint.text = state.exception.message
                }
            }

            val detector = GestureDetector(
                context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent?): Boolean = true

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean =
                        onItemSingleTapConfirmed.invoke(e).let { true }
                })

            binding.root.setOnTouchListener { _, event -> detector.onTouchEvent(event) }
        }
    }

    inner class ContentPageViewHolder(private val binding: ReaderChapterImageBinding) :
        BaseAdapter.ViewHolder<Page>(binding) {


        constructor(parent: ViewGroup) : this(
            viewBinding(ReaderChapterImageBinding::inflate, parent)
        )

        private fun setLoading() {
            binding.content.isVisible = false
            binding.progressBar.isVisible = true
            binding.retryButton.isVisible = false
            binding.errorHint.isVisible = false
        }

        private fun setError() {
            binding.content.isVisible = false
            binding.progressBar.isVisible = false
            binding.retryButton.isVisible = true
            binding.errorHint.isVisible = true
        }

        private fun setContent() {
            binding.content.isVisible = true
            binding.progressBar.isVisible = false
            binding.retryButton.isVisible = false
            binding.errorHint.isVisible = false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bind(item: Page, position: Int) {
            val page = item as Page.ContentPage
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
                        onItemLongPress.invoke(page.index, page.url)
                })

            binding.root.setOnTouchListener { _, event ->
                detector.onTouchEvent(event)
            }

            binding.number.text = (page.index + 1).toString()

            binding.retryButton.setOnClickListener { notifyItemChanged(page.index) }

            binding.content.zoomable = !isContinuous
            binding.content.setImageResource(android.R.color.transparent)

            binding.content.setOnLongClickListener {
                onItemLongPress.invoke(page.index, page.url).let { true }
            }

            binding.content.onTapListener = object : OnTapListener {
                override fun onTap(view: View?, ev: MotionEvent) {
                    onItemSingleTapConfirmed.invoke(ev)
                }
            }

            binding.progressBar.isIndeterminate = true
            ProgressInterceptor.addListener(
                page.url.toHttpUrlOrNull().toString(),
                object : OnProgressChangeListener {
                    override fun onProgressChange(
                        bytesRead: Long,
                        contentLength: Long,
                        done: Boolean
                    ) {
                        binding.progressBar.post {
                            val max = binding.progressBar.max
                            val progress = (max * bytesRead / contentLength).toInt()
                            if (progress in 0..max) {
                                binding.progressBar.isIndeterminate = false
                                binding.progressBar.progress = progress.coerceIn(0, 100)
                            }
                        }
                    }
                })

            Glide.with(context)
                .asBitmap()
                .timeout(20000)
                .load(page.url)
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
                        ProgressInterceptor.removeListener(page.url)
                        setContent()
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        ProgressInterceptor.removeListener(page.url)
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