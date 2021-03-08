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
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderChapterImageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderEmptyPageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageTransitionNextBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageTransitionPrevBinding
import com.fishhawk.driftinglibraryandroid.ui.base.BaseAdapter
import com.fishhawk.driftinglibraryandroid.util.glide.OnProgressChangeListener
import com.fishhawk.driftinglibraryandroid.util.glide.ProgressInterceptor
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import com.fishhawk.driftinglibraryandroid.widget.comicimageview.OnTapListener
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class AdjacentChapter(
    val title: String,
    var state: ViewState
)

data class ReaderContent(
    val title: String,
    var imageUrls: List<String>,

    val prev: AdjacentChapter?,
    val next: AdjacentChapter?
)

sealed class Page {
    object EmptyPage : Page()

    data class ContentPage(
        val index: Int,
        val url: String
    ) : Page()

    data class PrevTransitionPage(
        val currTitle: String,
        val prevTitle: String,
        var prevState: ViewState
    ) : Page()

    data class NextTransitionPage(
        val currTitle: String,
        val nextTitle: String,
        var nextState: ViewState
    ) : Page()
}

class ReaderViewAdapter(private val context: Context) : BaseAdapter<Page>() {

    var onItemLongPress: ((position: Int, url: String) -> Unit) = { _, _ -> }
    var onItemSingleTapConfirmed: ((event: MotionEvent) -> Unit) = {}

    lateinit var readerView: ReaderView

    var isContinuous = false
    var isAreaInterpolationEnabled = false

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

    fun updatePrevChapterState(state: ViewState) {
        val page = list.first()
        if (page is Page.PrevTransitionPage) {
            page.prevState = state
            notifyItemChanged(0)
        }
    }

    fun removePrevChapterState() {
        val page = list.first()
        if (page is Page.PrevTransitionPage) {
            list.removeFirst()
            notifyItemRemoved(0)
        }
    }

    fun updateNextChapterState(state: ViewState) {
        val page = list.last()
        if (page is Page.NextTransitionPage) {
            page.nextState = state
            notifyItemChanged(list.lastIndex)
        }
    }

    fun removeNextChapterState() {
        val page = list.last()
        if (page is Page.NextTransitionPage) {
            list.removeLast()
            notifyItemRemoved(list.size)
        }
    }

    fun setReaderContent(content: ReaderContent) {
        val newList = mutableListOf<Page>()

        if (content.prev != null &&
            content.prev.state != ViewState.Content
        ) newList.add(
            Page.PrevTransitionPage(
                content.title,
                content.prev.title,
                content.prev.state
            )
        )

        if (content.imageUrls.isEmpty()) newList.add(Page.EmptyPage)
        else newList.addAll(content.imageUrls.mapIndexed { index, url ->
            Page.ContentPage(index, url)
        })

        if (content.next != null &&
            content.next.state != ViewState.Content
        ) newList.add(
            Page.NextTransitionPage(
                content.title,
                content.next.title,
                content.next.state
            )
        )

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
            binding.currChapterName.text = page.currTitle
            binding.prevChapterName.text = page.prevTitle
            binding.multiStateView.viewState = page.prevState
            binding.multiStateView.onRetry = { readerView.onRequestPrevChapter?.invoke() }

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
            binding.currChapterName.text = page.currTitle
            binding.nextChapterName.text = page.nextTitle
            binding.multiStateView.viewState = page.nextState
            binding.multiStateView.onRetry = { readerView.onRequestNextChapter?.invoke() }

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
            binding.content.isAreaInterpolationEnabled = isAreaInterpolationEnabled
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