package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.viewpager2.widget.ViewPager2
import com.fishhawk.driftinglibraryandroid.databinding.ReaderViewPagerBinding

class ReaderViewPager constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ReaderView(context, attrs, defStyleAttr) {

    private val binding = ReaderViewPagerBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        adapter.isContinuous = false

        binding.content.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.content.adapter = adapter

        binding.content.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
                            // TODO: what if reachStart and reachEnd are both true
                            if (reachStart) onRequestPrevChapter?.invoke()
                            if (reachEnd) onRequestNextChapter?.invoke()
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

            override fun onPageSelected(position: Int) {
                onScrolled?.invoke(getPage())
            }
        })
    }

    override fun getPage(): Int = binding.content.currentItem
    override fun setPage(page: Int) = binding.content.setCurrentItem(page, false)

    override fun canScrollForward(): Boolean {
        val direction = if (isReversed) -1 else 1
        return when (binding.content.orientation) {
            ViewPager2.ORIENTATION_HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            ViewPager2.ORIENTATION_VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    override fun canScrollBackward(): Boolean {
        val direction = if (isReversed) 1 else -1
        return when (binding.content.orientation) {
            ViewPager2.ORIENTATION_HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            ViewPager2.ORIENTATION_VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    override fun toNext() {
        if (binding.content.currentItem == adapter.itemCount - 1) onRequestNextChapter?.invoke()
        else binding.content.currentItem += 1

    }

    override fun toPrev() {
        if (binding.content.currentItem == 0) onRequestPrevChapter?.invoke()
        else binding.content.currentItem -= 1
    }
}