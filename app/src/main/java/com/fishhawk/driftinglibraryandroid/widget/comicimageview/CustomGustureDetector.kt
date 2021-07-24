package com.fishhawk.driftinglibraryandroid.widget.comicimageview

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener

internal class CustomGestureDetector(
    context: Context?,
    listener: OnGestureListener
) {
    private val scaleDetector = ScaleGestureDetector(context, listener)
    private val normalDetector = GestureDetector(context, listener)

    val isScaling: Boolean
        get() = scaleDetector.isInProgress

    fun onTouchEvent(ev: MotionEvent): Boolean {
        return try {
            scaleDetector.onTouchEvent(ev)
            normalDetector.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            // Fix for support lib bug, happening when onDestroy is called
            true
        }
    }

    abstract class OnGestureListener :
        GestureDetector.SimpleOnGestureListener(),
        OnScaleGestureListener {
        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean = true
        override fun onScaleEnd(detector: ScaleGestureDetector?) {}
    }
}
