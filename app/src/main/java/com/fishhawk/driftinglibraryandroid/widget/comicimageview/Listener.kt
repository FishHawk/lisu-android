package com.fishhawk.driftinglibraryandroid.widget.comicimageview

import android.view.MotionEvent
import android.view.View

interface OnDragListener {
    fun onDrag(dx: Float, dy: Float)
}

interface OnFlingListener {
    fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float)
}

interface OnScaleListener {
    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)
}

interface OnTapListener {
    fun onTap(view: View?, ev: MotionEvent)
}
