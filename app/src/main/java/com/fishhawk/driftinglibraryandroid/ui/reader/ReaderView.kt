package com.fishhawk.driftinglibraryandroid.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.databinding.ReaderViewBinding

class ReaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ReaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val recyclerView = binding.content
    private val layoutManager = LinearLayoutManager(context)
    val adapter = ImageListAdapter(context)

    private val pagerSnapHelper = PagerSnapHelper()
    private var isSnapAttached = false
        set(value) {
            adapter.isContinuous = !value
            if (value) pagerSnapHelper.attachToRecyclerView(binding.content)
            else pagerSnapHelper.attachToRecyclerView(null)
            field = value
        }

    enum class ReadingDirection { LTR, RTL, TTB }

    private var readingDirection = ReadingDirection.LTR
        set(value) {
            layoutManager.reverseLayout = (value == ReadingDirection.RTL)
            layoutManager.orientation =
                if (value == ReadingDirection.TTB) LinearLayoutManager.VERTICAL
                else LinearLayoutManager.HORIZONTAL
            field = value
        }

    init {
        binding.content.layoutManager = layoutManager
        binding.content.adapter = adapter
    }


    enum class Preset { LTR, RTL, VERTICAL }

    fun applyPreset(preset: Preset) {
        isSnapAttached = (preset != Preset.VERTICAL)
        readingDirection = when (preset) {
            Preset.LTR -> ReadingDirection.LTR
            Preset.RTL -> ReadingDirection.RTL
            Preset.VERTICAL -> ReadingDirection.TTB
        }

        if (adapter.itemCount > 0) {
            val page = getPage()
            binding.content.adapter = binding.content.adapter
            setPage(page)
        } else {
            binding.content.adapter = binding.content.adapter
        }
    }

    var isMenuVisible = false
    var onRequestPrevChapter: (() -> Unit)? = null
    var onRequestNextChapter: (() -> Unit)? = null
    var onRequestMenu: ((isEnabled: Boolean) -> Unit)? = null
    var onScrolled: ((Int) -> Unit)? = null
    var onPageLongClicked: ((Int, String) -> Unit)?
        set(value) {
            adapter.onPageLongClicked = value
        }
        get() = adapter.onPageLongClicked

    fun getPage(): Int = layoutManager.findFirstVisibleItemPosition()
    fun setPage(page: Int) {
        if (adapter.itemCount != 0 && page >= 0 && page < adapter.itemCount)
            recyclerView.scrollToPosition(page)
    }

    fun refreshPage(page: Int) {
        adapter.notifyItemChanged(page)
    }

    fun setContent(content: List<String>) {
        adapter.setList(content)
    }

    private fun canScrollForward(): Boolean {
        val direction = if (layoutManager.reverseLayout) -1 else 1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> recyclerView.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> recyclerView.canScrollVertically(direction)
            else -> false
        }
    }

    private fun canScrollBackward(): Boolean {
        val direction = if (layoutManager.reverseLayout) 1 else -1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> recyclerView.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> recyclerView.canScrollVertically(direction)
            else -> false
        }
    }


    /* Scroll */
    init {
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

                        if (isUserInput && ((isSnapAttached && !hasSetting) || !isSnapAttached)) {
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


    /* Move */
    private val smoothScrollFactor = 0.8

    private fun toNext() {
        if (isSnapAttached) {
            val currentItem = layoutManager.findFirstVisibleItemPosition()
            if (currentItem == adapter.itemCount - 1) onRequestNextChapter?.invoke()
            else recyclerView.smoothScrollToPosition(currentItem + 1)
        } else {
            if (canScrollForward()) {
                val scrollDistanceH = (width * smoothScrollFactor).toInt()
                val scrollDistanceV = (height * smoothScrollFactor).toInt()
                recyclerView.smoothScrollBy(scrollDistanceH, scrollDistanceV)
            } else onRequestNextChapter?.invoke()
        }
    }

    private fun toPrev() {
        if (isSnapAttached) {
            val currentItem = layoutManager.findFirstVisibleItemPosition()
            if (currentItem == 0) onRequestPrevChapter?.invoke()
            else recyclerView.smoothScrollToPosition(currentItem - 1)
        } else {
            if (canScrollBackward()) {
                val scrollDistanceH = -(width * smoothScrollFactor).toInt()
                val scrollDistanceV = -(height * smoothScrollFactor).toInt()
                recyclerView.smoothScrollBy(scrollDistanceH, scrollDistanceV)
            } else onRequestPrevChapter?.invoke()
        }
    }

    private fun toLeft() {
        if (readingDirection == ReadingDirection.RTL) toNext()
        else toPrev()
    }

    private fun toRight() {
        if (readingDirection == ReadingDirection.RTL) toPrev()
        else toNext()
    }


    /* Touch */
    private val detector = GestureDetectorCompat(context, object :
        GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(ev: MotionEvent?): Boolean {
            if (isMenuVisible) {
                isMenuVisible = false
                onRequestMenu?.invoke(isMenuVisible)
            } else {
                val percentageX = ev?.x?.div(width)
                val threshold = 0.3

                if (percentageX != null) {
                    when {
                        percentageX < threshold -> toLeft()
                        percentageX > 1 - threshold -> toRight()
                        else -> {
                            isMenuVisible = true
                            onRequestMenu?.invoke(isMenuVisible)
                        }
                    }
                }
            }
            return true
        }
    })

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return detector.onTouchEvent(ev)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { onTouchEvent(it) }
        return super.dispatchTouchEvent(ev)
    }


    /* Key Event */
    var volumeKeysEnabled: Boolean = true

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> (volumeKeysEnabled && !isMenuVisible).also { if (it) toNext() }
            KeyEvent.KEYCODE_VOLUME_DOWN -> (volumeKeysEnabled && !isMenuVisible).also { if (it) toPrev() }
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                isMenuVisible = !isMenuVisible
                onRequestMenu?.invoke(isMenuVisible)
            }

            KeyEvent.KEYCODE_N -> onRequestNextChapter?.invoke()
            KeyEvent.KEYCODE_P -> onRequestPrevChapter?.invoke()

            KeyEvent.KEYCODE_DPAD_RIGHT -> toRight()
            KeyEvent.KEYCODE_DPAD_LEFT -> toLeft()

            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_PAGE_UP -> toPrev()

            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_PAGE_DOWN -> toNext()
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}