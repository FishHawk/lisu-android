package com.fishhawk.driftinglibraryandroid.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat

class ReaderLayout : FrameLayout {
    constructor(context: Context) :
            super(context)

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    var onClickLeftAreaListener: (() -> Unit)? = null
    var onClickRightAreaListener: (() -> Unit)? = null
    var onClickCenterAreaListener: (() -> Unit)? = null
    var onLongClickListener: (() -> Unit)? = null

    private val detector = GestureDetectorCompat(context, object :
        GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(ev: MotionEvent?): Boolean {
            val percentageX = ev?.x?.div(width)
            val threshold = 0.3

            if (percentageX != null) {
                when {
                    percentageX < threshold -> onClickLeftAreaListener?.let { it() }
                    percentageX > 1 - threshold -> onClickRightAreaListener?.let { it() }
                    else -> onClickCenterAreaListener?.let { it() }
                }
            }
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            onLongClickListener?.let { it() }
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