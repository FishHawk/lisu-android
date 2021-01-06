package com.fishhawk.driftinglibraryandroid.widget.comicimageview

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator

class ScaleRunnable(
    private val view: View,
    private val originScale: Float, private val targetScale: Float,
    private val focalX: Float, private val focalY: Float,
    private val scaleFunction: (Float, Float, Float) -> Unit,
    private val onFinished: () -> Unit
) : Runnable {

    private val interpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val startTime: Long = System.currentTimeMillis()
    private val duration = DEFAULT_DURATION

    override fun run() {
        val interpolation = getInterpolation()
        val currentScale = originScale + interpolation * (targetScale - originScale)
        scaleFunction(currentScale, focalX, focalY)
        if (interpolation < 1f) view.postOnAnimation(this)
        else onFinished()
    }

    private fun getInterpolation(): Float {
        val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
        return interpolator.getInterpolation(progress.coerceAtMost(1.0f))
    }

    companion object {
        private const val DEFAULT_DURATION = 200
    }
}