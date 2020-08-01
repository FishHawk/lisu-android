package com.fishhawk.driftinglibraryandroid.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
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
    private val pagerSnapHelper = PagerSnapHelper()
    private val layoutManager = LinearLayoutManager(context)
    val adapter = ImageListAdapter(context)

    private var isSnapAttached = false
        set(value) {
            if (value) pagerSnapHelper.attachToRecyclerView(binding.content)
            else pagerSnapHelper.attachToRecyclerView(null)
            field = value
        }

    init {
        binding.content.layoutManager = layoutManager
        binding.content.adapter = adapter
    }


    enum class Mode { LTR, RTL, VERTICAL }

    var mode = Mode.LTR
        set(value) {
            isSnapAttached = (value != Mode.VERTICAL)
            layoutManager.reverseLayout = (value == Mode.RTL)
            layoutManager.orientation =
                if (value == Mode.VERTICAL) LinearLayoutManager.VERTICAL
                else LinearLayoutManager.HORIZONTAL
            adapter.isContinuous = !isSnapAttached

            if (adapter.itemCount > 0) {
                val page = getPage()
                binding.content.adapter = binding.content.adapter
                setPage(page)
            } else {
                binding.content.adapter = binding.content.adapter
            }

            field = value
        }

    var onRequestPrevChapter: (() -> Unit)? = null
    var onRequestNextChapter: (() -> Unit)? = null
    var onRequestMenu: (() -> Unit)? = null
    var onScrolled: ((Int) -> Unit)? = null

    fun getPage(): Int = layoutManager.findFirstVisibleItemPosition()
    fun setPage(page: Int) {
        if (adapter.itemCount != 0 && page >= 0 && page < adapter.itemCount)
            recyclerView.scrollToPosition(page)
    }

    fun setContent(content: List<String>) {
        adapter.changeList(content.toMutableList())
    }

    private fun canScrollForward(): Boolean {
        val direction = if (layoutManager.reverseLayout) -1 else 1
        return when (layoutManager.orientation) {
            LinearLayoutManager.HORIZONTAL -> recyclerView.canScrollHorizontally(direction)
            LinearLayoutManager.VERTICAL -> recyclerView.canScrollVertically(direction)
            else -> false
        }
    }

    private fun canScrollBackward(): Boolean {
        val direction = if (layoutManager.reverseLayout) 1 else -1
        return when (layoutManager.orientation) {
            LinearLayoutManager.HORIZONTAL -> recyclerView.canScrollHorizontally(direction)
            LinearLayoutManager.VERTICAL -> recyclerView.canScrollVertically(direction)
            else -> false
        }
    }


    /*
     * Scroll
     */

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


    /*
     * Click
     */

    private val smoothScrollFactor = 0.8

    private fun gotoNextPage() {
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

    private fun gotoPrevPage() {
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

    private val detector = GestureDetectorCompat(context, object :
        GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(ev: MotionEvent?): Boolean {
            val onLeftAreaClicked = {
                if (mode == Mode.RTL) gotoNextPage()
                else gotoPrevPage()
            }
            val onRightAreaClicked = {
                if (mode == Mode.RTL) gotoPrevPage()
                else gotoNextPage()
            }

            val percentageX = ev?.x?.div(width)
            val threshold = 0.3

            if (percentageX != null) {
                when {
                    percentageX < threshold -> onLeftAreaClicked()
                    percentageX > 1 - threshold -> onRightAreaClicked()
                    else -> onRequestMenu?.invoke()
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
}