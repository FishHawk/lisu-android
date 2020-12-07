package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

abstract class ReaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class ReadingOrientation { HORIZONTAL, VERTICAL }
    enum class ReadingDirection { LTR, RTL }

    abstract var readingOrientation: ReadingOrientation

    var readingDirection = ReadingDirection.LTR
        set(value) {
            layoutDirection = when (value) {
                ReadingDirection.LTR -> RecyclerView.LAYOUT_DIRECTION_LTR
                ReadingDirection.RTL -> RecyclerView.LAYOUT_DIRECTION_RTL
            }
            field = value
        }
    abstract var pageIntervalEnabled: Boolean

    val isRtl = readingDirection == ReadingDirection.RTL

    val adapter = ImageListAdapter(context)
    fun refreshPage(page: Int) = adapter.notifyItemChanged(page)
    fun setContent(content: List<String>) = adapter.setList(content)

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


    abstract fun getPage(): Int
    abstract fun setPage(page: Int)

    protected abstract fun canScrollForward(): Boolean
    protected abstract fun canScrollBackward(): Boolean

    protected abstract fun toNext()
    protected abstract fun toPrev()

    protected fun toLeft() = if (isRtl) toNext() else toPrev()
    protected fun toRight() = if (isRtl) toPrev() else toNext()

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