package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.widget.ViewState

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
    val isRtl get() = readingDirection == ReadingDirection.RTL

    abstract var isPageIntervalEnabled: Boolean

    val adapter = ReaderViewAdapter(context)
    abstract fun setPrevChapterState(state: ViewState)
    abstract fun setNextChapterState(state: ViewState)
    fun refreshPage(page: Int) = adapter.notifyItemChanged(page)

    var onRequestPrevChapter: (() -> Unit)? = null
    var onRequestNextChapter: (() -> Unit)? = null
    var onRequestMenuVisibility: (() -> Boolean)? = null
    var onRequestMenu: ((isEnabled: Boolean) -> Unit)? = null
    var onPageChanged: ((Int) -> Unit)? = null
    var onPageLongClicked: ((Int, String) -> Unit)
        set(value) {
            adapter.onItemLongPress = value
        }
        get() = adapter.onItemLongPress

    private val isMenuVisible: Boolean
        get() = onRequestMenuVisibility?.invoke() == true


    abstract fun setPage(page: Int)

    protected abstract fun toNext()
    protected abstract fun toPrev()

    private fun toLeft() = if (isRtl) toNext() else toPrev()
    private fun toRight() = if (isRtl) toPrev() else toNext()

    /* Touch */
    init {
        adapter.onItemSingleTapConfirmed = { ev ->
            if (isMenuVisible) {
                onRequestMenu?.invoke(false)
            } else {
                val percentageX = (ev.rawX - left).div(width)
                val threshold = 0.3

                when {
                    percentageX < threshold -> toLeft()
                    percentageX > 1 - threshold -> toRight()
                    else -> {
                        onRequestMenu?.invoke(true)
                    }
                }
            }
        }
        adapter.readerView = this
    }
}