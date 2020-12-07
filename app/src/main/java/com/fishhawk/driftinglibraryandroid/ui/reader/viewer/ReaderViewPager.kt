package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.databinding.ReaderViewPagerBinding

class ReaderViewPager constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ReaderView(context, attrs, defStyleAttr) {

    private val binding = ReaderViewPagerBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val pagerSnapHelper = PagerSnapHelper()

    val layoutManager = LinearLayoutManager(context)
    override fun getPage(): Int = layoutManager.findFirstVisibleItemPosition()
    override fun setPage(page: Int) {
        if (adapter.itemCount != 0 && page >= 0 && page < adapter.itemCount)
            binding.content.scrollToPosition(page)
    }

    override fun canScrollForward(): Boolean {
        val direction = if (layoutManager.reverseLayout) -1 else 1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    override fun canScrollBackward(): Boolean {
        val direction = if (layoutManager.reverseLayout) 1 else -1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    init {
        adapter.isContinuous = false
        pagerSnapHelper.attachToRecyclerView(binding.content)

        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
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

                        if (isUserInput && !hasSetting) {
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
                onScrolled?.invoke(getPage())
            }
        })
    }

    override fun toNext() {
        val currentItem = layoutManager.findFirstVisibleItemPosition()
        if (currentItem == adapter.itemCount - 1) onRequestNextChapter?.invoke()
        else binding.content.smoothScrollToPosition(currentItem + 1)

    }

    override fun toPrev() {
        val currentItem = layoutManager.findFirstVisibleItemPosition()
        if (currentItem == 0) onRequestPrevChapter?.invoke()
        else binding.content.smoothScrollToPosition(currentItem - 1)
    }
}