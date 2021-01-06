package com.fishhawk.driftinglibraryandroid.widget.comicimageview

import android.graphics.RectF
import android.view.View
import android.widget.OverScroller
import kotlin.math.roundToInt

class FlingRunnable(
    private val view: View,
    rect: RectF,
    viewWidth: Int, viewHeight: Int,
    velocityX: Int, velocityY: Int,
    private val listener: (Float, Float) -> Unit
) : Runnable {

    private val scroller: OverScroller = OverScroller(view.context)
    private var currentX = 0
    private var currentY = 0

    init {
        val startX = (-rect.left).roundToInt()
        val startY = (-rect.top).roundToInt()

        val minX: Int
        val maxX: Int
        if (viewWidth < rect.width()) {
            minX = 0
            maxX = (rect.width() - viewWidth).roundToInt()
        } else {
            maxX = startX
            minX = maxX
        }

        val minY: Int
        val maxY: Int
        if (viewHeight < rect.height()) {
            minY = 0
            maxY = (rect.height() - viewHeight).roundToInt()
        } else {
            maxY = startY
            minY = maxY
        }

        currentX = startX
        currentY = startY

        if (startX != maxX || startY != maxY) {
            scroller.fling(
                startX, startY, velocityX, velocityY, minX,
                maxX, minY, maxY, 0, 0
            )
        }
    }

    override fun run() {
        if (scroller.isFinished) return
        if (scroller.computeScrollOffset()) {
            val newX = scroller.currX
            val newY = scroller.currY
            listener(currentX - newX.toFloat(), currentY - newY.toFloat())
            currentX = newX
            currentY = newY
            view.postOnAnimation(this)
        }
    }

    fun cancelFling() {
        scroller.forceFinished(true)
    }
}