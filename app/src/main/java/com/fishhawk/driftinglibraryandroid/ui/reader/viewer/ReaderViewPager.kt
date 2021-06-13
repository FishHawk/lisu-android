package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.fishhawk.driftinglibraryandroid.databinding.ReaderViewPagerBinding
import com.fishhawk.driftinglibraryandroid.util.dpToPx
import com.fishhawk.driftinglibraryandroid.widget.ViewState

class ReaderViewPager constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ReaderView(context, attrs, defStyleAttr) {

    private val binding = ReaderViewPagerBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    override var readingOrientation: ReadingOrientation = ReadingOrientation.HORIZONTAL
        set(value) {
            binding.content.orientation = when (value) {
                ReadingOrientation.HORIZONTAL -> ViewPager2.ORIENTATION_HORIZONTAL
                ReadingOrientation.VERTICAL -> ViewPager2.ORIENTATION_VERTICAL
            }
            field = value
        }

    override var isPageIntervalEnabled: Boolean = false
        set(value) {
            val transformer = if (value) MarginPageTransformer(context.dpToPx(16)) else null
            binding.content.setPageTransformer(transformer)
            field = value
        }


    init {
        adapter.isContinuous = false
        binding.content.adapter = adapter
        binding.content.offscreenPageLimit = 3
    }

    override fun setPrevChapterState(state: ViewState) {
        if (state is ViewState.Content &&
            adapter.list[binding.content.currentItem] !is Page.PrevTransitionPage
        ) adapter.removePrevChapterState()
        else adapter.updatePrevChapterState(state)
    }

    override fun setNextChapterState(state: ViewState) {
        if (state is ViewState.Content &&
            adapter.list[binding.content.currentItem] !is Page.NextTransitionPage
        ) adapter.removeNextChapterState()
        else adapter.updateNextChapterState(state)
    }

    override fun setPage(page: Int) {
        val index = adapter.list.indexOfFirst { it is Page.ContentPage && it.index == page }
        binding.content.setCurrentItem(index, false)
    }

    override fun toNext() {
        if (binding.content.currentItem == adapter.itemCount - 1) onRequestNextChapter?.invoke()
        else binding.content.currentItem += 1
    }

    override fun toPrev() {
        if (binding.content.currentItem == 0) onRequestPrevChapter?.invoke()
        else binding.content.currentItem -= 1
    }


    init {
        binding.content.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val page = adapter.list[position]
                if (page is Page.ContentPage) onPageChanged?.invoke(page.index) ?: Unit
            }
        })
        object : OverScrollDetector(binding.content) {
            override fun onOverScrollBackward() = onRequestPrevChapter?.invoke() ?: Unit
            override fun onOverScrollForward() = onRequestNextChapter?.invoke() ?: Unit
        }
    }
}


abstract class OverScrollDetector(private val pager: ViewPager2) {
    abstract fun onOverScrollBackward()
    abstract fun onOverScrollForward()

    private var isScrollingForward = false

    private val factory = object : RecyclerView.EdgeEffectFactory() {
        override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
            fun updateScrollDirection() {
                isScrollingForward =
                    if (pager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                        if (pager.layoutDirection == ViewPager2.LAYOUT_DIRECTION_RTL)
                            direction == DIRECTION_LEFT
                        else direction == DIRECTION_RIGHT
                    } else direction == DIRECTION_BOTTOM
            }

            return object : EdgeEffect(view.context) {
                override fun onPull(deltaDistance: Float, displacement: Float) {
                    if (isFinished) updateScrollDirection()
                    super.onPull(deltaDistance, displacement)
                }
            }
        }
    }

    private val callback = object : ViewPager2.OnPageChangeCallback() {
        private var isUserInput = false
        private var hasSetting = false
        private var isFirstPage = false
        private var isLastPage = false

        private fun reset() {
            hasSetting = false
            isUserInput = false
            isFirstPage = false
            isLastPage = false
        }

        private fun isLastPage(): Boolean {
            return pager.adapter?.let { pager.currentItem == it.itemCount - 1 } ?: true
        }

        private fun isFirstPage(): Boolean {
            return pager.adapter?.let { pager.currentItem == 0 } ?: true
        }

        override fun onPageScrollStateChanged(state: Int) {
            when (state) {
                ViewPager2.SCROLL_STATE_IDLE -> {
                    isFirstPage = isFirstPage && isFirstPage()
                    isLastPage = isLastPage && isLastPage()

                    if (isUserInput && !hasSetting) {
                        if (isFirstPage && !isLastPage) onOverScrollBackward()
                        else if (!isFirstPage && isLastPage) onOverScrollForward()
                        else if (isFirstPage && isLastPage) {
                            if (isScrollingForward) onOverScrollForward() else onOverScrollBackward()
                        }
                    }
                    reset()
                }
                ViewPager2.SCROLL_STATE_DRAGGING -> {
                    isUserInput = true
                    isFirstPage = isFirstPage()
                    isLastPage = isLastPage()
                }
                ViewPager2.SCROLL_STATE_SETTLING -> {
                    hasSetting = true
                }
            }
        }
    }

    init {
        pager.registerOnPageChangeCallback(callback)
        (pager.getChildAt(0) as RecyclerView).edgeEffectFactory = factory
    }
}