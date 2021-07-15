package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fishhawk.driftinglibraryandroid.databinding.ReaderViewContinuousBinding
import com.fishhawk.driftinglibraryandroid.widget.ViewState

class ReaderViewContinuous constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ReaderView(context, attrs, defStyleAttr) {

    private val binding = ReaderViewContinuousBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val layoutManager = LinearLayoutManager(context)

    override var readingOrientation: ReadingOrientation = ReadingOrientation.VERTICAL
        set(value) {
            layoutManager.orientation = when (value) {
                ReadingOrientation.HORIZONTAL -> ViewPager2.ORIENTATION_HORIZONTAL
                ReadingOrientation.VERTICAL -> ViewPager2.ORIENTATION_VERTICAL
            }
            field = value
        }

    private val itemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == 0) return
            val offset = (16 * resources.displayMetrics.density + 0.5f).toInt()
            when (readingOrientation) {
                ReadingOrientation.HORIZONTAL -> outRect.left = offset
                ReadingOrientation.VERTICAL -> outRect.top = offset
            }
        }
    }

    override var isPageIntervalEnabled: Boolean = false
        set(value) {
            if (value) binding.content.addItemDecoration(itemDecoration)
            else binding.content.removeItemDecoration(itemDecoration)
            field = value
        }

    init {
        adapter.isContinuous = true

        binding.content.layoutManager = layoutManager
        binding.content.adapter = adapter

        binding.content.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var isUserInput = false
            private var hasSetting = false
            private var reachStart = false
            private var reachEnd = false

            fun reset() {
                hasSetting = false
                isUserInput = false
                reachStart = false
                reachEnd = false
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, state: Int) {
                when (state) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        reachStart = reachStart && !canScrollBackward()
                        reachEnd = reachEnd && !canScrollForward()

                        if (isUserInput) {
                            // TODO: what if reachStart and reachEnd are both true
                            if (reachStart) onRequestPrevChapter?.invoke()
                            if (reachEnd) onRequestNextChapter?.invoke()
                        }
                        reset()
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        isUserInput = true
                        reachStart = !canScrollBackward()
                        reachEnd = !canScrollForward()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        hasSetting = true
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onPageChanged?.invoke(layoutManager.findFirstVisibleItemPosition())
            }
        })
    }

    override fun setPrevChapterState(state: ViewState) {
        TODO("Not yet implemented")
    }

    override fun setNextChapterState(state: ViewState) {
        TODO("Not yet implemented")
    }

    override fun setPage(page: Int) {
        val index = adapter.list.indexOfFirst { it is Page.ContentPage && it.index == page }
        binding.content.scrollToPosition(index)
    }

    fun canScrollForward(): Boolean {
        val direction = if (layoutManager.reverseLayout) -1 else 1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    fun canScrollBackward(): Boolean {
        val direction = if (layoutManager.reverseLayout) 1 else -1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    private val smoothScrollFactor = 0.8
    override fun toNext() {
        if (canScrollForward()) {
            val scrollDistanceH = (width * smoothScrollFactor).toInt()
            val scrollDistanceV = (height * smoothScrollFactor).toInt()
            binding.content.smoothScrollBy(scrollDistanceH, scrollDistanceV)
        } else onRequestNextChapter?.invoke()
    }

    override fun toPrev() {
        if (canScrollBackward()) {
            val scrollDistanceH = -(width * smoothScrollFactor).toInt()
            val scrollDistanceV = -(height * smoothScrollFactor).toInt()
            binding.content.smoothScrollBy(scrollDistanceH, scrollDistanceV)
        } else onRequestPrevChapter?.invoke()
    }
}