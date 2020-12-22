package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.fishhawk.driftinglibraryandroid.databinding.ReaderViewPagerBinding
import com.fishhawk.driftinglibraryandroid.util.dpToPx

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

    override var pageIntervalEnabled: Boolean = false
        set(value) {
            val transformer = if (value) MarginPageTransformer(context.dpToPx(16)) else null
            binding.content.setPageTransformer(transformer)
            field = value
        }


    init {
        adapter.isContinuous = false
        binding.content.adapter = adapter
    }

    override fun getPage(): Int = binding.content.currentItem
    override fun setPage(page: Int) = binding.content.setCurrentItem(page, false)

    override fun toNext() {
        if (binding.content.currentItem == adapter.itemCount - 1) onRequestNextChapter?.invoke()
        else binding.content.currentItem += 1
    }

    override fun toPrev() {
        if (binding.content.currentItem == 0) onRequestPrevChapter?.invoke()
        else binding.content.currentItem -= 1
    }


    private val onPageSelectedCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onPageChanged?.invoke(getPage())
        }
    }

    private val reachEdgeDetector = ReachEdgeDetector(this, binding.content)

    init {
        binding.content.registerOnPageChangeCallback(onPageSelectedCallback)
        reachEdgeDetector.attach()
    }
}

class ReachEdgeDetector(
    private val reader: ReaderViewPager,
    private val pager: ViewPager2
) {
    var isScrollingForward = false

    private val factory = object : RecyclerView.EdgeEffectFactory() {
        override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
            return object : EdgeEffect(view.context) {
                override fun onPull(deltaDistance: Float, displacement: Float) {
                    if (isFinished) {
                        if (reader.isHorizontal && reader.isLtr)
                            isScrollingForward = direction == DIRECTION_RIGHT
                        else if (reader.isHorizontal && reader.isRtl)
                            isScrollingForward = direction == DIRECTION_LEFT
                        else if (reader.isVertical)
                            isScrollingForward = direction == DIRECTION_BOTTOM
                    }
                    super.onPull(deltaDistance, displacement)
                }
            }
        }
    }

    private val callback = object : ViewPager2.OnPageChangeCallback() {
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

        override fun onPageScrollStateChanged(state: Int) {
            when (state) {
                ViewPager2.SCROLL_STATE_IDLE -> {
                    reachStart = reachStart && !canScrollBackward()
                    reachEnd = reachEnd && !canScrollForward()

                    if (isUserInput && !hasSetting) {
                        if (reachStart && !reachEnd) reader.onRequestPrevChapter?.invoke()
                        else if (!reachStart && reachEnd) reader.onRequestNextChapter?.invoke()
                        else if (reachStart && reachEnd) {
                            if (isScrollingForward) reader.onRequestNextChapter?.invoke()
                            else reader.onRequestPrevChapter?.invoke()
                        }
                    }
                    reset()
                }
                ViewPager2.SCROLL_STATE_DRAGGING -> {
                    isUserInput = true
                    reachStart = !canScrollBackward()
                    reachEnd = !canScrollForward()
                }
                ViewPager2.SCROLL_STATE_SETTLING -> {
                    hasSetting = true
                }
            }
        }

        private fun canScrollForward(): Boolean {
            val direction = if (reader.isRtl) -1 else 1
            return when (reader.readingOrientation) {
                ReaderView.ReadingOrientation.HORIZONTAL -> pager.canScrollHorizontally(direction)
                ReaderView.ReadingOrientation.VERTICAL -> pager.canScrollVertically(direction)
            }
        }

        private fun canScrollBackward(): Boolean {
            val direction = if (reader.isRtl) 1 else -1
            return when (reader.readingOrientation) {
                ReaderView.ReadingOrientation.HORIZONTAL -> pager.canScrollHorizontally(direction)
                ReaderView.ReadingOrientation.VERTICAL -> pager.canScrollVertically(direction)
            }
        }
    }

    fun attach() {
        pager.registerOnPageChangeCallback(callback)
        (pager.getChildAt(0) as RecyclerView).edgeEffectFactory = factory
    }
}